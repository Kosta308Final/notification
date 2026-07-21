package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.NotificationRespDto;
import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import com.alphatragen.notification.service.NotificationUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationUserService userService;

    public NotificationController(NotificationUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<NotificationRespDto> getNotificationsAuthenticated(
            @CurrentUser JwtUserClaims claims,
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getNotifications(claims.userId(), claims.apartmentId(), isRead, pageable)
                .map(NotificationRespDto::new);
    }

    @GetMapping("/unread-count")
    public long getUnreadCountAuthenticated(@CurrentUser JwtUserClaims claims) {
        return userService.getUnreadCount(claims.userId(), claims.apartmentId());
    }

    @PatchMapping("/{id}/read")
    public void markAsReadAuthenticated(
            @PathVariable("id") Long id,
            @CurrentUser JwtUserClaims claims) {
        userService.markAsRead(id, claims.userId(), claims.apartmentId());
    }

    @PatchMapping("/read-all")
    public void markAllAsReadAuthenticated(@CurrentUser JwtUserClaims claims) {
        userService.markAllAsRead(claims.userId(), claims.apartmentId());
    }
}
