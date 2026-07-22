package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.PushSubscriptionReqDto;
import com.alphatragen.notification.dto.PushUnsubscribeReqDto;
import com.alphatragen.notification.service.PushSubscriptionService;
import com.alphatragen.notification.config.VapidConfig;
import com.alphatragen.notification.security.CurrentUser;
import com.alphatragen.notification.security.JwtUserClaims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/subscriptions")
public class PushSubscriptionController {

    private final PushSubscriptionService subscriptionService;
    private final VapidConfig vapidConfig;

    @Autowired
    public PushSubscriptionController(PushSubscriptionService subscriptionService, VapidConfig vapidConfig) {
        this.subscriptionService = subscriptionService;
        this.vapidConfig = vapidConfig;
    }

    /** 기존 단위 테스트와 서비스 내부 호출을 위한 생성자입니다. */
    public PushSubscriptionController(PushSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
        this.vapidConfig = null;
    }

    @GetMapping("/vapid-public-key")
    public Map<String, String> vapidPublicKey() {
        return Map.of("publicKey", vapidConfig.getPublicKey());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribeAuthenticated(
            @CurrentUser JwtUserClaims claims,
            @Valid @RequestBody PushSubscriptionReqDto requestDto) {
        subscriptionService.subscribe(
                claims.userId(), claims.apartmentId(),
                requestDto.endpoint(),
                requestDto.p256dh(),
                requestDto.auth(),
                requestDto.browser(),
                requestDto.deviceType()
        );
    }

    @PostMapping("/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateAuthenticated(
            @CurrentUser JwtUserClaims claims,
            @Valid @RequestBody PushUnsubscribeReqDto requestDto) {
        subscriptionService.unsubscribe(claims.userId(), requestDto.endpoint());
    }

    public void subscribe(Long userId, Long apartmentId, PushSubscriptionReqDto requestDto) {
        subscriptionService.subscribe(userId, apartmentId, requestDto.endpoint(), requestDto.p256dh(), requestDto.auth(), requestDto.browser(), requestDto.deviceType());
    }
    public void deactivate(Long userId, PushUnsubscribeReqDto requestDto) { subscriptionService.unsubscribe(userId, requestDto.endpoint()); }
}
