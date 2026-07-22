package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.DesktopDeviceRegistrationReqDto;
import com.alphatragen.notification.dto.DesktopDeviceRespDto;
import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import com.alphatragen.notification.service.DesktopDeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/devices")
public class DesktopDeviceController {

    private final DesktopDeviceService deviceService;

    public DesktopDeviceController(DesktopDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/desktop")
    @ResponseStatus(HttpStatus.CREATED)
    public DesktopDeviceRespDto register(
            @CurrentUser JwtUserClaims claims,
            @Valid @RequestBody DesktopDeviceRegistrationReqDto request) {
        return new DesktopDeviceRespDto(deviceService.register(claims.userId(), claims.apartmentId(), request));
    }

    @PatchMapping("/desktop/{deviceId}")
    public DesktopDeviceRespDto update(
            @PathVariable String deviceId,
            @CurrentUser JwtUserClaims claims,
            @Valid @RequestBody DesktopDeviceRegistrationReqDto request) {
        return new DesktopDeviceRespDto(deviceService.update(claims.userId(), claims.apartmentId(), deviceId, request));
    }

    @DeleteMapping("/desktop/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String deviceId, @CurrentUser JwtUserClaims claims) {
        deviceService.deactivate(claims.userId(), deviceId);
    }

    @PostMapping("/desktop/{deviceId}/heartbeat")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void heartbeat(@PathVariable String deviceId, @CurrentUser JwtUserClaims claims) {
        deviceService.heartbeat(claims.userId(), deviceId);
    }
}
