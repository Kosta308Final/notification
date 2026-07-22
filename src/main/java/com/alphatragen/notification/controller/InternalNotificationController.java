package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.NotificationRespDto;
import com.alphatragen.notification.service.NotificationUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class InternalNotificationController {

    private final NotificationUserService userService;

    public InternalNotificationController(NotificationUserService userService) {
        this.userService = userService;
    }

    public Page<NotificationRespDto> getNotifications(Long userId, Long apartmentId, Boolean isRead, int page, int size) {
        return userService.getNotifications(userId, apartmentId, isRead, PageRequest.of(page, size))
                .map(NotificationRespDto::new);
    }

    public long getUnreadCount(Long userId, Long apartmentId) {
        return userService.getUnreadCount(userId, apartmentId);
    }

    public void markAsRead(Long id, Long userId) {
        userService.markAsRead(id, userId);
    }

    public void markAllAsRead(Long userId, Long apartmentId) {
        userService.markAllAsRead(userId, apartmentId);
    }
}
