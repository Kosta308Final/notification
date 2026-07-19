package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.NotificationResponseDto;
import com.alphatragen.notification.service.NotificationUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.alphatragen.notification.security.JwtUserClaims;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationUserService userService;

    public NotificationController(NotificationUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<NotificationResponseDto> getNotificationsAuthenticated(
            Authentication authentication,
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return userService.getNotifications(claims.userId(), claims.apartmentId(), isRead, pageable)
                .map(NotificationResponseDto::new);
    }

    @GetMapping("/unread-count")
    public long getUnreadCountAuthenticated(Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        return userService.getUnreadCount(claims.userId(), claims.apartmentId());
    }

    @PatchMapping("/{id}/read")
    public void markAsReadAuthenticated(
            @PathVariable("id") Long id,
            Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        userService.markAsRead(id, claims.userId(), claims.apartmentId());
    }

    @PatchMapping("/read-all")
    public void markAllAsReadAuthenticated(Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        userService.markAllAsRead(claims.userId(), claims.apartmentId());
    }

    // 기존 단위 테스트와 내부 호출 호환용. HTTP 엔드포인트에는 노출하지 않는다.
    public Page<NotificationResponseDto> getNotifications(Long userId, Long apartmentId, Boolean isRead, int page, int size) {
        return userService.getNotifications(userId, apartmentId, isRead, PageRequest.of(page, size)).map(NotificationResponseDto::new);
    }
    public long getUnreadCount(Long userId, Long apartmentId) { return userService.getUnreadCount(userId, apartmentId); }
    public void markAsRead(Long id, Long userId) { userService.markAsRead(id, userId); }
    public void markAllAsRead(Long userId, Long apartmentId) { userService.markAllAsRead(userId, apartmentId); }
}
