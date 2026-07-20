package com.alphatragen.notification.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** JWT에서 알림 서비스가 사용하는 사용자 식별 정보를 읽는다. */
public record JwtUserClaims(Long userId, Long apartmentId, List<String> roles) {
    public static JwtUserClaims from(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT authentication is required");
        }
        Jwt jwt = token.getToken();
        Long userId = numberClaim(jwt, "userId", "user_id", "sub");
        Long apartmentId = numberClaim(jwt, "apartmentId", "apartment_id");
        if (userId == null || apartmentId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Required JWT claims are missing");
        }
        return new JwtUserClaims(userId, apartmentId, roles(jwt));
    }

    private static Long numberClaim(Jwt jwt, String... names) {
        for (String name : names) {
            Object value = jwt.getClaims().get(name);
            if (value instanceof Number number) return number.longValue();
            if (value != null) {
                try { return Long.valueOf(value.toString()); } catch (NumberFormatException ignored) { }
            }
        }
        return null;
    }

    private static List<String> roles(Jwt jwt) {
        Object value = jwt.getClaims().get("roles");
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        if (value != null) return Arrays.stream(value.toString().split("[, ]+")).filter(s -> !s.isBlank()).toList();
        return List.of();
    }
}
