package com.alphatragen.notification.event;

import java.util.List;

public record NotificationCreatedEvent(
        Long notificationId,
        List<Long> recipientUserIds,
        String title,
        String content,
        String actionUrl
) {
}
