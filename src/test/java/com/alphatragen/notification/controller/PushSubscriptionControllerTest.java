package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.PushSubscriptionRequestDto;
import com.alphatragen.notification.dto.PushUnsubscribeRequestDto;
import com.alphatragen.notification.service.PushSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PushSubscriptionControllerTest {

    private PushSubscriptionService subscriptionService;
    private PushSubscriptionController controller;

    @BeforeEach
    void setUp() {
        subscriptionService = mock(PushSubscriptionService.class);
        controller = new PushSubscriptionController(subscriptionService);
    }

    @Test
    void testSubscribe() {
        // Given
        Long userId = 1L;
        Long apartmentId = 10L;
        PushSubscriptionRequestDto requestDto = new PushSubscriptionRequestDto(
                "https://fcm.googleapis.com/fcm/send/1", "p256dhKey", "authKey", "Chrome", "Mobile"
        );

        // When
        controller.subscribe(userId, apartmentId, requestDto);

        // Then
        verify(subscriptionService, times(1)).subscribe(
                userId, apartmentId, "https://fcm.googleapis.com/fcm/send/1", "p256dhKey", "authKey", "Chrome", "Mobile"
        );
    }

    @Test
    void testDeactivate() {
        // Given
        Long userId = 1L;
        PushUnsubscribeRequestDto requestDto = new PushUnsubscribeRequestDto("https://fcm.googleapis.com/fcm/send/1");

        // When
        controller.deactivate(userId, requestDto);

        // Then
        verify(subscriptionService, times(1)).unsubscribe(userId, "https://fcm.googleapis.com/fcm/send/1");
    }
}
