package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationSourceType;
import com.alphatragen.notification.domain.NotificationTargetType;

import java.time.LocalDateTime;

public class AdminNotificationResponseDto {
    private Long id;
    private String eventId;
    private String title;
    private String content;
    private NotificationImportance importance;
    private NotificationSourceType sourceType;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime retentionUntil;
    private NotificationTargetType targetType;
    private int recipientCount;

    public AdminNotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.eventId = notification.getEventId();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.importance = notification.getImportance();
        this.sourceType = notification.getSourceType();
        this.createdBy = notification.getCreatedBy();
        this.createdAt = notification.getCreatedAt();
        this.retentionUntil = notification.getRetentionUntil();
        this.targetType = notification.getTargets().isEmpty()
                ? null : notification.getTargets().get(0).getTargetType();
        this.recipientCount = notification.getRecipients().size();
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public NotificationImportance getImportance() { return importance; }
    public NotificationSourceType getSourceType() { return sourceType; }
    public Long getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRetentionUntil() { return retentionUntil; }
    public NotificationTargetType getTargetType() { return targetType; }
    public int getRecipientCount() { return recipientCount; }
}
