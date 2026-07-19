package com.alphatragen.notification.push;

public interface PushSender {
    void sendPush(Long notificationId, Long recipientUserId, String title, String content, String actionUrl);
}
