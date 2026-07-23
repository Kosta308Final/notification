package com.alphatragen.notification.push;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebPushSenderTest {

    private PushSubscriptionRepository pushSubscriptionRepository;
    private NotificationRepository notificationRepository;
    private PushService pushService;
    private ObjectMapper objectMapper;
    private WebPushSender webPushSender;

    @BeforeEach
    void setUp() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        pushSubscriptionRepository = mock(PushSubscriptionRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        pushService = mock(PushService.class);
        webPushSender = new WebPushSender(pushSubscriptionRepository, notificationRepository, pushService);
    }

    @Test
    void whenNoSubscriptionsFound_thenDoNothing() {
        when(pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(1L, 10L))
                .thenReturn(Collections.emptyList());

        webPushSender.sendPush(10L, 1L, 10L, "Title", "Content", "/test");

        verifyNoInteractions(pushService);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void whenActiveSubscriptionsExist_thenSendToEachAndSaveLastUsedOnSuccess() throws Exception {
        PushSubscription sub1 = subscription(101L, "https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");
        PushSubscription sub2 = subscription(102L, "https://fcm.googleapis.com/fcm/send/some_token");

        when(pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(1L, 10L))
                .thenReturn(Arrays.asList(sub1, sub2));

        Notification notification = notification(NotificationImportance.URGENT);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(201);
        when(response.getStatusLine()).thenReturn(statusLine);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response);

        webPushSender.sendPush(10L, 1L, 10L, "Title", "Content", "/test");

        ArgumentCaptor<nl.martijndwars.webpush.Notification> notificationCaptor =
                ArgumentCaptor.forClass(nl.martijndwars.webpush.Notification.class);
        verify(pushService, times(2)).send(notificationCaptor.capture());

        List<nl.martijndwars.webpush.Notification> sentNotifications = notificationCaptor.getAllValues();
        assertThat(sentNotifications).hasSize(2);
        assertThat(sentNotifications.get(0).getEndpoint()).isEqualTo(sub1.getEndpoint());
        assertThat(sentNotifications.get(1).getEndpoint()).isEqualTo(sub2.getEndpoint());

        verify(pushSubscriptionRepository, times(1)).save(sub1);
        verify(pushSubscriptionRepository, times(1)).save(sub2);
        assertThat(sub1.isActive()).isTrue();
        assertThat(sub2.isActive()).isTrue();
    }

    @Test
    void whenSubscriptionIsExpired_thenDeactivateSubscription() throws Exception {
        PushSubscription sub1 = subscription(101L, "https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");

        when(pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(1L, 10L))
                .thenReturn(Collections.singletonList(sub1));

        Notification notification = notification(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response410 = mock(HttpResponse.class);
        StatusLine statusLine410 = mock(StatusLine.class);
        when(statusLine410.getStatusCode()).thenReturn(410); // Gone
        when(response410.getStatusLine()).thenReturn(statusLine410);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response410);

        webPushSender.sendPush(10L, 1L, 10L, "Title", "Content", "/test");

        verify(pushSubscriptionRepository, times(1)).save(sub1);
        assertThat(sub1.isActive()).isFalse();
    }

    @Test
    void whenSubscriptionIsNotFound_thenDeactivateSubscription() throws Exception {
        PushSubscription sub1 = subscription(101L, "https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");

        when(pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(1L, 10L))
                .thenReturn(Collections.singletonList(sub1));

        Notification notification = notification(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response404 = mock(HttpResponse.class);
        StatusLine statusLine404 = mock(StatusLine.class);
        when(statusLine404.getStatusCode()).thenReturn(404); // Not Found
        when(response404.getStatusLine()).thenReturn(statusLine404);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response404);

        webPushSender.sendPush(10L, 1L, 10L, "Title", "Content", "/test");

        verify(pushSubscriptionRepository, times(1)).save(sub1);
        assertThat(sub1.isActive()).isFalse();
    }

    @Test
    void whenOneSubscriptionFails_thenContinueSendingToOtherSubscriptions() throws Exception {
        PushSubscription sub1 = subscription(101L, "https://updates.push.services.mozilla.com/wpush/v2/sub1");
        PushSubscription sub2 = subscription(102L, "https://fcm.googleapis.com/fcm/send/sub2");

        when(pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(1L, 10L))
                .thenReturn(Arrays.asList(sub1, sub2));

        Notification notification = notification(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response201 = mock(HttpResponse.class);
        StatusLine statusLine201 = mock(StatusLine.class);
        when(statusLine201.getStatusCode()).thenReturn(201);
        when(response201.getStatusLine()).thenReturn(statusLine201);

        // First call throws exception (network issue, jose exception, etc.), second succeeds
        when(pushService.send(any(nl.martijndwars.webpush.Notification.class)))
                .thenThrow(new IOException("Network Timeout"))
                .thenReturn(response201);

        webPushSender.sendPush(10L, 1L, 10L, "Title", "Content", "/test");

        verify(pushService, times(2)).send(any(nl.martijndwars.webpush.Notification.class));
        
        // sub1 threw an exception, it shouldn't be saved or deactivated (transient failure)
        verify(pushSubscriptionRepository, never()).save(sub1);
        assertThat(sub1.isActive()).isTrue();

        // sub2 succeeded, it should be saved
        verify(pushSubscriptionRepository, times(1)).save(sub2);
        assertThat(sub2.isActive()).isTrue();
    }

    private PushSubscription subscription(Long id, String endpoint) {
        return PushSubscription.builder()
                .id(id)
                .userId(1L)
                .apartmentId(10L)
                .endpoint(endpoint)
                .p256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA")
                .auth("eXNhYmNkZWZnaGlqa2xtbg==")
                .active(true)
                .build();
    }

    private Notification notification(NotificationImportance importance) {
        return Notification.builder()
                .id(10L)
                .importance(importance)
                .build();
    }
}
