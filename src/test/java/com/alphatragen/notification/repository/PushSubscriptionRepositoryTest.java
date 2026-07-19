package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.PushSubscription;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PushSubscriptionRepositoryTest {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSavePushSubscriptionSuccess() {
        PushSubscription subscription = new PushSubscription();
        subscription.setUserId(1L);
        subscription.setApartmentId(10L);
        subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAAB");
        subscription.setP256dh("p256dhKey");
        subscription.setAuth("authKey");
        subscription.setBrowser("Firefox");
        subscription.setDeviceType("Desktop");

        PushSubscription saved = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("https://updates.push.services.mozilla.com/wpush/v2/gAAAAAB", saved.getEndpoint());
        assertTrue(saved.isActive());
    }

    @Test
    void testDuplicateEndpointThrowsException() {
        PushSubscription sub1 = new PushSubscription();
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://fcm.googleapis.com/fcm/send/duplicate");
        sub1.setP256dh("p256dhKey1");
        sub1.setAuth("authKey1");
        pushSubscriptionRepository.save(sub1);
        entityManager.flush();

        PushSubscription sub2 = new PushSubscription();
        sub2.setUserId(2L); // Different user
        sub2.setApartmentId(10L);
        sub2.setEndpoint("https://fcm.googleapis.com/fcm/send/duplicate"); // Same endpoint
        sub2.setP256dh("p256dhKey2");
        sub2.setAuth("authKey2");

        assertThrows(DataIntegrityViolationException.class, () -> {
            pushSubscriptionRepository.save(sub2);
            entityManager.flush();
        });
    }

    @Test
    void testSameUserMultipleEndpointsSuccess() {
        PushSubscription sub1 = new PushSubscription();
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://fcm.googleapis.com/fcm/send/device1");
        sub1.setP256dh("p256dhKey1");
        sub1.setAuth("authKey1");
        pushSubscriptionRepository.save(sub1);

        PushSubscription sub2 = new PushSubscription();
        sub2.setUserId(1L); // Same user
        sub2.setApartmentId(10L);
        sub2.setEndpoint("https://fcm.googleapis.com/fcm/send/device2"); // Different endpoint
        sub2.setP256dh("p256dhKey2");
        sub2.setAuth("authKey2");
        pushSubscriptionRepository.save(sub2);

        entityManager.flush();

        long count = pushSubscriptionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(1L))
                .count();
        assertEquals(2, count);
    }

    @Test
    void testDeactivateSubscription() {
        PushSubscription subscription = new PushSubscription();
        subscription.setUserId(1L);
        subscription.setApartmentId(10L);
        subscription.setEndpoint("https://fcm.googleapis.com/fcm/send/deactivate");
        subscription.setP256dh("p256dhKey");
        subscription.setAuth("authKey");
        subscription = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        subscription.setActive(false);
        PushSubscription updated = pushSubscriptionRepository.save(subscription);
        entityManager.flush();

        assertFalse(updated.isActive());
    }
}
