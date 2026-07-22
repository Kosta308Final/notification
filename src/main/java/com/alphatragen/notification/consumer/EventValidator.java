package com.alphatragen.notification.consumer;

import com.alphatragen.notification.dto.NotificationEventDto;
import org.springframework.stereotype.Component;

@Component
public class EventValidator {

    public void validate(NotificationEventDto event) {
        if (event == null) {
            throw new IllegalArgumentException("notification event is required");
        }
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (event.getEventType() == null) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
        if (event.getApartmentId() == null) {
            throw new IllegalArgumentException("apartmentId is required");
        }
        if (event.getTargetType() == null) {
            throw new IllegalArgumentException("targetType is required");
        }
        if (event.getActionUrl() != null && !event.getActionUrl().matches("^/[a-zA-Z0-9_/#-]*$")) {
            throw new IllegalArgumentException("actionUrl must be a relative internal path");
        }

        switch (event.getTargetType()) {
            case INDIVIDUAL -> require(event.getUserId(), "userId is required for INDIVIDUAL target type");
            case HOUSEHOLD -> {
                requireText(event.getBuilding(), "building and unit are required for HOUSEHOLD target type");
                requireText(event.getUnit(), "building and unit are required for HOUSEHOLD target type");
            }
            case BUILDING -> requireText(event.getBuilding(), "building is required for BUILDING target type");
            case ROLE -> requireText(event.getRole(), "role is required for ROLE target type");
            case APARTMENT -> { }
        }
    }

    private void require(Long value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
}
