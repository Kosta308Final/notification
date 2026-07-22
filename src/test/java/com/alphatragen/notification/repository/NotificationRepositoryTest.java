package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Tag("jpa")
@Import(JpaConfig.class)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveNotificationSuccess() {
        Notification notification = notification("evt-001")
                .importance(NotificationImportance.NORMAL)
                .title("Test Title")
                .content("Test Content")
                .actionUrl("/test-path")
                .build();

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("evt-001", saved.getEventId());
        assertEquals(NotificationImportance.NORMAL, saved.getImportance());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testDuplicateEventIdThrowsException() {
        Notification notification1 = notification("evt-dup")
                .title("Test 1")
                .content("Content 1")
                .build();

        notificationRepository.save(notification1);
        entityManager.flush();

        Notification notification2 = notification("evt-dup")
                .title("Test 2")
                .content("Content 2")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            notificationRepository.save(notification2);
            entityManager.flush();
        });
    }

    @Test
    void testDefaultImportanceNormal() {
        Notification notification = notification("evt-default-importance").build();

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        assertEquals(NotificationImportance.NORMAL, saved.getImportance());
    }

    @Test
    void testNullActionUrlIsAllowed() {
        Notification notification = notification("evt-null-action-url")
                .actionUrl(null)
                .build();

        assertDoesNotThrow(() -> {
            Notification saved = notificationRepository.save(notification);
            entityManager.flush();
            assertNull(saved.getActionUrl());
        });
    }

    @Test
    void testSaveTargetsViaCascade() {
        Notification notification = notification("evt-cascade-target").build();

        NotificationTarget target = NotificationTarget.builder()
                .targetType(NotificationTargetType.HOUSEHOLD)
                .apartmentId(1L)
                .building("101")
                .unit("1001")
                .build();

        notification.addTarget(target);

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(1, saved.getTargets().size());
        assertNotNull(saved.getTargets().get(0).getId());
        assertEquals("101", saved.getTargets().get(0).getBuilding());
        assertEquals("1001", saved.getTargets().get(0).getUnit());
    }

    @Test
    void testDeleteNotificationCascadesToTargets() {
        Notification notification = notification("evt-cascade-delete").build();

        NotificationTarget target = NotificationTarget.builder()
                .targetType(NotificationTargetType.INDIVIDUAL)
                .apartmentId(1L)
                .userId(999L)
                .build();

        notification.addTarget(target);

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        Long targetId = saved.getTargets().get(0).getId();
        assertNotNull(entityManager.find(NotificationTarget.class, targetId));

        notificationRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Notification.class, saved.getId()));
        assertNull(entityManager.find(NotificationTarget.class, targetId));
    }

    private Notification.NotificationBuilder notification(String eventId) {
        return Notification.builder()
                .eventId(eventId)
                .sourceType(NotificationSourceType.DOMAIN)
                .title("Title")
                .content("Content")
                .retentionUntil(LocalDateTime.now().plusDays(90));
    }
}
