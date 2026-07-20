package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationSetting;
import java.time.LocalDateTime;

public record NotificationSettingRespDto(
        Long apartmentId,
        int retentionDays,
        Long updatedBy,
        LocalDateTime updatedAt
) {
    public static NotificationSettingRespDto from(NotificationSetting setting) {
        return new NotificationSettingRespDto(setting.getApartmentId(), setting.getRetentionDays(),
                setting.getUpdatedBy(), setting.getUpdatedAt());
    }
}
