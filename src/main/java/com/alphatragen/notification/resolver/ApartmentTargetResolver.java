package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApartmentTargetResolver implements TargetResolver {

    private final UserServiceClient userServiceClient;

    public ApartmentTargetResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean supports(NotificationTargetType targetType) {
        return targetType == NotificationTargetType.APARTMENT;
    }

    @Override
    public List<Long> resolve(Long apartmentId, Long userId, String building, String unit, String role) {
        if (apartmentId == null) {
            throw new IllegalArgumentException("apartmentId is required for APARTMENT target type");
        }
        return userServiceClient.findUsersByApartment(apartmentId);
    }
}
