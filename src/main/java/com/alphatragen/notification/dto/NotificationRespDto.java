package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationRecipient;
import java.time.LocalDateTime;

public class NotificationRespDto {
    private Long id;
    private Long notificationId;
    private String eventId;
    private String title;
    private String content;
    private NotificationImportance importance;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;
    private boolean urgent;

    public NotificationRespDto() {
    }

    public NotificationRespDto(NotificationRecipient recipient) {
        this.id = recipient.getId();
        this.notificationId = recipient.getNotification().getId();
        this.eventId = recipient.getNotification().getEventId();
        this.title = recipient.getNotification().getTitle();
        this.content = recipient.getNotification().getContent();
        this.importance = recipient.getNotification().getImportance();
        this.isRead = recipient.isRead();
        this.createdAt = recipient.getNotification().getCreatedAt();
        this.actionUrl = recipient.getNotification().getActionUrl();
        this.urgent = recipient.getNotification().getImportance().name().equals("URGENT");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationImportance getImportance() {
        return importance;
    }

    public void setImportance(NotificationImportance importance) {
        this.importance = importance;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
}
