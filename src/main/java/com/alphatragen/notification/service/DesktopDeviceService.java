package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.DesktopDevice;
import com.alphatragen.notification.dto.DesktopDeviceRegistrationReqDto;
import com.alphatragen.notification.repository.DesktopDeviceRepository;
import com.alphatragen.notification.realtime.RealtimeConnectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class DesktopDeviceService {

    private final DesktopDeviceRepository repository;
    private final RealtimeConnectionService realtimeConnectionService;

    public DesktopDeviceService(DesktopDeviceRepository repository, RealtimeConnectionService realtimeConnectionService) {
        this.repository = repository;
        this.realtimeConnectionService = realtimeConnectionService;
    }

    public DesktopDevice register(Long userId, Long apartmentId, DesktopDeviceRegistrationReqDto request) {
        DesktopDevice device = repository.findByDeviceId(request.deviceId()).orElse(null);
        if (device != null) {
            if (!device.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Device is already registered by another user");
            }
            device.update(apartmentId, request.deviceName(), request.platform(), request.appVersion(),
                    request.notificationPermission(), LocalDateTime.now());
            return repository.save(device);
        }

        return repository.save(DesktopDevice.builder()
                .userId(userId)
                .apartmentId(apartmentId)
                .deviceId(request.deviceId())
                .deviceName(request.deviceName())
                .platform(request.platform())
                .appVersion(request.appVersion())
                .notificationPermission(request.notificationPermission())
                .lastConnectedAt(LocalDateTime.now())
                .build());
    }

    public DesktopDevice update(Long userId, Long apartmentId, String deviceId,
                                DesktopDeviceRegistrationReqDto request) {
        if (!deviceId.equals(request.deviceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path deviceId and body deviceId must match");
        }
        DesktopDevice device = findOwnedDevice(userId, deviceId);
        device.update(apartmentId, request.deviceName(), request.platform(), request.appVersion(),
                request.notificationPermission(), LocalDateTime.now());
        return repository.save(device);
    }

    public void deactivate(Long userId, String deviceId) {
        DesktopDevice device = findOwnedDevice(userId, deviceId);
        device.deactivate();
        repository.save(device);
        if (realtimeConnectionService != null) {
            realtimeConnectionService.disconnect(deviceId);
        }
    }

    public void heartbeat(Long userId, String deviceId) {
        DesktopDevice device = findOwnedDevice(userId, deviceId);
        device.heartbeat(LocalDateTime.now());
        repository.save(device);
    }

    private DesktopDevice findOwnedDevice(Long userId, String deviceId) {
        DesktopDevice device = repository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        if (!device.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device belongs to another user");
        }
        return device;
    }
}
