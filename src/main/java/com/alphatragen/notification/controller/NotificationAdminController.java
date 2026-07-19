package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.ManualNotificationRequestDto;
import com.alphatragen.notification.dto.RecipientPreviewResponseDto;
import com.alphatragen.notification.dto.AdminNotificationResponseDto;
import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.service.NotificationAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.alphatragen.notification.security.JwtUserClaims;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationAdminController {
    private final NotificationAdminService adminService;
    public NotificationAdminController(NotificationAdminService adminService) { this.adminService = adminService; }

    @GetMapping
    public Page<AdminNotificationResponseDto> history(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return adminService.getHistory(claims.apartmentId(), String.join(",", claims.roles()), pageable);
    }

    @PostMapping("/recipients/preview")
    public RecipientPreviewResponseDto preview(@Valid @RequestBody ManualNotificationRequestDto request, Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        return adminService.preview(request, claims.apartmentId(), String.join(",", claims.roles()));
    }

    @PostMapping
    public Notification send(@Valid @RequestBody ManualNotificationRequestDto request, Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        return adminService.send(request, claims.userId(), claims.apartmentId(), String.join(",", claims.roles()));
    }
}
