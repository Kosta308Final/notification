package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.domain.NotificationSourceType;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.push.PushSender;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
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
        NotificationSetting setting = NotificationSetting.builder()
                .apartmentId(1L)
                .retentionDays(45)
                .build();
        settingRepository.save(setting);

        NotificationEventDto dto = new NotificationEventDto(
                "evt-100",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        dto.setTemplateData(Map.of("status", "IN_PROGRESS"));
        dto.setActionUrl("/complaints/1");

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        Notification result = notificationApplicationService.createNotification(dto);

        assertNotNull(result.getId());
        assertEquals("evt-100", result.getEventId());
        assertEquals(NotificationImportance.NORMAL, result.getImportance());
        assertEquals(NotificationSourceType.DOMAIN, result.getSourceType());
        assertEquals("Complaint status updated", result.getTitle());
        assertEquals("Your complaint status changed to IN_PROGRESS.", result.getContent());
        assertEquals("/complaints/1", result.getActionUrl());

        LocalDateTime expectedRetention = LocalDateTime.now().plusDays(45);
        assertTrue(result.getRetentionUntil().isAfter(expectedRetention.minusMinutes(1)));
        assertTrue(result.getRetentionUntil().isBefore(expectedRetention.plusMinutes(1)));

        assertEquals(1, result.getRecipients().size());
        assertEquals(100L, result.getRecipients().get(0).getRecipientUserId());
    }

    @Test
    @Transactional
    void testCreateNotificationIdempotency() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-dup-check",
                NotificationEventType.COMPLAINT_ANSWERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        Notification first = notificationApplicationService.createNotification(dto);
        Notification second = notificationApplicationService.createNotification(dto);

        assertEquals(first.getId(), second.getId());
        long count = notificationRepository.findAll().stream()
                .filter(n -> n.getEventId().equals("evt-dup-check"))
                .count();
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testPushNotificationFailureDoesNotRollback() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-push-fail",
                NotificationEventType.COMPLAINT_ANSWERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        doThrow(new RuntimeException("Push server error"))
                .when(pushSender).sendPush(anyLong(), anyLong(), any(), any(), any());

        Notification result = assertDoesNotThrow(() -> notificationApplicationService.createNotification(dto));

        assertNotNull(result.getId());
        assertTrue(notificationRepository.existsById(result.getId()));
    }

    @Test
    void testPushSenderCalledAfterCommitOnly() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-tx-commit-check",
                NotificationEventType.COMPLAINT_ANSWERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        doAnswer(invocation -> {
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive(),
                    "Push sending must be executed AFTER transaction commit");
            return null;
        }).when(pushSender).sendPush(anyLong(), anyLong(), any(), any(), any());

        Notification result = notificationApplicationService.createNotification(dto);

        assertNotNull(result.getId());
        verify(pushSender, times(1)).sendPush(eq(result.getId()), eq(100L), any(), any(), any());
    }

    @Test
    void testTransactionRollbackOnRecipientSaveFailure() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-rollback-check",
                NotificationEventType.COMPLAINT_ANSWERED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);

        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenThrow(new RuntimeException("User resolution failed"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> notificationApplicationService.createNotification(dto));

        assertFalse(notificationRepository.existsByEventId("evt-rollback-check"));
        verifyNoInteractions(pushSender);
    }

    @Test
    void maintenanceFeePaymentConfirmedCreatesHouseholdNotificationAndPushesAfterCommit() {
        NotificationEventDto dto = new NotificationEventDto(
                "MAINTENANCE_FEE_PAYMENT_CONFIRMED-900",
                NotificationEventType.MAINTENANCE_FEE_PAYMENT_CONFIRMED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.HOUSEHOLD
        );
        dto.setBuilding("101");
        dto.setUnit("1001");
        dto.setActionUrl("/maintenance-fees/900");
        dto.setTemplateData(Map.of(
                "maintenanceFeeId", "900",
                "householdId", "77",
                "billingMonth", "2026-07",
                "paidAmount", "185000",
                "paidAt", "2026-07-21T10:15:00"
        ));

        when(userServiceClient.findUsersByHousehold(1L, "101", "1001"))
                .thenReturn(List.of(100L, 101L));

        Notification result = notificationApplicationService.createNotification(dto);

        assertNotNull(result.getId());
        assertEquals(NotificationImportance.NORMAL, result.getImportance());
        assertEquals("Maintenance fee payment confirmed", result.getTitle());
        assertEquals("2026-07 maintenance fee payment of 185000 has been confirmed.", result.getContent());
        assertEquals("/maintenance-fees/900", result.getActionUrl());
        assertEquals(2, result.getRecipients().size());
        verify(pushSender).sendPush(result.getId(), 100L, result.getTitle(), result.getContent(), result.getActionUrl());
        verify(pushSender).sendPush(result.getId(), 101L, result.getTitle(), result.getContent(), result.getActionUrl());
    }
}
