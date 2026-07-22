package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.DesktopDevice;
import com.alphatragen.notification.dto.DesktopDeviceRegistrationReqDto;
import com.alphatragen.notification.repository.DesktopDeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesktopDeviceServiceTest {

    @Mock
    private DesktopDeviceRepository repository;

    @InjectMocks
    private DesktopDeviceService service;

    @Test
    void registersNewDeviceForAuthenticatedUser() {
        DesktopDeviceRegistrationReqDto request = request("device-1", "DESKTOP-1");
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.empty());
        when(repository.save(any(DesktopDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesktopDevice result = service.register(7L, 3L, request);

        assertEquals(7L, result.getUserId());
        assertEquals(3L, result.getApartmentId());
        assertEquals("DESKTOP-1", result.getDeviceName());
    }

    @Test
    void sameUsersRegistrationUpdatesExistingDevice() {
        DesktopDevice existing = serviceDevice(7L, "device-1");
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DesktopDevice result = service.register(7L, 4L, request("device-1", "DESKTOP-2"));

        assertEquals(4L, result.getApartmentId());
        assertEquals("DESKTOP-2", result.getDeviceName());
    }

    @Test
    void rejectsDeviceOwnedByAnotherUser() {
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.of(serviceDevice(7L, "device-1")));

        assertThrows(IllegalArgumentException.class,
                () -> service.register(8L, 3L, request("device-1", "DESKTOP-1")));
    }

    @Test
    void updatesOwnedDevice() {
        DesktopDevice existing = serviceDevice(7L, "device-1");
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DesktopDevice result = service.update(7L, 4L, "device-1", request("device-1", "DESKTOP-2"));

        assertEquals(4L, result.getApartmentId());
        assertEquals("DESKTOP-2", result.getDeviceName());
        assertTrue(result.isActive());
    }

    @Test
    void deactivatesOwnedDevice() {
        DesktopDevice existing = serviceDevice(7L, "device-1");
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.deactivate(7L, "device-1");

        assertFalse(existing.isActive());
    }

    @Test
    void heartbeatReactivatesDeviceAndUpdatesLastConnectedAt() {
        DesktopDevice existing = DesktopDevice.builder().userId(7L).apartmentId(3L).deviceId("device-1")
                .deviceName("DESKTOP-1").platform("WINDOWS").appVersion("1.0.0")
                .notificationPermission("GRANTED").active(false).build();
        when(repository.findByDeviceId("device-1")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.heartbeat(7L, "device-1");

        assertTrue(existing.isActive());
    }

    private DesktopDeviceRegistrationReqDto request(String deviceId, String deviceName) {
        return new DesktopDeviceRegistrationReqDto(deviceId, deviceName, "WINDOWS", "1.0.0", "GRANTED");
    }

    private DesktopDevice serviceDevice(Long userId, String deviceId) {
        return DesktopDevice.builder().userId(userId).apartmentId(3L).deviceId(deviceId)
                .deviceName("DESKTOP-OLD").platform("WINDOWS").appVersion("1.0.0")
                .notificationPermission("DEFAULT").build();
    }
}
