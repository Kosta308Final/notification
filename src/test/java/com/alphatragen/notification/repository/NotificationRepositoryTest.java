package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveNotificationSuccess() {
        Notification notification = new Notification();
        notification.setEventId("evt-001");
        notification.setImportance(NotificationImportance.NORMAL);
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Test Title");
        notification.setContent("Test Content");
        notification.setActionUrl("/test-path");
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("evt-001", saved.getEventId());
        assertEquals(NotificationImportance.NORMAL, saved.getImportance());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testDuplicateEventIdThrowsException() {
        Notification notification1 = new Notification();
        notification1.setEventId("evt-dup");
        notification1.setSourceType(NotificationSourceType.DOMAIN);
        notification1.setTitle("Test 1");
        notification1.setContent("Content 1");
        notification1.setRetentionUntil(LocalDateTime.now().plusDays(90));

        notificationRepository.save(notification1);
        entityManager.flush();

        Notification notification2 = new Notification();
        notification2.setEventId("evt-dup");
        notification2.setSourceType(NotificationSourceType.DOMAIN);
        notification2.setTitle("Test 2");
        notification2.setContent("Content 2");
        notification2.setRetentionUntil(LocalDateTime.now().plusDays(90));

        assertThrows(DataIntegrityViolationException.class, () -> {
            notificationRepository.save(notification2);
            entityManager.flush();
        });
    }

    @Test
    void testDefaultImportanceNormal() {
        Notification notification = new Notification();
        notification.setEventId("evt-default-importance");
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title");
        notification.setContent("Content");
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));

        Notification saved = notificationRepository.save(notification);
        entityManager.flush();

        assertEquals(NotificationImportance.NORMAL, saved.getImportance());
    }

    @Test
    void testNullActionUrlIsAllowed() {
        Notification notification = new Notification();
        notification.setEventId("evt-null-action-url");
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title");
        notification.setContent("Content");
        notification.setActionUrl(null);
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));

        assertDoesNotThrow(() -> {
            Notification saved = notificationRepository.save(notification);
            entityManager.flush();
            assertNull(saved.getActionUrl());
        });
    }

    @Test
    void testSaveTargetsViaCascade() {
        Notification notification = new Notification();
        notification.setEventId("evt-cascade-target");
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title");
        notification.setContent("Content");
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));

        NotificationTarget target = new NotificationTarget();
        target.setNotification(notification);
        target.setTargetType(NotificationTargetType.HOUSEHOLD);
        target.setApartmentId(1L);
        target.setBuilding("101");
        target.setUnit("1001");

        notification.setTargets(Collections.singletonList(target));

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
        Notification notification = new Notification();
        notification.setEventId("evt-cascade-delete");
        notification.setSourceType(NotificationSourceType.DOMAIN);
        notification.setTitle("Title");
        notification.setContent("Content");
        notification.setRetentionUntil(LocalDateTime.now().plusDays(90));

        NotificationTarget target = new NotificationTarget();
        target.setNotification(notification);
        target.setTargetType(NotificationTargetType.INDIVIDUAL);
        target.setApartmentId(1L);
        target.setUserId(999L);

        notification.setTargets(Collections.singletonList(target));

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
}
