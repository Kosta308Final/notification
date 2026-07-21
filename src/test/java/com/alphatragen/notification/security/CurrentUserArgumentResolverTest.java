package com.alphatragen.notification.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsCurrentUserJwtUserClaimsParameter() throws NoSuchMethodException {
        MethodParameter parameter = currentUserParameter();

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void resolvesClaimsFromSecurityContextAuthentication() throws Exception {
        Jwt jwt = jwt(Map.of("userId", 7, "apartmentId", 11, "roles", List.of("OFFICE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        JwtUserClaims claims = (JwtUserClaims) resolver.resolveArgument(
                currentUserParameter(), null, null, null);

        assertEquals(7L, claims.userId());
        assertEquals(11L, claims.apartmentId());
        assertEquals(List.of("OFFICE_ADMIN"), claims.roles());
    }

    private MethodParameter currentUserParameter() throws NoSuchMethodException {
        Method method = Handler.class.getDeclaredMethod("handle", JwtUserClaims.class);
        return new MethodParameter(method, 0);
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.EPOCH, Instant.MAX,
                Map.of("alg", "none"), claims);
    }

    private static class Handler {
        @SuppressWarnings("unused")
        void handle(@CurrentUser JwtUserClaims claims) {
        }
    }
}
