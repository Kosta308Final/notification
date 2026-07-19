package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationRecipient;
import java.time.LocalDateTime;

public class NotificationResponseDto {
    private Long id;
    private String title;
    private String content;
    private NotificationImportance importance;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;

    public NotificationResponseDto() {
    }

    public NotificationResponseDto(NotificationRecipient recipient) {
        this.id = recipient.getId();
        this.title = recipient.getNotification().getTitle();
        this.content = recipient.getNotification().getContent();
        this.importance = recipient.getNotification().getImportance();
        this.isRead = recipient.isRead();
        this.createdAt = recipient.getNotification().getCreatedAt();
        this.actionUrl = recipient.getNotification().getActionUrl();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
