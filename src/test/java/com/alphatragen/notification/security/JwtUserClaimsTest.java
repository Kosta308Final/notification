package com.alphatragen.notification.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUserClaimsTest {
    @Test
    void extractsIdentityApartmentAndRolesFromJwt() {
        Jwt jwt = jwt(Map.of("sub", "7", "apartmentId", 11, "roles", List.of("OFFICE_ADMIN")));
        JwtUserClaims claims = JwtUserClaims.from(new JwtAuthenticationToken(jwt));

        assertEquals(7L, claims.userId());
        assertEquals(11L, claims.apartmentId());
        assertEquals(List.of("OFFICE_ADMIN"), claims.roles());
    }

    @Test
    void rejectsJwtWithoutRequiredApartmentClaim() {
        Jwt jwt = jwt(Map.of("sub", "7"));
        assertThrows(ResponseStatusException.class,
                () -> JwtUserClaims.from(new JwtAuthenticationToken(jwt)));
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.EPOCH, Instant.MAX,
                Map.of("alg", "none"), claims);
    }
}
