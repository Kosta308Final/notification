package com.alphatragen.notification.consumer;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import com.alphatragen.notification.service.NotificationApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext
@Tag("kafka")
public class NotificationEventConsumerTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEnv("KAFKA_HEAP_OPTS", "-Xms128M -Xmx256M")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.listener.auto-startup", () -> "true");
        // Ensure retry backoff is small for testing
        registry.add("app.kafka.retry-backoff-ms", () -> "100");
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.alphatragen.notification.repository.NotificationRecipientRepository recipientRepository;

    @Autowired
    private com.alphatragen.notification.repository.PushSubscriptionRepository pushSubscriptionRepository;

    @MockitoSpyBean
    private NotificationApplicationService notificationApplicationService;

    @MockitoBean
    private NotificationTargetResolverComposite targetResolverComposite;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        pushSubscriptionRepository.deleteAll();
        Mockito.reset(targetResolverComposite, notificationApplicationService);
        Mockito.when(targetResolverComposite.resolveTargets(any()))
                .thenReturn(List.of(100L, 101L));
    }

    @Test
    void testConsumeNormalMessage_Success() throws Exception {
        String eventId = UUID.randomUUID().toString();
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        dto.setTemplateData(Map.of("status", "RESOLVED"));
        dto.setActionUrl("/complaints/1");

        kafkaTemplate.send("notification-events", eventId, dto);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Notification> opt = notificationRepository.findByEventId(eventId);
            assertThat(opt).isPresent();
            assertThat(opt.get().getTitle()).isEqualTo("Complaint status updated");
        });
    }

    @Test
    void testConsumeInvalidMessage_ShouldNotCreateNotification() throws Exception {
        String eventId = UUID.randomUUID().toString();
        // Invalid DTO: missing occurredAt or invalid targetType specific fields
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                null, // missing occurredAt
                1L,
                NotificationTargetType.INDIVIDUAL
        );

        kafkaTemplate.send("notification-events", eventId, dto);

        // Sleep to ensure it had time to be processed and skipped/failed
        Thread.sleep(2000);

        Optional<Notification> opt = notificationRepository.findByEventId(eventId);
        assertThat(opt).isEmpty();
    }

    @Test
    void testDuplicateEvent_ShouldIgnoreSecondExecution() throws Exception {
        String eventId = UUID.randomUUID().toString();
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                NotificationEventType.NOTICE_CREATED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.APARTMENT
        );
        dto.setTemplateData(Map.of("noticeTitle", "Urgent Maintenance", "noticeContent", "Water shutdown"));
        dto.setActionUrl("/notice/99");

        // Send duplicate messages
        kafkaTemplate.send("notification-events", eventId, dto);
        kafkaTemplate.send("notification-events", eventId, dto);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Notification> opt = notificationRepository.findByEventId(eventId);
            assertThat(opt).isPresent();
            assertThat(opt.get().getActionUrl()).isEqualTo("/notice/99");
            assertThat(recipientRepository.findAll().stream()
                    .filter(recipient -> recipient.getNotification().getId().equals(opt.get().getId()))
                    .toList())
                    .extracting(com.alphatragen.notification.domain.NotificationRecipient::getRecipientUserId)
                    .containsExactlyInAnyOrder(100L, 101L);
        });

        // Let's check that verify on service call wait or sleep
        Thread.sleep(2000);

        // Even if received twice, idempotency protects and doesn't throw or duplicate
        long count = notificationRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testConsumeMissingPersonDetectedEventFromBackendShape() {
        String message = """
                {
                  "eventId": "%s",
                  "eventType": "MISSING_PERSON_DETECTED",
                  "occurredAt": "2026-07-25T14:31:12+09:00",
                  "sourceService": "apartment-service",
                  "apartmentId": 1,
                  "recipient": {
                    "type": "INDIVIDUAL",
                    "userId": 2001
                  },
                  "templateData": {
                    "missingPersonId": 101,
                    "detectionRequestId": "det-20260725-0001",
                    "detailId": 1,
                    "cameraName": "정문 앞 CCTV",
                    "cameraAddress": "서울시 강남구 예시로 101, 아파트 정문",
                    "imageUrl": "/mock/missing-person/gate-front-001.jpg"
                  },
                  "actionUrl": "/missing-person/detections/1",
                  "urgent": true
                }
                """.formatted(UUID.randomUUID());

        kafkaTemplate.send("notification-events", message);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Notification> opt = notificationRepository.findAll().stream()
                    .filter(notification -> "/missing-person/detections/1".equals(notification.getActionUrl()))
                    .findFirst();
            assertThat(opt).isPresent();
            assertThat(opt.get().getTitle()).isEqualTo("실종자 유사 인물 감지");
            assertThat(opt.get().getContent()).contains("정문 앞 CCTV").contains("아파트 정문");
        });
    }

    @Test
    void testRetryPolicy_MaxThreeAttempts() throws Exception {
        String eventId = UUID.randomUUID().toString();
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        dto.setTemplateData(Map.of("status", "RESOLVED"));

        // Make the service fail intentionally to test retries
        Mockito.doThrow(new RuntimeException("Simulated Transient DB Error"))
                .when(notificationApplicationService).createNotification(any(NotificationEventDto.class));

        kafkaTemplate.send("notification-events", eventId, dto);

        // The service should be invoked exactly 3 times (1 initial + 2 retries)
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationApplicationService, times(3)).createNotification(any(NotificationEventDto.class));
        });
    }

    @Test
    void testConsumeUserWithdrawal_DeactivatesSubscriptions() throws Exception {
        String eventId = UUID.randomUUID().toString();
        
        // Save initial subscription
        com.alphatragen.notification.domain.PushSubscription sub =
                com.alphatragen.notification.domain.PushSubscription.builder()
                        .userId(100L)
                        .apartmentId(1L)
                        .endpoint("https://fcm.googleapis.com/fcm/send/withdraw-test")
                        .p256dh("p256")
                        .auth("auth")
                        .active(true)
                        .build();
        pushSubscriptionRepository.save(sub);
        
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                NotificationEventType.USER_WITHDRAWN,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        kafkaTemplate.send("notification-events", eventId, dto);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var subs = pushSubscriptionRepository.findByUserId(100L);
            assertThat(subs).isNotEmpty();
            assertThat(subs.get(0).isActive()).isFalse();
        });
    }
}
