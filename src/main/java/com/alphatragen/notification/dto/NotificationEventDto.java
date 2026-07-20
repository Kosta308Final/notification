package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.Map;

public class NotificationEventDto {

    @NotBlank(message = "eventId is required")
    private String eventId;

    @NotNull(message = "eventType is required")
    private NotificationEventType eventType;

    @NotNull(message = "occurredAt is required")
    private LocalDateTime occurredAt;

    @NotNull(message = "apartmentId is required")
    private Long apartmentId;

    @NotNull(message = "targetType is required")
    private NotificationTargetType targetType;

    private Long userId;
    private String building;
    private String unit;
    private String role;
    private Map<String, String> templateData;

    @Pattern(regexp = "^/[a-zA-Z0-9_/-]*$", message = "actionUrl must be a relative internal path")
    private String actionUrl;

    public NotificationEventDto() {
    }

    public NotificationEventDto(String eventId, NotificationEventType eventType, LocalDateTime occurredAt, Long apartmentId, NotificationTargetType targetType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.apartmentId = apartmentId;
        this.targetType = targetType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public NotificationTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(NotificationTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, String> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(Map<String, String> templateData) {
        this.templateData = templateData;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public void validateTargetSpecificFields() {
        if (targetType == null) return;
        switch (targetType) {
            case INDIVIDUAL:
                if (userId == null) {
                    throw new IllegalArgumentException("userId is required for INDIVIDUAL target type");
                }
                break;
            case HOUSEHOLD:
                if (building == null || building.isBlank() || unit == null || unit.isBlank()) {
                    throw new IllegalArgumentException("building and unit are required for HOUSEHOLD target type");
                }
                break;
            case BUILDING:
                if (building == null || building.isBlank()) {
                    throw new IllegalArgumentException("building is required for BUILDING target type");
                }
                break;
            case ROLE:
                if (role == null || role.isBlank()) {
                    throw new IllegalArgumentException("role is required for ROLE target type");
                }
                break;
            case APARTMENT:
                // No extra fields required other than apartmentId (which is checked at @NotNull level)
                break;
        }
    }
}
