package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ManualNotificationReqDto(
        @NotNull Long apartmentId,
        @NotNull NotificationTargetType targetType,
        Long userId,
        String building,
        String unit,
        String role,
        @NotNull NotificationImportance importance,
        String title,
        String content,
        @Pattern(regexp = "^/[a-zA-Z0-9_/-]*$", message = "actionUrl must be a relative internal path") String actionUrl,
        Integer retentionDays
) {
    public ManualNotificationReqDto {
        if (importance == null) importance = NotificationImportance.NORMAL;
    }

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
