package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.DesktopDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface DesktopDeviceRepository extends JpaRepository<DesktopDevice, Long> {
    Optional<DesktopDevice> findByDeviceId(String deviceId);
    List<DesktopDevice> findByUserIdInAndApartmentIdAndActiveTrue(List<Long> userIds, Long apartmentId);
    List<DesktopDevice> findByUserIdAndApartmentIdAndActiveTrue(Long userId, Long apartmentId);
}
