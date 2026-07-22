package com.alphatragen.notification.consumer;

import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class EventDeserializer {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private final ObjectMapper objectMapper;

    public EventDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NotificationEventDto deserialize(String message) throws Exception {
        return deserialize(objectMapper.readTree(message));
    }

    NotificationEventDto deserialize(JsonNode event) {
        if (event == null || event.isNull()) {
            throw new IllegalArgumentException("notification event is required");
        }

        NotificationEventType eventType = toEventType(text(event, "eventType"));
        NotificationEventDto dto = new NotificationEventDto(
                text(event, "eventId"),
                eventType,
                toOccurredAt(event.get("occurredAt")),
                nullableLong(event, "apartmentId"),
                null
        );

        JsonNode recipient = event.get("recipient");
        JsonNode target = recipient != null && recipient.isObject() ? recipient : event;
        String targetType = text(target, "type");
        if (target == event) {
            targetType = text(event, "targetType");
        }
        dto.setTargetType(toTargetType(targetType));
        dto.setUserId(nullableLong(target, "userId"));
        dto.setBuilding(text(target, "building"));
        dto.setUnit(text(target, "unit"));
        dto.setRole(text(target, "role"));
        dto.setTemplateData(toTemplateData(event.get("templateData")));
        dto.setActionUrl(text(event, "actionUrl"));

        if (eventType == NotificationEventType.NOTICE_CREATED) {
            dto.getTemplateData().putIfAbsent("noticeContent", dto.getTemplateData().get("content"));
        }
        return dto;
    }

    private NotificationEventType toEventType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        return switch (value.toUpperCase()) {
//            case "VOTE_END_IMMINENT" -> NotificationEventType.VOTE_END_IMMINENT;
//            case "NOTICE_CREATED" -> NotificationEventType.NOTICE_CREATED;
//            case "USER_WITHDRAWN" -> NotificationEventType.USER_WITHDRAWN;
            default -> NotificationEventType.fromValue(value);
        };
    }

    private NotificationTargetType toTargetType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("recipient or targetType is required");
        }
        try {
            return NotificationTargetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown target type: " + value, e);
        }
    }

    private LocalDateTime toOccurredAt(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return fromEpochTimestamp(value.decimalValue());
        }
        if (value.isArray()) {
            return LocalDateTime.of(
                    value.get(0).asInt(),
                    value.get(1).asInt(),
                    value.get(2).asInt(),
                    value.get(3).asInt(),
                    value.get(4).asInt(),
                    value.size() > 5 ? value.get(5).asInt() : 0,
                    value.size() > 6 ? value.get(6).asInt() : 0
            );
        }
        String text = value.asText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(text);
        }
    }

    private LocalDateTime fromEpochTimestamp(BigDecimal timestamp) {
        // 1e11 미만은 초, 그 이상은 밀리초 단위로 처리한다.
        BigDecimal secondsValue = timestamp.abs().compareTo(new BigDecimal("100000000000")) >= 0
                ? timestamp.movePointLeft(3)
                : timestamp;
        long seconds = secondsValue.longValue();
        int nanos = secondsValue.subtract(BigDecimal.valueOf(seconds))
                .movePointRight(9)
                .intValue();
        return Instant.ofEpochSecond(seconds, nanos)
                .atZone(DEFAULT_ZONE)
                .toLocalDateTime();
    }

    private Map<String, String> toTemplateData(JsonNode node) {
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

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long nullableLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }
}
