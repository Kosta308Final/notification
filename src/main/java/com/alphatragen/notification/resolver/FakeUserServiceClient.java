package com.alphatragen.notification.resolver;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class FakeUserServiceClient implements UserServiceClient {
    @Override
    public List<Long> findUsersByIndividual(Long apartmentId, Long userId) {
        return Collections.singletonList(userId);
    }

    @Override
    public List<Long> findUsersByHousehold(Long apartmentId, String building, String unit) {
        return Collections.emptyList();
    }

    @Override
    public List<Long> findUsersByBuilding(Long apartmentId, String building) {
        return Collections.emptyList();
    }

    @Override
    public List<Long> findUsersByRole(Long apartmentId, String role) {
        return Collections.emptyList();
    }

    @Override
    public List<Long> findUsersByApartment(Long apartmentId) {
        return Collections.emptyList();
    }
}
