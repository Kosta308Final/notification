package com.alphatragen.notification.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsolePushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(ConsolePushSender.class);

    @Override
    public void sendPush(Long notificationId, Long recipientUserId, String title, String content, String actionUrl) {
        log.info("Sending Web Push to User: {} for Notification: {}, Title: {}", recipientUserId, notificationId, title);
    }

    @Override
    public void sendPush(Long notificationId, Long recipientUserId, Long apartmentId,
                         String title, String content, String actionUrl) {
        sendPush(notificationId, recipientUserId, title, content, actionUrl);
    }
}
