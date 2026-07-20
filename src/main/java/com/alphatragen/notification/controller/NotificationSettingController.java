package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.NotificationSettingRespDto;
import com.alphatragen.notification.dto.NotificationSettingUpdateReqDto;
import com.alphatragen.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.alphatragen.notification.security.JwtUserClaims;

@RestController
@RequestMapping({"/api/admin/notifications/settings", "/api/admin/notification-settings"})
public class NotificationSettingController {
    private final NotificationSettingService service;

    public NotificationSettingController(NotificationSettingService service) {
        this.service = service;
    }

    @GetMapping
    public NotificationSettingRespDto get(Authentication authentication) {
        return service.get(JwtUserClaims.from(authentication).apartmentId());
    }

    @PutMapping
    public NotificationSettingRespDto update(
            @Valid @RequestBody NotificationSettingUpdateReqDto request,
            Authentication authentication) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        return service.update(claims.apartmentId(), request.retentionDays(), claims.userId(), claims.apartmentId(), String.join(",", claims.roles()));
    }
}
