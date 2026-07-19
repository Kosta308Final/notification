package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationSetting;
import java.time.LocalDateTime;

public record NotificationSettingResponseDto(
        Long apartmentId,
        int retentionDays,
        Long updatedBy,
        LocalDateTime updatedAt
) {
    public static NotificationSettingResponseDto from(NotificationSetting setting) {
        return new NotificationSettingResponseDto(setting.getApartmentId(), setting.getRetentionDays(),
                setting.getUpdatedBy(), setting.getUpdatedAt());
    }
}
