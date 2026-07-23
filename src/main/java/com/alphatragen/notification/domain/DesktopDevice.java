package com.alphatragen.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "desktop_device")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DesktopDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(name = "device_id", nullable = false, unique = true, length = 100)
    private String deviceId;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform;

    @Column(name = "app_version", nullable = false, length = 50)
    private String appVersion;

    @Column(name = "notification_permission", nullable = false, length = 20)
    private String notificationPermission;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_connected_at", nullable = false)
    private LocalDateTime lastConnectedAt;

    @Builder
    private DesktopDevice(Long userId, Long apartmentId, String deviceId, String deviceName,
                          String platform, String appVersion, String notificationPermission,
                          Boolean active, LocalDateTime lastConnectedAt) {
        this.userId = userId;
        this.apartmentId = apartmentId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.appVersion = appVersion;
        this.notificationPermission = notificationPermission;
        this.active = active == null || active;
        this.lastConnectedAt = lastConnectedAt == null ? LocalDateTime.now() : lastConnectedAt;
    }

    public void update(Long apartmentId, String deviceName, String platform, String appVersion,
                       String notificationPermission, LocalDateTime connectedAt) {
        this.apartmentId = apartmentId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.appVersion = appVersion;
        this.notificationPermission = notificationPermission;
        this.active = true;
        this.lastConnectedAt = connectedAt;
    }

    public void heartbeat(LocalDateTime connectedAt) {
        this.active = true;
        this.lastConnectedAt = connectedAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
