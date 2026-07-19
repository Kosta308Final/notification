package com.alphatragen.notification.controller;

import com.alphatragen.notification.dto.PushSubscriptionRequestDto;
import com.alphatragen.notification.dto.PushUnsubscribeRequestDto;
import com.alphatragen.notification.service.PushSubscriptionService;
import com.alphatragen.notification.config.VapidConfig;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import com.alphatragen.notification.security.JwtUserClaims;
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
        this(subscriptionService, new VapidConfig());
    }

    @GetMapping("/vapid-public-key")
    public Map<String, String> vapidPublicKey() {
        return Map.of("publicKey", vapidConfig.getPublicKey());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribeAuthenticated(
            Authentication authentication,
            @Valid @RequestBody PushSubscriptionRequestDto requestDto) {
        JwtUserClaims claims = JwtUserClaims.from(authentication);
        subscriptionService.subscribe(
                claims.userId(), claims.apartmentId(),
                requestDto.getEndpoint(),
                requestDto.getP256dh(),
                requestDto.getAuth(),
                requestDto.getBrowser(),
                requestDto.getDeviceType()
        );
    }

    @PostMapping("/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateAuthenticated(
            Authentication authentication,
            @Valid @RequestBody PushUnsubscribeRequestDto requestDto) {
        subscriptionService.unsubscribe(JwtUserClaims.from(authentication).userId(), requestDto.getEndpoint());
    }

    public void subscribe(Long userId, Long apartmentId, PushSubscriptionRequestDto requestDto) {
        subscriptionService.subscribe(userId, apartmentId, requestDto.getEndpoint(), requestDto.getP256dh(), requestDto.getAuth(), requestDto.getBrowser(), requestDto.getDeviceType());
    }
    public void deactivate(Long userId, PushUnsubscribeRequestDto requestDto) { subscriptionService.unsubscribe(userId, requestDto.getEndpoint()); }
}
