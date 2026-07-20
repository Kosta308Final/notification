package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRecipientRepositoryTest {

    @Autowired
    private NotificationRecipientRepository recipientRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setEventId("evt-recipient-test");
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title");
        notification.setContent("Content");
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));
        notification = notificationRepository.save(notification);
        entityManager.flush();
    }

    @Test
    void testSaveRecipientSuccess() {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(notification);
        recipient.setRecipientUserId(100L);
        recipient.setRead(false);

        NotificationRecipient saved = recipientRepository.save(recipient);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(notification.getId(), saved.getNotification().getId());
        assertEquals(100L, saved.getRecipientUserId());
        assertFalse(saved.isRead());
    }

    @Test
    void testDuplicateRecipientThrowsException() {
        NotificationRecipient recipient1 = new NotificationRecipient();
        recipient1.setNotification(notification);
        recipient1.setRecipientUserId(100L);
        recipientRepository.save(recipient1);
        entityManager.flush();

        NotificationRecipient recipient2 = new NotificationRecipient();
        recipient2.setNotification(notification);
        recipient2.setRecipientUserId(100L); // Duplicate user ID for same notification

        assertThrows(DataIntegrityViolationException.class, () -> {
            recipientRepository.save(recipient2);
            entityManager.flush();
        });
    }

    @Test
    void testMultipleRecipientsForSameNotificationSuccess() {
        NotificationRecipient recipient1 = new NotificationRecipient();
        recipient1.setNotification(notification);
        recipient1.setRecipientUserId(100L);
        recipientRepository.save(recipient1);

        NotificationRecipient recipient2 = new NotificationRecipient();
        recipient2.setNotification(notification);
        recipient2.setRecipientUserId(200L); // Different user
        recipientRepository.save(recipient2);

        entityManager.flush();

        // Count recipients for this specific notification to avoid polluting with other tests
        long count = recipientRepository.findAll().stream()
                .filter(r -> r.getNotification().getId().equals(notification.getId()))
                .count();
        assertEquals(2, count);
    }

    @Test
    void testReadStatusTransition() {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(notification);
        recipient.setRecipientUserId(100L);
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        recipient.setRead(true);
        LocalDateTime now = LocalDateTime.now();
        recipient.setReadAt(now);

        NotificationRecipient updated = recipientRepository.save(recipient);
        entityManager.flush();

        assertTrue(updated.isRead());
        assertEquals(now, updated.getReadAt());
    }
}
