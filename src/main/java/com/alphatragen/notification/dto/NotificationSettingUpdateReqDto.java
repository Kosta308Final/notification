package com.alphatragen.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.alphatragen.notification.domain.PcChannelMode;

public record NotificationSettingUpdateReqDto(
        @Min(30) @Max(365) Integer retentionDays,
        PcChannelMode pcChannelMode,
        Boolean desktopNativeEnabled,
        Boolean floatingEnabled,
        Boolean urgentAutoExpand
) {
    public NotificationSettingUpdateReqDto(Integer retentionDays) {
        this(retentionDays, null, null, null, null);
    }
}
