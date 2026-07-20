package com.alphatragen.notification.event;

import com.alphatragen.notification.delivery.NotificationDeliveryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationCreatedEventListener {

    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationCreatedEventListener(NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        notificationDeliveryService.deliver(event);
    }
}
