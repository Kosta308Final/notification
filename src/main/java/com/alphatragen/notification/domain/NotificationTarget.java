package com.alphatragen.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림이 전달될 대상 조건을 저장하는 엔티티입니다.
 *
 * notification_target 테이블에 아파트, 사용자, 동·호수, 역할 등의 대상 조건을 저장하고,
 * notification_id 외래 키로 하나의 Notification에 연결됩니다.
 */
@Entity
@Table(name = "notification_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonIgnore
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private NotificationTargetType targetType;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "role", length = 50)
    private String role;

    @Builder
    private NotificationTarget(Long id, Notification notification, NotificationTargetType targetType, Long apartmentId,
                               Long userId, String building, String unit, String role) {
        this.id = id;
        this.notification = notification;
        this.targetType = targetType;
        this.apartmentId = apartmentId;
        this.userId = userId;
        this.building = building;
        this.unit = unit;
        this.role = role;
    }

    void assignNotification(Notification notification) {
        this.notification = notification;
    }
}
