package com.alphatragen.notification.push;

import com.alphatragen.notification.domain.PushSubscription;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/** Web Push 응답을 해석하고 구독 상태를 갱신합니다. */
@Component
public class WebPushResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(WebPushResponseHandler.class);
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public WebPushResponseHandler(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    public void handle(PushSubscription subscription, HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
        log.info("Web Push response: status={}, body={}", statusCode, responseBody);

        if (statusCode == 201) {
            log.info("Successfully sent Web Push to subscription: {}", subscription.getId());
            subscription.activate(LocalDateTime.now());
            pushSubscriptionRepository.save(subscription);
        } else if (statusCode == 410 || statusCode == 404) {
            log.warn("Subscription has expired or is no longer valid. Deactivating subscription: {}. Status: {}",
                    subscription.getId(), statusCode);
            subscription.deactivate();
            pushSubscriptionRepository.save(subscription);
        } else {
            log.error("Failed to send Web Push to subscription: {}. Response status code: {}",
                    subscription.getId(), statusCode);
        }
    }
}
