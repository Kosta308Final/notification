package com.alphatragen.notification.repository;

import com.alphatragen.notification.config.JpaConfig;
import com.alphatragen.notification.domain.PushSubscription;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Tag("jpa")
@Import(JpaConfig.class)
class PushSubscriptionRepositoryTest {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSavePushSubscriptionSuccess() {
        PushSubscription subscription = subscription("https://updates.push.services.mozilla.com/wpush/v2/gAAAAAB")
                .browser("Firefox")
                .deviceType("Desktop")
                .build();

        PushSubscription saved = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("https://updates.push.services.mozilla.com/wpush/v2/gAAAAAB", saved.getEndpoint());
        assertTrue(saved.isActive());
    }

    @Test
    void testDuplicateEndpointThrowsException() {
        PushSubscription sub1 = subscription("https://fcm.googleapis.com/fcm/send/duplicate")
                .p256dh("p256dhKey1")
                .auth("authKey1")
                .build();
        pushSubscriptionRepository.save(sub1);
        entityManager.flush();

        PushSubscription sub2 = subscription("https://fcm.googleapis.com/fcm/send/duplicate")
                .userId(2L)
                .p256dh("p256dhKey2")
                .auth("authKey2")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            pushSubscriptionRepository.save(sub2);
            entityManager.flush();
        });
    }

    @Test
    void testSameUserMultipleEndpointsSuccess() {
        PushSubscription sub1 = subscription("https://fcm.googleapis.com/fcm/send/device1")
                .p256dh("p256dhKey1")
                .auth("authKey1")
                .build();
        pushSubscriptionRepository.save(sub1);

        PushSubscription sub2 = subscription("https://fcm.googleapis.com/fcm/send/device2")
                .p256dh("p256dhKey2")
                .auth("authKey2")
                .build();
        pushSubscriptionRepository.save(sub2);

        entityManager.flush();

        long count = pushSubscriptionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(1L))
                .count();
        assertEquals(2, count);
    }

    @Test
    void testDeactivateSubscription() {
        PushSubscription subscription = subscription("https://fcm.googleapis.com/fcm/send/deactivate").build();
        subscription = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        subscription.deactivate();
        PushSubscription updated = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        assertFalse(updated.isActive());
    }

    private PushSubscription.PushSubscriptionBuilder subscription(String endpoint) {
        return PushSubscription.builder()
                .userId(1L)
                .apartmentId(10L)
                .endpoint(endpoint)
                .p256dh("p256dhKey")
                .auth("authKey");
    }
}
