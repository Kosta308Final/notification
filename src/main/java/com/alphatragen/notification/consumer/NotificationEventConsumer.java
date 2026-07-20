package com.alphatragen.notification.consumer;

import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.service.NotificationApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationApplicationService notificationApplicationService;
    private final com.alphatragen.notification.service.PushSubscriptionService pushSubscriptionService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(
            NotificationApplicationService notificationApplicationService,
            com.alphatragen.notification.service.PushSubscriptionService pushSubscriptionService,
            ObjectMapper objectMapper) {
        this.notificationApplicationService = notificationApplicationService;
        this.pushSubscriptionService = pushSubscriptionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group"
    )
    public void consume(String message) {
        NotificationEventDto eventDto;
        try {
            eventDto = toDto(objectMapper.readTree(message));
        } catch (Exception e) {
            log.error("Fatal validation error parsing notification event: {}", e.getMessage());
            return;
        }

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

    private NotificationEventDto toDto(JsonNode event) {
        if (event == null || event.isNull()) {
            throw new IllegalArgumentException("notification event is required");
        }

        String eventId = text(event, "eventId");
        String eventTypeValue = text(event, "eventType");
        Long apartmentId = requiredLong(event, "apartmentId");
        JsonNode occurredAt = event.get("occurredAt");
        if (occurredAt == null || occurredAt.isNull()) {
            throw new IllegalArgumentException("occurredAt is required");
        }

        NotificationEventType eventType = mapEventType(eventTypeValue);
        NotificationEventDto dto = new NotificationEventDto(
                eventId,
                eventType,
                parseOccurredAt(occurredAt.asText()),
                apartmentId,
                null
        );

        JsonNode recipient = event.get("recipient");
        if (recipient != null && recipient.isObject()) {
            String targetType = text(recipient, "type");
            dto.setTargetType(com.alphatragen.notification.domain.NotificationTargetType.valueOf(targetType));
            dto.setUserId(nullableLong(recipient, "userId"));
            dto.setBuilding(text(recipient, "building"));
            dto.setUnit(text(recipient, "unit"));
            dto.setRole(text(recipient, "role"));
        } else {
            String targetType = text(event, "targetType");
            if (targetType == null) {
                throw new IllegalArgumentException("recipient or targetType is required");
            }
            dto.setTargetType(com.alphatragen.notification.domain.NotificationTargetType.valueOf(targetType));
            dto.setUserId(nullableLong(event, "userId"));
            dto.setBuilding(text(event, "building"));
            dto.setUnit(text(event, "unit"));
            dto.setRole(text(event, "role"));
        }

        Map<String, String> templateData = readTemplateData(event.get("templateData"));
        if (eventType == NotificationEventType.URGENT_NOTICE) {
            templateData.putIfAbsent("noticeContent", templateData.get("content"));
        }
        dto.setTemplateData(templateData);
        dto.setActionUrl(text(event, "actionUrl"));
        dto.validateTargetSpecificFields();
        return dto;
    }

    private NotificationEventType mapEventType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        return switch (value.toUpperCase()) {
            case "COMPLAINT_ANSWERED" -> NotificationEventType.COMPLAINT_ANSWER_REGISTERED;
            case "VOTE_CLOSING_SOON" -> NotificationEventType.VOTE_END_IMMINENT;
            case "NOTICE_CREATED" -> NotificationEventType.URGENT_NOTICE;
            case "USER_WITHDRAWN" -> NotificationEventType.USER_WITHDRAWAL;
            default -> NotificationEventType.fromValue(value);
        };
    }

    private Map<String, String> readTemplateData(JsonNode node) {
        Map<String, String> result = new HashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                result.put(field.getKey(), field.getValue().isNull() ? null : field.getValue().asText());
            }
        }
        return result;
    }

    private LocalDateTime parseOccurredAt(String value) {
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(value);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long requiredLong(JsonNode node, String field) {
        Long value = nullableLong(node, field);
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private Long nullableLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }
}
