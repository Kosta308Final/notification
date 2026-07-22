package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.DesktopDevice;

import java.time.LocalDateTime;

public record DesktopDeviceRespDto(
        String deviceId,
        String deviceName,
        String platform,
        String appVersion,
        String notificationPermission,
        boolean active,
        LocalDateTime lastConnectedAt
) {
    public DesktopDeviceRespDto(DesktopDevice device) {
        this(device.getDeviceId(), device.getDeviceName(), device.getPlatform(), device.getAppVersion(),
                device.getNotificationPermission(), device.isActive(), device.getLastConnectedAt());
    }
}
