package com.alphatragen.notification.realtime;

import com.alphatragen.notification.domain.DesktopDevice;
import com.alphatragen.notification.dto.RealtimeNotificationDto;
import com.alphatragen.notification.repository.DesktopDeviceRepository;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.annotation.PreDestroy;

import java.util.Map;

@Service
public class RealtimeConnectionService {
    private final DesktopDeviceRepository deviceRepository;
    private final RealtimeConnectionManager manager = new RealtimeConnectionManager();

    public RealtimeConnectionService(DesktopDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public SseEmitter connect(Long userId, Long apartmentId, String deviceId) {
        DesktopDevice device = ownedActiveDevice(userId, apartmentId, deviceId);
        SseEmitter emitter = manager.connect(device.getDeviceId());
        try {
            emitter.send(SseEmitter.event().name("connected").data(MapData.connected(device.getDeviceId())));
        } catch (Exception exception) {
            manager.disconnect(device.getDeviceId());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "실시간 연결을 시작할 수 없습니다.", exception);
        }
        return emitter;
    }

    public DeliveryResult send(NotificationCreatedEvent event) {
        RealtimeNotificationDto payload = new RealtimeNotificationDto(event.notificationId(), event.eventId(),
                event.title(), event.content(), event.importance(), event.actionUrl(), event.importance().name().equals("URGENT"), event.createdAt());
        int success = 0;
        int failure = 0;
        for (DesktopDevice device : deviceRepository.findByUserIdInAndApartmentIdAndActiveTrue(event.recipientUserIds(), event.apartmentId())) {
            if (manager.send(device.getDeviceId(), payload)) success++; else failure++;
        }
        return new DeliveryResult(success, failure);
    }

    public void disconnect(String deviceId) { manager.disconnect(deviceId); }

    @PreDestroy
    public void shutdown() { manager.disconnectAll(); }

    private DesktopDevice ownedActiveDevice(Long userId, Long apartmentId, String deviceId) {
        DesktopDevice device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        if (!device.getUserId().equals(userId) || !device.getApartmentId().equals(apartmentId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device belongs to another user or apartment");
        if (!device.isActive()) throw new ResponseStatusException(HttpStatus.GONE, "Device is inactive");
        return device;
    }

    public record DeliveryResult(int successCount, int failureCount) {}

    private static final class MapData {
        private static Map<String, String> connected(String deviceId) { return Map.of("deviceId", deviceId); }
    }
}
