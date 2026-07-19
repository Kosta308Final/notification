package com.alphatragen.notification.event;

import com.alphatragen.notification.push.PushSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationCreatedEventListener.class);
    private final PushSender pushSender;

    public NotificationCreatedEventListener(PushSender pushSender) {
        this.pushSender = pushSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        int successCount = 0;
        int failureCount = 0;
        log.info("push_dispatch_started notificationId={} recipientCount={}", event.notificationId(), event.recipientUserIds().size());
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
                log.error("push_dispatch_failed notificationId={} recipientUserId={}", event.notificationId(), recipientUserId, e);
                // Recorded separately, does not rollback the original notification creation transaction (since it's already committed)
            }
        }
        log.info("push_dispatch_completed notificationId={} successCount={} failureCount={}", event.notificationId(), successCount, failureCount);
    }
}
