package com.alphatragen.notification.delivery;

import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.push.PushSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final PushSender pushSender;

    public NotificationDeliveryService(PushSender pushSender) {
        this.pushSender = pushSender;
    }

    public void deliver(NotificationCreatedEvent event) {
        int successCount = 0;
        int failureCount = 0;
        log.info("push_dispatch_started notificationId={} recipientCount={}",
                event.notificationId(), event.recipientUserIds().size());

        for (Long recipientUserId : event.recipientUserIds()) {
            try {
                pushSender.sendPush(
                        event.notificationId(),
                        recipientUserId,
                        event.title(),
                        event.content(),
                        event.actionUrl()
                );
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("push_dispatch_failed notificationId={} recipientUserId={}",
                        event.notificationId(), recipientUserId, e);
            }
        }

        log.info("push_dispatch_completed notificationId={} successCount={} failureCount={}",
                event.notificationId(), successCount, failureCount);
    }
}
