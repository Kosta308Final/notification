package com.alphatragen.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 아파트별 알림 보관 정책을 저장하는 엔티티입니다.
 *
 * notification_setting 테이블에 아파트별 보관 기간과 최종 수정 정보를 저장하며,
 * apartment_id를 unique로 두어 아파트마다 하나의 설정만 유지합니다.
 */
@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_id", nullable = false, unique = true)
    private Long apartmentId;

    @Column(name = "retention_days", nullable = false)
    private int retentionDays = 90;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder
    private NotificationSetting(Long id, Long apartmentId, Integer retentionDays, Long updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.retentionDays = retentionDays == null ? 90 : retentionDays;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public void updateRetention(int retentionDays, Long updatedBy, LocalDateTime updatedAt) {
        validateRetentionDays(retentionDays);
        this.retentionDays = retentionDays;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    private static void validateRetentionDays(int retentionDays) {
        if (retentionDays < 30 || retentionDays > 365) {
            throw new IllegalArgumentException("Retention days must be between 30 and 365 days");
        }
    }
}
