package com.alphatragen.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preference", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "apartment_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "apartment_id", nullable = false) private Long apartmentId;
    @Enumerated(EnumType.STRING) @Column(name = "pc_channel_mode", nullable = false, length = 30) private PcChannelMode pcChannelMode = PcChannelMode.DESKTOP_FIRST;
    @Column(name = "desktop_native_enabled", nullable = false) private boolean desktopNativeEnabled = true;
    @Column(name = "floating_enabled", nullable = false) private boolean floatingEnabled = true;
    @Column(name = "urgent_auto_expand", nullable = false) private boolean urgentAutoExpand = true;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    public UserNotificationPreference(Long userId, Long apartmentId) { this.userId = userId; this.apartmentId = apartmentId; }
    public void update(PcChannelMode mode, boolean nativeEnabled, boolean floatingEnabled, boolean urgentExpand) { this.pcChannelMode = mode; this.desktopNativeEnabled = nativeEnabled; this.floatingEnabled = floatingEnabled; this.urgentAutoExpand = urgentExpand; this.updatedAt = LocalDateTime.now(); }
}
