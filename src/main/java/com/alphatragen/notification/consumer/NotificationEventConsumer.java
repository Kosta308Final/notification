package com.alphatragen.notification.consumer;

import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.application.NotificationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationEventHandler notificationEventHandler;
    private final EventDeserializer eventDeserializer;
    private final EventValidator eventValidator;

    public NotificationEventConsumer(
            NotificationEventHandler notificationEventHandler,
            EventDeserializer eventDeserializer,
            EventValidator eventValidator) {
        this.notificationEventHandler = notificationEventHandler;
        this.eventDeserializer = eventDeserializer;
        this.eventValidator = eventValidator;
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group"
    )
    public void consume(String message) {
        NotificationEventDto eventDto;
        try {
            eventDto = eventDeserializer.deserialize(message);
            eventValidator.validate(eventDto);
        } catch (Exception e) {
            log.error("Fatal validation error parsing notification event: {}", e.getMessage());
            return;
        }

        log.info("Received notification event message: eventId={}, eventType={}", eventDto.getEventId(), eventDto.getEventType());
        try {
            notificationEventHandler.handle(eventDto);
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
