package com.alphatragen.notification.push;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@Profile("!test")
public class WebPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(WebPushSender.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final PushService pushService;
    private final WebPushPayloadGenerator payloadGenerator;
    private final WebPushResponseHandler responseHandler;

    public WebPushSender(PushSubscriptionRepository pushSubscriptionRepository,
                         NotificationRepository notificationRepository,
                         PushService pushService) {
        this(pushSubscriptionRepository, notificationRepository, pushService,
                new WebPushPayloadGenerator(), new WebPushResponseHandler(pushSubscriptionRepository));
    }

    @Autowired
    public WebPushSender(PushSubscriptionRepository pushSubscriptionRepository,
                         NotificationRepository notificationRepository,
                         PushService pushService,
                         WebPushPayloadGenerator payloadGenerator,
                         WebPushResponseHandler responseHandler) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.pushService = pushService;
        this.payloadGenerator = payloadGenerator;
        this.responseHandler = responseHandler;
    }

    @Override
    public void sendPush(Long notificationId, Long recipientUserId, String title, String content, String actionUrl) {
        List<PushSubscription> activeSubscriptions =
                pushSubscriptionRepository.findByUserIdAndIsActiveTrue(recipientUserId);
        sendToSubscriptions(notificationId, recipientUserId, title, content, actionUrl, activeSubscriptions);
    }

    @Override
    public void sendPush(Long notificationId, Long recipientUserId, Long apartmentId,
                         String title, String content, String actionUrl) {
        List<PushSubscription> activeSubscriptions =
                pushSubscriptionRepository.findByUserIdAndApartmentIdAndIsActiveTrue(recipientUserId, apartmentId);
        sendToSubscriptions(notificationId, recipientUserId, title, content, actionUrl, activeSubscriptions);
    }

    private void sendToSubscriptions(Long notificationId, Long recipientUserId, String title, String content,
                                     String actionUrl, List<PushSubscription> activeSubscriptions) {
        if (activeSubscriptions.isEmpty()) {
            log.info("No active push subscriptions found for user: {}", recipientUserId);
            return;
        }

        Notification notificationEntity = notificationRepository.findById(notificationId).orElse(null);
        String importance = notificationEntity == null ? "NORMAL" : notificationEntity.getImportance().name();
        String payload;
        try {
            payload = payloadGenerator.generate(notificationId, title, content, importance, actionUrl);
        } catch (Exception e) {
            log.error("Failed to create Web Push payload for notification: {}", notificationId, e);
            return;
        }

        for (PushSubscription sub : activeSubscriptions) {
            try {
                Subscription subscription = new Subscription(
                        sub.getEndpoint(), new Subscription.Keys(sub.getP256dh(), sub.getAuth()));
                nl.martijndwars.webpush.Notification webPushNotification =
                        new nl.martijndwars.webpush.Notification(subscription, payload);

                log.info("Sending Web Push payload subscriptionId={} recipientUserId={}",
                        sub.getId(), recipientUserId);
                HttpResponse response = pushService.send(webPushNotification);
                responseHandler.handle(sub, response);
            } catch (Exception e) {
                log.error("Exception occurred while sending Web Push to subscription: {}", sub.getId(), e);
                // 일시적인 전송 예외에서는 구독을 비활성화하지 않습니다.
            }
        }
    }
}
