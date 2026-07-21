package com.alphatragen.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자의 브라우저 푸시 알림 구독 정보를 저장하는 엔티티입니다.
 *
 * push_subscription 테이블에 사용자·아파트 식별자와 Web Push 인증 정보, 활성 상태를 저장하며,
 * endpoint를 unique로 두어 동일한 브라우저 구독이 중복 저장되지 않도록 합니다.
 */
@Entity
@Table(name = "push_subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(name = "endpoint", nullable = false, unique = true, length = 500)
    private String endpoint;

    @Column(name = "p256dh", nullable = false, length = 255)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 255)
    private String auth;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt = LocalDateTime.now();

    @Builder
    private PushSubscription(Long id, Long userId, Long apartmentId, String endpoint, String p256dh, String auth,
                             String browser, String deviceType, Boolean active, LocalDateTime lastUsedAt) {
        this.id = id;
        this.userId = userId;
        this.apartmentId = apartmentId;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.browser = browser;
        this.deviceType = deviceType;
        isActive = active == null || active;
        this.lastUsedAt = lastUsedAt == null ? LocalDateTime.now() : lastUsedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void updateSubscription(Long apartmentId, String p256dh, String auth, String browser, String deviceType,
                                   LocalDateTime lastUsedAt) {
        this.apartmentId = apartmentId;
        this.p256dh = p256dh;
        this.auth = auth;
        this.browser = browser;
        this.deviceType = deviceType;
        activate(lastUsedAt);
    }

    public void activate(LocalDateTime lastUsedAt) {
        isActive = true;
        this.lastUsedAt = lastUsedAt;
    }

    public void deactivate() {
        isActive = false;
    }
}
