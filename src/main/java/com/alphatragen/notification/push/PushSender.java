package com.alphatragen.notification.push;

public interface PushSender {
    void sendPush(Long notificationId, Long recipientUserId, String title, String content, String actionUrl);

    default void sendPush(Long notificationId, Long recipientUserId, Long apartmentId,
                          String title, String content, String actionUrl) {
        sendPush(notificationId, recipientUserId, title, content, actionUrl);
    }
}
