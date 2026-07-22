package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class PushSubscriptionServiceTest {

    @Autowired
    private PushSubscriptionService pushSubscriptionService;

    @Autowired
    private PushSubscriptionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testSubscribeSuccess() {
        // When
        PushSubscription sub = pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );

        // Then
        assertNotNull(sub.getId());
        assertEquals(1L, sub.getUserId());
        assertEquals("https://fcm.googleapis.com/fcm/send/1", sub.getEndpoint());
        assertTrue(sub.isActive());
    }

    @Test
    void testSubscribeDuplicateEndpointSameUserUpdates() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );

        // When
        PushSubscription sub = pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "new_p256dhKey", "new_authKey", "Firefox", "Desktop"
        );

        // Then
        assertEquals("new_p256dhKey", sub.getP256dh());
        assertEquals("Firefox", sub.getBrowser());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testSubscribeReactivatesSubscription() {
        // Given
        PushSubscription sub = pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );
        pushSubscriptionService.unsubscribe(1L, "https://fcm.googleapis.com/fcm/send/1");
        assertFalse(repository.findById(sub.getId()).orElseThrow().isActive());

        // When
        PushSubscription reactivated = pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );

        // Then
        assertTrue(reactivated.isActive());
    }

    @Test
    void testSubscribeEndpointTakeoverPrevention() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );

        // When & Then
        // Try subscribing the same endpoint under user 2
        assertThrows(IllegalArgumentException.class, () -> {
            pushSubscriptionService.subscribe(
                    2L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey2", "authKey2", "Safari", "Desktop"
            );
        });
    }

    @Test
    void testSubscribeMissingKeysThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            pushSubscriptionService.subscribe(1L, 10L, "", "p256dh", "auth", "Chrome", "Mobile");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            pushSubscriptionService.subscribe(1L, 10L, "endpoint", "", "auth", "Chrome", "Mobile");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            pushSubscriptionService.subscribe(1L, 10L, "endpoint", "p256dh", null, "Chrome", "Mobile");
        });
    }

    @Test
    void testUnsubscribeOnlyDeactivatesTargetEndpoint() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/2", "p256dhKey2", "authKey2", "Chrome", "Desktop"
        );

        // When
        pushSubscriptionService.unsubscribe(1L, "https://fcm.googleapis.com/fcm/send/1");

        // Then
        assertFalse(repository.findByEndpoint("https://fcm.googleapis.com/fcm/send/1").orElseThrow().isActive());
        assertTrue(repository.findByEndpoint("https://fcm.googleapis.com/fcm/send/2").orElseThrow().isActive());
    }

    @Test
    void testUnsubscribeBelongingToAnotherUserThrows() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pushSubscriptionService.unsubscribe(2L, "https://fcm.googleapis.com/fcm/send/1");
        });
    }

    @Test
    void testDeactivateSubscriptionByWithdrawal() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/2", "p256dhKey2", "authKey2", "Chrome", "Desktop"
        );
        pushSubscriptionService.subscribe(
                2L, 10L, "https://fcm.googleapis.com/fcm/send/3", "p256dhKey3", "authKey3", "Chrome", "Mobile"
        );

        // When
        pushSubscriptionService.deactivateSubscriptionByWithdrawal(1L);

        // Then
        List<PushSubscription> user1Subs = repository.findByUserId(1L);
        assertFalse(user1Subs.get(0).isActive());
        assertFalse(user1Subs.get(1).isActive());

        List<PushSubscription> user2Subs = repository.findByUserId(2L);
        assertTrue(user2Subs.get(0).isActive());
    }

    @Test
    void testDeactivateSubscriptionByWithdrawalIdempotent() {
        // Given
        pushSubscriptionService.subscribe(
                1L, 10L, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey1", "authKey1", "Chrome", "Mobile"
        );
        pushSubscriptionService.unsubscribe(1L, "https://fcm.googleapis.com/fcm/send/1");

        // When & Then (should not fail if already inactive)
        assertDoesNotThrow(() -> {
            pushSubscriptionService.deactivateSubscriptionByWithdrawal(1L);
        });
    }
}
