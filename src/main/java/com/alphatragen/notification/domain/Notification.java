package com.alphatragen.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 알림 본문을 저장하는 엔티티입니다.
 *
 * notification 테이블에 알림 제목, 내용, 발생 출처, 중요도와 보관 만료일을 저장하며,
 * NotificationTarget 및 NotificationRecipient와 1:N 관계로 연결됩니다.
 */
@Entity
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    private NotificationImportance importance = NotificationImportance.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private NotificationSourceType sourceType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "retention_until", nullable = false)
    private LocalDateTime retentionUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationTarget> targets = new ArrayList<>();

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationRecipient> recipients = new ArrayList<>();

    @Builder
    private Notification(Long id, String eventId, NotificationImportance importance, NotificationSourceType sourceType,
                         String title, String content, String actionUrl, Long createdBy,
                         LocalDateTime retentionUntil, LocalDateTime createdAt,
                         @Singular List<NotificationTarget> targets,
                         @Singular List<NotificationRecipient> recipients) {
        this.id = id;
        this.eventId = eventId;
        this.importance = importance == null ? NotificationImportance.NORMAL : importance;
        this.sourceType = sourceType;
        this.title = title;
        this.content = content;
        this.actionUrl = actionUrl;
        this.createdBy = createdBy;
        this.retentionUntil = retentionUntil;
        this.createdAt = createdAt;
        if (targets != null) {
            targets.forEach(this::addTarget);
        }
        if (recipients != null) {
            recipients.forEach(this::addRecipient);
        }
    }

    public List<NotificationTarget> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public void addTarget(NotificationTarget target) {
        if (target == null) {
            return;
        }
        target.assignNotification(this);
        this.targets.add(target);
    }

    public List<NotificationRecipient> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    public void addRecipient(NotificationRecipient recipient) {
        if (recipient == null) {
            return;
        }
        recipient.assignNotification(this);
        this.recipients.add(recipient);
    }
}
