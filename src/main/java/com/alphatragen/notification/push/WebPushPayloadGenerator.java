package com.alphatragen.notification.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Web Push 메시지에 사용할 payload를 생성합니다. */
@Component
public class WebPushPayloadGenerator {

    private final ObjectMapper objectMapper;

    public WebPushPayloadGenerator() {
        this(new ObjectMapper());
    }

    public WebPushPayloadGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generate(Long notificationId, String title, String content,
                           String importance, String actionUrl) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notificationId);
        payload.put("title", title);
        payload.put("body", content);
        payload.put("importance", importance);
        payload.put("actionUrl", actionUrl);
        return objectMapper.writeValueAsString(payload);
    }
}
