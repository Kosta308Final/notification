package com.alphatragen.notification.controller;

import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import com.alphatragen.notification.realtime.RealtimeConnectionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {
    private final RealtimeConnectionService realtimeConnectionService;

    public NotificationStreamController(RealtimeConnectionService realtimeConnectionService) {
        this.realtimeConnectionService = realtimeConnectionService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@CurrentUser JwtUserClaims claims, @RequestParam String deviceId) {
        return realtimeConnectionService.connect(claims.userId(), claims.apartmentId(), deviceId);
    }
}
