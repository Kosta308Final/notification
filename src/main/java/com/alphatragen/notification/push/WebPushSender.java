package com.alphatragen.notification.push;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@Profile("!test")
public class WebPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(WebPushSender.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final PushService pushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebPushSender(PushSubscriptionRepository pushSubscriptionRepository,
                         NotificationRepository notificationRepository,
                         PushService pushService) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.pushService = pushService;
    }

    @Override
    public void sendPush(Long notificationId, Long recipientUserId, String title, String content, String actionUrl) {
        List<PushSubscription> activeSubscriptions = pushSubscriptionRepository.findByUserIdAndIsActiveTrue(recipientUserId);
        if (activeSubscriptions.isEmpty()) {
            log.info("No active push subscriptions found for user: {}", recipientUserId);
            return;
        }

        Notification notificationEntity = notificationRepository.findById(notificationId).orElse(null);
        String importance = (notificationEntity != null) ? notificationEntity.getImportance().name() : "NORMAL";

        for (PushSubscription sub : activeSubscriptions) {
            try {
                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                );

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("notificationId", notificationId);
                payloadMap.put("title", title);
                payloadMap.put("body", content);
                payloadMap.put("importance", importance);
                payloadMap.put("actionUrl", actionUrl);

                String payload = objectMapper.writeValueAsString(payloadMap);

                nl.martijndwars.webpush.Notification webPushNotification =
                        new nl.martijndwars.webpush.Notification(subscription, payload);

            log.info("Sending Web Push payload subscriptionId={} recipientUserId={}", sub.getId(), recipientUserId);
                HttpResponse response = pushService.send(webPushNotification);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 201) {
                    log.info("Successfully sent Web Push to subscription: {}", sub.getId());
                    sub.setLastUsedAt(LocalDateTime.now());
                    pushSubscriptionRepository.save(sub);
                } else if (statusCode == 410 || statusCode == 404) {
                    log.warn("Subscription has expired or is no longer valid. Deactivating subscription: {}. Status: {}", sub.getId(), statusCode);
                    sub.setActive(false);
                    pushSubscriptionRepository.save(sub);
                } else {
                    log.error("Failed to send Web Push to subscription: {}. Response status code: {}", sub.getId(), statusCode);
                }
            } catch (Exception e) {
                log.error("Exception occurred while sending Web Push to subscription: {}", sub.getId(), e);
                // Do NOT deactivate subscription on transient/other network exceptions.
            }
        }
    }
}
