package com.alphatragen.notification.consumer;

import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.service.NotificationApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationApplicationService notificationApplicationService;
    private final com.alphatragen.notification.service.PushSubscriptionService pushSubscriptionService;

    public NotificationEventConsumer(
            NotificationApplicationService notificationApplicationService,
            com.alphatragen.notification.service.PushSubscriptionService pushSubscriptionService) {
        this.notificationApplicationService = notificationApplicationService;
        this.pushSubscriptionService = pushSubscriptionService;
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group"
    )
    public void consume(NotificationEventDto eventDto) {
        if (eventDto == null) {
            log.error("Received null notification event");
            return;
        }
        log.info("Received notification event message: eventId={}, eventType={}", eventDto.getEventId(), eventDto.getEventType());
        try {
            if (eventDto.getEventType() == NotificationEventType.USER_WITHDRAWAL) {
                eventDto.validateTargetSpecificFields();
                pushSubscriptionService.deactivateSubscriptionByWithdrawal(eventDto.getUserId());
            } else {
                notificationApplicationService.createNotification(eventDto);
            }
            log.info("Successfully processed notification event: eventId={}", eventDto.getEventId());
        } catch (IllegalArgumentException e) {
            // Fatal validation errors should be caught and logged, not retried
            log.error("Fatal validation error processing event: eventId={}, message={}", eventDto.getEventId(), e.getMessage());
        } catch (Exception e) {
            // Other exceptions (like database issues) should be rethrown to trigger retry policy
            log.error("Error processing event: eventId={}, message={}", eventDto.getEventId(), e.getMessage());
            throw e;
        }
    }
}
