package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PushSubscriptionService {

    private final PushSubscriptionRepository repository;

    public PushSubscriptionService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    public PushSubscription subscribe(Long userId, Long apartmentId, String endpoint, String p256dh, String auth, String browser, String deviceType) {
        if (endpoint == null || endpoint.isBlank() || p256dh == null || p256dh.isBlank() || auth == null || auth.isBlank()) {
            throw new IllegalArgumentException("Keys (endpoint, p256dh, auth) are required");
        }

        Optional<PushSubscription> existingOpt = repository.findByEndpoint(endpoint);
        if (existingOpt.isPresent()) {
            PushSubscription existing = existingOpt.get();
            
            // 다른 사용자의 endpoint 탈취를 방지하는 정책이 적용된다.
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Endpoint is already registered by another user");
            }
            
            existing.updateSubscription(apartmentId, p256dh, auth, browser, deviceType, LocalDateTime.now());
            return repository.save(existing);
        }

        PushSubscription subscription = PushSubscription.builder()
                .userId(userId)
                .apartmentId(apartmentId)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .browser(browser)
                .deviceType(deviceType)
                .active(true)
                .lastUsedAt(LocalDateTime.now())
                .build();
        return repository.save(subscription);
    }

    public void unsubscribe(Long userId, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint is required");
        }
        PushSubscription subscription = repository.findByEndpoint(endpoint)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        
        // 다른 사용자의 endpoint는 비활성화할 수 없다.
        if (!subscription.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cannot unsubscribe endpoint belonging to another user");
        }
        
        subscription.deactivate();
        repository.save(subscription);
    }

    public void deactivateSubscriptionByWithdrawal(Long userId) {
        if (userId == null) {
            return;
        }
        List<PushSubscription> subscriptions = repository.findByUserId(userId);
        for (PushSubscription sub : subscriptions) {
            sub.deactivate();
        }
        repository.saveAll(subscriptions);
    }
}
