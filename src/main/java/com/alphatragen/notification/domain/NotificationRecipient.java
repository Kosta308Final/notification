package com.alphatragen.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 특정 사용자에게 배정된 알림과 그 처리 상태(수신·읽음)를 저장하는 엔티티입니다.
 *
 * notification_recipient 테이블에 수신 사용자, 읽음 여부, 읽은 시각과 푸시 발송 시각을 저장하고,
 * notification_id 외래 키로 원본 Notification에 연결됩니다.
 */
@Entity
@Table(name = "notification_recipient", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"notification_id", "recipient_user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonIgnore
    private Notification notification;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "push_sent_at")
    private LocalDateTime pushSentAt;

    @Builder
    private NotificationRecipient(Long id, Notification notification, Long recipientUserId, boolean read,
                                  LocalDateTime readAt, LocalDateTime pushSentAt) {
        this.id = id;
        this.notification = notification;
        this.recipientUserId = recipientUserId;
        isRead = read;
        this.readAt = readAt;
        this.pushSentAt = pushSentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    void assignNotification(Notification notification) {
        this.notification = notification;
    }

    public void markAsRead(LocalDateTime readAt) {
        this.isRead = true;
        this.readAt = readAt;
    }

    public void markPushSent(LocalDateTime pushSentAt) {
        this.pushSentAt = pushSentAt;
    }
}
