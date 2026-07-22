package com.alphatragen.notification.delivery;

import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.push.PushSender;
import com.alphatragen.notification.realtime.RealtimeConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final NotificationChannelRoutingService routingService;

    public NotificationDeliveryService(NotificationChannelRoutingService routingService) {
        this.routingService = routingService;
    }

    public void deliver(NotificationCreatedEvent event) {
        routingService.deliver(event);
    }
}
