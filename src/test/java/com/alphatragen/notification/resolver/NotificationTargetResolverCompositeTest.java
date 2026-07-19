package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationTargetResolverCompositeTest {

    private UserServiceClient userServiceClient;
    private NotificationTargetResolverComposite compositeResolver;

    @BeforeEach
    void setUp() {
        userServiceClient = mock(UserServiceClient.class);
        TargetResolver individualResolver = new IndividualTargetResolver(userServiceClient);
        TargetResolver householdResolver = new HouseholdTargetResolver(userServiceClient);
        TargetResolver buildingResolver = new BuildingTargetResolver(userServiceClient);
        TargetResolver roleResolver = new RoleTargetResolver(userServiceClient);
        TargetResolver apartmentResolver = new ApartmentTargetResolver(userServiceClient);

        compositeResolver = new NotificationTargetResolverComposite(Arrays.asList(
                individualResolver, householdResolver, buildingResolver, roleResolver, apartmentResolver
        ));
    }

    @Test
    void testResolveIndividualSuccess() {
        when(userServiceClient.findUsersByIndividual(1L, 100L))
                .thenReturn(Collections.singletonList(100L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.INDIVIDUAL, 1L, 100L, null, null, null);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0));
        verify(userServiceClient).findUsersByIndividual(1L, 100L);
    }

    @Test
    void testResolveIndividualThrowsExceptionWhenUserIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            compositeResolver.resolveTargets(NotificationTargetType.INDIVIDUAL, 1L, null, null, null, null);
        });
    }

    @Test
    void testResolveHouseholdSuccess() {
        when(userServiceClient.findUsersByHousehold(1L, "101동", "102호"))
                .thenReturn(Arrays.asList(100L, 200L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.HOUSEHOLD, 1L, null, "101동", "102호", null);

        assertEquals(2, result.size());
        assertTrue(result.contains(100L));
        assertTrue(result.contains(200L));
        verify(userServiceClient).findUsersByHousehold(1L, "101동", "102호");
    }

    @Test
    void testResolveBuildingSuccess() {
        when(userServiceClient.findUsersByBuilding(1L, "101동"))
                .thenReturn(Arrays.asList(100L, 200L, 300L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.BUILDING, 1L, null, "101동", null, null);

        assertEquals(3, result.size());
        verify(userServiceClient).findUsersByBuilding(1L, "101동");
    }

    @Test
    void testResolveRoleSuccess() {
        when(userServiceClient.findUsersByRole(1L, "REPRESENTATIVE"))
                .thenReturn(Arrays.asList(100L, 300L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.ROLE, 1L, null, null, null, "REPRESENTATIVE");

        assertEquals(2, result.size());
        verify(userServiceClient).findUsersByRole(1L, "REPRESENTATIVE");
    }

    @Test
    void testResolveApartmentSuccess() {
        when(userServiceClient.findUsersByApartment(1L))
                .thenReturn(Arrays.asList(100L, 200L, 300L, 400L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.APARTMENT, 1L, null, null, null, null);

        assertEquals(4, result.size());
        verify(userServiceClient).findUsersByApartment(1L);
    }

    @Test
    void testDeduplicationAndNullRemoval() {
        when(userServiceClient.findUsersByApartment(1L))
                .thenReturn(Arrays.asList(100L, 100L, null, 200L, 200L));

        List<Long> result = compositeResolver.resolveTargets(NotificationTargetType.APARTMENT, 1L, null, null, null, null);

        assertEquals(2, result.size());
        assertTrue(result.contains(100L));
        assertTrue(result.contains(200L));
    }
}
