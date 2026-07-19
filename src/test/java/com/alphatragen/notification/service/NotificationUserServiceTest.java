package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.repository.NotificationRecipientRepository;
import com.alphatragen.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationUserServiceTest {

    @Autowired
    private NotificationUserService notificationUserService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository recipientRepository;

    private final Long user1Id = 100L;
    private final Long user2Id = 200L;
    private final Long apt1Id = 10L;
    private final Long apt2Id = 20L;

    @BeforeEach
    void setUp() {
        recipientRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    private Notification createNotification(String eventId, NotificationImportance importance, LocalDateTime retentionUntil) {
        Notification notification = new Notification();
        notification.setEventId(eventId);
        notification.setImportance(importance);
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title: " + eventId);
        notification.setContent("Content: " + eventId);
        notification.setRetentionUntil(retentionUntil);
        return notificationRepository.save(notification);
    }

    private void addTarget(Notification notification, Long apartmentId) {
        NotificationTarget target = new NotificationTarget();
        target.setNotification(notification);
        target.setTargetType(NotificationTargetType.APARTMENT);
        target.setApartmentId(apartmentId);
        notification.getTargets().add(target);
        notificationRepository.save(notification);
    }

    private NotificationRecipient addRecipient(Notification notification, Long userId) {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(notification);
        recipient.setRecipientUserId(userId);
        recipient.setRead(false);
        return recipientRepository.save(recipient);
    }

    @Test
    void testGetNotificationsFilterAndSort() {
        // Given
        Notification n1 = createNotification("evt1", NotificationImportance.NORMAL, LocalDateTime.now().minusDays(1));
        addTarget(n1, apt1Id);
        addRecipient(n1, user1Id); // Expired

        Notification n2 = createNotification("evt2", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n2, apt1Id);
        addRecipient(n2, user1Id); // Active

        Notification n3 = createNotification("evt3", NotificationImportance.URGENT, LocalDateTime.now().plusDays(5));
        addTarget(n3, apt1Id);
        addRecipient(n3, user1Id); // Active Urgent

        Notification n4 = createNotification("evt4", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n4, apt1Id);
        NotificationRecipient r4 = addRecipient(n4, user1Id);
        r4.setRead(true);
        recipientRepository.save(r4); // Active Read

        Notification n5 = createNotification("evt5", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n5, apt2Id);
        addRecipient(n5, user1Id); // Apt 2

        Notification n6 = createNotification("evt6", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n6, apt1Id);
        addRecipient(n6, user2Id); // User 2

        // When
        Page<NotificationRecipient> result = notificationUserService.getNotifications(user1Id, apt1Id, null, PageRequest.of(0, 20));

        // Then
        // Only active notifications of user 1 in apt 1 (n2, n3, n4)
        assertEquals(3, result.getTotalElements());
        // Urgent & Unread (n3) must be the first
        assertEquals("Title: evt3", result.getContent().get(0).getNotification().getTitle());
    }

    @Test
    void testGetUnreadCount() {
        // Given
        Notification n1 = createNotification("evt1", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n1, apt1Id);
        addRecipient(n1, user1Id); // Active Unread

        Notification n2 = createNotification("evt2", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n2, apt1Id);
        NotificationRecipient r2 = addRecipient(n2, user1Id);
        r2.setRead(true);
        recipientRepository.save(r2); // Active Read

        Notification n3 = createNotification("evt3", NotificationImportance.NORMAL, LocalDateTime.now().minusDays(1));
        addTarget(n3, apt1Id);
        addRecipient(n3, user1Id); // Expired Unread

        // When
        long count = notificationUserService.getUnreadCount(user1Id, apt1Id);

        // Then
        assertEquals(1, count);
    }

    @Test
    void testMarkAsRead() {
        // Given
        Notification n = createNotification("evt1", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n, apt1Id);
        NotificationRecipient r = addRecipient(n, user1Id);

        // When
        NotificationRecipient result = notificationUserService.markAsRead(r.getId(), user1Id);

        // Then
        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());

        // Idempotency: re-read maintains the original readAt timestamp
        LocalDateTime initialReadAt = result.getReadAt();
        NotificationRecipient resultAgain = notificationUserService.markAsRead(r.getId(), user1Id);
        assertEquals(initialReadAt, resultAgain.getReadAt());

        // Forbidden
        assertThrows(ResponseStatusException.class, () -> notificationUserService.markAsRead(r.getId(), user2Id));

        // Not Found
        assertThrows(ResponseStatusException.class, () -> notificationUserService.markAsRead(9999L, user1Id));
    }

    @Test
    void testMarkAllAsRead() {
        // Given
        Notification n1 = createNotification("evt1", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n1, apt1Id);
        NotificationRecipient r1 = addRecipient(n1, user1Id);

        Notification n2 = createNotification("evt2", NotificationImportance.NORMAL, LocalDateTime.now().plusDays(5));
        addTarget(n2, apt1Id);
        NotificationRecipient r2 = addRecipient(n2, user1Id);

        // When
        notificationUserService.markAllAsRead(user1Id, apt1Id);

        // Then
        assertTrue(recipientRepository.findById(r1.getId()).orElseThrow().isRead());
        assertTrue(recipientRepository.findById(r2.getId()).orElseThrow().isRead());
        assertEquals(0, notificationUserService.getUnreadCount(user1Id, apt1Id));
    }
}
