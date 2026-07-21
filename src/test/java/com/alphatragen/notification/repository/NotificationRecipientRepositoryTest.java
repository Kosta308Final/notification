package com.alphatragen.notification.repository;

import com.alphatragen.notification.config.JpaConfig;
import com.alphatragen.notification.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Tag("jpa")
@Import(JpaConfig.class)
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
        notification = Notification.builder()
                .eventId("evt-recipient-test")
                .sourceType(NotificationSourceType.DOMAIN)
                .title("Title")
                .content("Content")
                .retentionUntil(LocalDateTime.now().plusDays(90))
                .build();
        notification = notificationRepository.save(notification);
        entityManager.flush();
    }

    @Test
    void testSaveRecipientSuccess() {
        NotificationRecipient recipient = NotificationRecipient.builder()
                .notification(notification)
                .recipientUserId(100L)
                .read(false)
                .build();

        NotificationRecipient saved = recipientRepository.save(recipient);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(notification.getId(), saved.getNotification().getId());
        assertEquals(100L, saved.getRecipientUserId());
        assertFalse(saved.isRead());
    }

    @Test
    void testDuplicateRecipientThrowsException() {
        NotificationRecipient recipient1 = recipient(100L);
        recipientRepository.save(recipient1);
        entityManager.flush();

        NotificationRecipient recipient2 = recipient(100L);

        assertThrows(DataIntegrityViolationException.class, () -> {
            recipientRepository.save(recipient2);
            entityManager.flush();
        });
    }

    @Test
    void testMultipleRecipientsForSameNotificationSuccess() {
        NotificationRecipient recipient1 = recipient(100L);
        recipientRepository.save(recipient1);

        NotificationRecipient recipient2 = recipient(200L);
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
        NotificationRecipient recipient = recipient(100L);
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        recipient.markAsRead(now);

        NotificationRecipient updated = recipientRepository.save(recipient);
        entityManager.flush();

        assertTrue(updated.isRead());
        assertEquals(now, updated.getReadAt());
    }

    private NotificationRecipient recipient(Long recipientUserId) {
        return NotificationRecipient.builder()
                .notification(notification)
                .recipientUserId(recipientUserId)
                .build();
    }
}
