package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.NotificationSettingRespDto;
import com.alphatragen.notification.dto.NotificationSettingUpdateReqDto;
import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import com.alphatragen.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/notifications/settings", "/api/admin/notifications/settings", "/api/admin/notification-settings"})
public class NotificationSettingController {
    private final NotificationSettingService service;

    public NotificationSettingController(NotificationSettingService service) {
        this.service = service;
    }

    @GetMapping
    public NotificationSettingRespDto get(@CurrentUser JwtUserClaims claims) {
        return service.getForUser(claims.userId(), claims.apartmentId());
    }

    @PutMapping
    public NotificationSettingRespDto update(
            @Valid @RequestBody NotificationSettingUpdateReqDto request,
            @CurrentUser JwtUserClaims claims) {
            return service.updateForUser(claims.userId(), claims.apartmentId(), request);
    }
}
