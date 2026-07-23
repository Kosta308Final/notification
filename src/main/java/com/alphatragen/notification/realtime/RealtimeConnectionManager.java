package com.alphatragen.notification.realtime;

import com.alphatragen.notification.dto.RealtimeNotificationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeConnectionManager {
    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();

    public SseEmitter connect(String deviceId) {
        SseEmitter emitter = new SseEmitter(0L);
        SseEmitter previous = connections.put(deviceId, emitter);
        if (previous != null) previous.complete();
        emitter.onCompletion(() -> connections.remove(deviceId, emitter));
        emitter.onTimeout(() -> connections.remove(deviceId, emitter));
        emitter.onError(error -> connections.remove(deviceId, emitter));
        return emitter;
    }

    public boolean send(String deviceId, RealtimeNotificationDto notification) {
        SseEmitter emitter = connections.get(deviceId);
        if (emitter == null) return false;
        try {
            emitter.send(SseEmitter.event().name("notification").id(String.valueOf(notification.notificationId())).data(notification));
            return true;
        } catch (IOException | IllegalStateException exception) {
            connections.remove(deviceId, emitter);
            emitter.completeWithError(exception);
            return false;
        }
    }

    public void disconnect(String deviceId) {
        SseEmitter emitter = connections.remove(deviceId);
        if (emitter != null) emitter.complete();
    }

    public void disconnectAll() {
        connections.values().forEach(SseEmitter::complete);
        connections.clear();
    }
}
