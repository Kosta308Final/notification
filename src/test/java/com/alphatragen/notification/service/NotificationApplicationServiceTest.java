package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.push.PushSender;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationApplicationServiceTest {

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository settingRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private PushSender pushSender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        settingRepository.deleteAll();
    }

    @Test
    @Transactional
    void testCreateNotificationSuccess() {
        // Given
        NotificationSetting setting = new NotificationSetting();
        setting.setApartmentId(1L);
        setting.setRetentionDays(45);
        settingRepository.save(setting);

        NotificationEventDto dto = new NotificationEventDto(
                "evt-100",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        Map<String, String> templateData = new HashMap<>();
        templateData.put("status", "처리중");
        dto.setTemplateData(templateData);
        dto.setActionUrl("/complaints/1");

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        // When
        Notification result = notificationApplicationService.createNotification(dto);

        // Then
        assertNotNull(result.getId());
        assertEquals("evt-100", result.getEventId());
        assertEquals(NotificationImportance.NORMAL, result.getImportance());
        assertEquals(NotificationSourceType.DOMAIN, result.getSourceType());
        assertEquals("민원 처리 상태가 변경되었습니다.", result.getTitle());
        assertEquals("등록하신 민원의 상태가 [처리중](으)로 변경되었습니다.", result.getContent());
        assertEquals("/complaints/1", result.getActionUrl());

        // Check retention period
        LocalDateTime expectedRetention = LocalDateTime.now().plusDays(45);
        assertTrue(result.getRetentionUntil().isAfter(expectedRetention.minusMinutes(1)));
        assertTrue(result.getRetentionUntil().isBefore(expectedRetention.plusMinutes(1)));

        // Verify recipient saved
        assertEquals(1, result.getRecipients().size());
        assertEquals(100L, result.getRecipients().get(0).getRecipientUserId());
    }

    @Test
    @Transactional
    void testCreateNotificationIdempotency() {
        // Given
        NotificationEventDto dto = new NotificationEventDto(
                "evt-dup-check",
                NotificationEventType.COMPLAINT_ANSWER_REGISTERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        // When
        Notification first = notificationApplicationService.createNotification(dto);
        Notification second = notificationApplicationService.createNotification(dto);

        // Then
        assertEquals(first.getId(), second.getId());
        long count = notificationRepository.findAll().stream()
                .filter(n -> n.getEventId().equals("evt-dup-check"))
                .count();
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testPushNotificationFailureDoesNotRollback() {
        // Given
        NotificationEventDto dto = new NotificationEventDto(
                "evt-push-fail",
                NotificationEventType.COMPLAINT_ANSWER_REGISTERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        doThrow(new RuntimeException("Push server error"))
                .when(pushSender).sendPush(anyLong(), anyLong(), any(), any(), any());

        // When
        Notification result = assertDoesNotThrow(() -> notificationApplicationService.createNotification(dto));

        // Then
        assertNotNull(result.getId());
        assertTrue(notificationRepository.existsById(result.getId()));
    }

    @Test
    void testPushSenderCalledAfterCommitOnly() {
        // Given
        NotificationEventDto dto = new NotificationEventDto(
                "evt-tx-commit-check",
                NotificationEventType.COMPLAINT_ANSWER_REGISTERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        // We assert inside the call that no transaction is active
        doAnswer(invocation -> {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            assertFalse(txActive, "Push sending must be executed AFTER transaction commit");
            return null;
        }).when(pushSender).sendPush(anyLong(), anyLong(), any(), any(), any());

        // When
        Notification result = notificationApplicationService.createNotification(dto);

        // Then
        assertNotNull(result.getId());
        verify(pushSender, times(1)).sendPush(eq(result.getId()), eq(100L), any(), any(), any());
    }

    @Test
    void testTransactionRollbackOnRecipientSaveFailure() {
        // Given
        NotificationEventDto dto = new NotificationEventDto(
                "evt-rollback-check",
                NotificationEventType.COMPLAINT_ANSWER_REGISTERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        // User service throws exception causing resolve to fail, which triggers rollback before saving
        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenThrow(new RuntimeException("User resolution failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationApplicationService.createNotification(dto));

        // Verify notification is not in DB
        assertFalse(notificationRepository.existsByEventId("evt-rollback-check"));
        verifyNoInteractions(pushSender);
    }
}
