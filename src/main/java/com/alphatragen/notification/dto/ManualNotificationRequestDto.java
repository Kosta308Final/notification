package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ManualNotificationRequestDto {
    @NotNull private Long apartmentId;
    @NotNull private NotificationTargetType targetType;
    private Long userId;
    private String building;
    private String unit;
    private String role;
    @NotNull private NotificationImportance importance = NotificationImportance.NORMAL;
    private String title;
    private String content;
    @Pattern(regexp = "^/[a-zA-Z0-9_/-]*$", message = "actionUrl must be a relative internal path")
    private String actionUrl;
    private Integer retentionDays;

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }
    public NotificationTargetType getTargetType() { return targetType; }
    public void setTargetType(NotificationTargetType targetType) { this.targetType = targetType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public NotificationImportance getImportance() { return importance; }
    public void setImportance(NotificationImportance importance) { this.importance = importance; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }

    public void validateTargetSpecificFields() {
        if (targetType == null) throw new IllegalArgumentException("targetType is required");
        switch (targetType) {
            case INDIVIDUAL -> { if (userId == null) throw new IllegalArgumentException("userId is required for INDIVIDUAL target type"); }
            case HOUSEHOLD -> { if (blank(building) || blank(unit)) throw new IllegalArgumentException("building and unit are required for HOUSEHOLD target type"); }
            case BUILDING -> { if (blank(building)) throw new IllegalArgumentException("building is required for BUILDING target type"); }
            case ROLE -> { if (blank(role)) throw new IllegalArgumentException("role is required for ROLE target type"); }
            case APARTMENT -> { }
        }
        if (retentionDays != null && (retentionDays < 30 || retentionDays > 365)) {
            throw new IllegalArgumentException("retentionDays must be between 30 and 365 days");
        }
    }

    public void validateForSend() {
        validateTargetSpecificFields();
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is required");
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
