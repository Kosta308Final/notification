package com.alphatragen.notification.controller;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.dto.AdminNotificationPageRespDto;
import com.alphatragen.notification.dto.ManualNotificationReqDto;
import com.alphatragen.notification.dto.RecipientPreviewRespDto;
import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import com.alphatragen.notification.service.NotificationAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationAdminController {
    private final NotificationAdminService adminService;
    public NotificationAdminController(NotificationAdminService adminService) { this.adminService = adminService; }

    @GetMapping
    public AdminNotificationPageRespDto history(
            @CurrentUser JwtUserClaims claims,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return AdminNotificationPageRespDto.from(
                adminService.getHistory(claims.apartmentId(), String.join(",", claims.roles()), pageable)
        );
    }

    @PostMapping("/recipients/preview")
    public RecipientPreviewRespDto preview(
            @Valid @RequestBody ManualNotificationReqDto request,
            @CurrentUser JwtUserClaims claims) {
        return adminService.preview(request, claims.apartmentId(), String.join(",", claims.roles()));
    }

    @PostMapping
    public Notification send(
            @Valid @RequestBody ManualNotificationReqDto request,
            @CurrentUser JwtUserClaims claims) {
        return adminService.send(request, claims.userId(), claims.apartmentId(), String.join(",", claims.roles()));
    }
}
