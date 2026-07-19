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
        pushSubscriptionRepository = mock(PushSubscriptionRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        pushService = mock(PushService.class);
        webPushSender = new WebPushSender(pushSubscriptionRepository, notificationRepository, pushService);
    }

    @Test
    void whenNoSubscriptionsFound_thenDoNothing() {
        when(pushSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .thenReturn(Collections.emptyList());

        webPushSender.sendPush(10L, 1L, "Title", "Content", "/test");

        verifyNoInteractions(pushService);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void whenActiveSubscriptionsExist_thenSendToEachAndSaveLastUsedOnSuccess() throws Exception {
        PushSubscription sub1 = new PushSubscription();
        sub1.setId(101L);
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");
        sub1.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub1.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub1.setActive(true);

        PushSubscription sub2 = new PushSubscription();
        sub2.setId(102L);
        sub2.setUserId(1L);
        sub2.setApartmentId(10L);
        sub2.setEndpoint("https://fcm.googleapis.com/fcm/send/some_token");
        sub2.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub2.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub2.setActive(true);

        when(pushSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .thenReturn(Arrays.asList(sub1, sub2));

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setImportance(NotificationImportance.URGENT);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(201);
        when(response.getStatusLine()).thenReturn(statusLine);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response);

        webPushSender.sendPush(10L, 1L, "Title", "Content", "/test");

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
        PushSubscription sub1 = new PushSubscription();
        sub1.setId(101L);
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");
        sub1.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub1.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub1.setActive(true);

        when(pushSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .thenReturn(Collections.singletonList(sub1));

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setImportance(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response410 = mock(HttpResponse.class);
        StatusLine statusLine410 = mock(StatusLine.class);
        when(statusLine410.getStatusCode()).thenReturn(410); // Gone
        when(response410.getStatusLine()).thenReturn(statusLine410);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response410);

        webPushSender.sendPush(10L, 1L, "Title", "Content", "/test");

        verify(pushSubscriptionRepository, times(1)).save(sub1);
        assertThat(sub1.isActive()).isFalse();
    }

    @Test
    void whenSubscriptionIsNotFound_thenDeactivateSubscription() throws Exception {
        PushSubscription sub1 = new PushSubscription();
        sub1.setId(101L);
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAA");
        sub1.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub1.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub1.setActive(true);

        when(pushSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .thenReturn(Collections.singletonList(sub1));

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setImportance(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response404 = mock(HttpResponse.class);
        StatusLine statusLine404 = mock(StatusLine.class);
        when(statusLine404.getStatusCode()).thenReturn(404); // Not Found
        when(response404.getStatusLine()).thenReturn(statusLine404);

        when(pushService.send(any(nl.martijndwars.webpush.Notification.class))).thenReturn(response404);

        webPushSender.sendPush(10L, 1L, "Title", "Content", "/test");

        verify(pushSubscriptionRepository, times(1)).save(sub1);
        assertThat(sub1.isActive()).isFalse();
    }

    @Test
    void whenOneSubscriptionFails_thenContinueSendingToOtherSubscriptions() throws Exception {
        PushSubscription sub1 = new PushSubscription();
        sub1.setId(101L);
        sub1.setUserId(1L);
        sub1.setApartmentId(10L);
        sub1.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/sub1");
        sub1.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub1.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub1.setActive(true);

        PushSubscription sub2 = new PushSubscription();
        sub2.setId(102L);
        sub2.setUserId(1L);
        sub2.setApartmentId(10L);
        sub2.setEndpoint("https://fcm.googleapis.com/fcm/send/sub2");
        sub2.setP256dh("BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA");
        sub2.setAuth("eXNhYmNkZWZnaGlqa2xtbg==");
        sub2.setActive(true);

        when(pushSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .thenReturn(Arrays.asList(sub1, sub2));

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setImportance(NotificationImportance.NORMAL);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        HttpResponse response201 = mock(HttpResponse.class);
        StatusLine statusLine201 = mock(StatusLine.class);
        when(statusLine201.getStatusCode()).thenReturn(201);
        when(response201.getStatusLine()).thenReturn(statusLine201);

        // First call throws exception (network issue, jose exception, etc.), second succeeds
        when(pushService.send(any(nl.martijndwars.webpush.Notification.class)))
                .thenThrow(new IOException("Network Timeout"))
                .thenReturn(response201);

        webPushSender.sendPush(10L, 1L, "Title", "Content", "/test");

        verify(pushService, times(2)).send(any(nl.martijndwars.webpush.Notification.class));
        
        // sub1 threw an exception, it shouldn't be saved or deactivated (transient failure)
        verify(pushSubscriptionRepository, never()).save(sub1);
        assertThat(sub1.isActive()).isTrue();

        // sub2 succeeded, it should be saved
        verify(pushSubscriptionRepository, times(1)).save(sub2);
        assertThat(sub2.isActive()).isTrue();
    }
}
