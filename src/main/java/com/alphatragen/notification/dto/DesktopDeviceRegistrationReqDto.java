package com.alphatragen.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record DesktopDeviceRegistrationReqDto(
        @NotBlank(message = "deviceId is required") String deviceId,
        @NotBlank(message = "deviceName is required") String deviceName,
        @NotBlank(message = "platform is required") String platform,
        @NotBlank(message = "appVersion is required") String appVersion,
        @NotBlank(message = "notificationPermission is required") String notificationPermission
) {
}
