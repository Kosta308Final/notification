package com.alphatragen.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateReqDto(
        @NotNull @Min(30) @Max(365) Integer retentionDays
) {
}
