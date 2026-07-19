package com.alphatragen.notification.service;

import com.alphatragen.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExpiredNotificationCleanupService {
    private static final Logger log = LoggerFactory.getLogger(ExpiredNotificationCleanupService.class);
    private final NotificationRepository notificationRepository;

    public ExpiredNotificationCleanupService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "${app.retention.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public int deleteExpiredNotifications() {
        int deleted = notificationRepository.deleteByRetentionUntilBefore(LocalDateTime.now());
        log.info("Expired notification cleanup completed: deletedCount={}", deleted);
        return deleted;
    }
}
