package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.domain.PcChannelMode;
import java.time.LocalDateTime;

public record NotificationSettingRespDto(
        Long apartmentId,
        int retentionDays,
        Long updatedBy,
        LocalDateTime updatedAt
        , PcChannelMode pcChannelMode, boolean desktopNativeEnabled, boolean floatingEnabled, boolean urgentAutoExpand
) {
    public NotificationSettingRespDto(Long apartmentId, int retentionDays, Long updatedBy, LocalDateTime updatedAt) {
        this(apartmentId, retentionDays, updatedBy, updatedAt, com.alphatragen.notification.domain.PcChannelMode.DESKTOP_FIRST, true, true, true);
    }
    public static NotificationSettingRespDto from(NotificationSetting setting) {
        return new NotificationSettingRespDto(setting.getApartmentId(), setting.getRetentionDays(),
                setting.getUpdatedBy(), setting.getUpdatedAt(), setting.getPcChannelMode(), setting.isDesktopNativeEnabled(), setting.isFloatingEnabled(), setting.isUrgentAutoExpand());
    }
}
