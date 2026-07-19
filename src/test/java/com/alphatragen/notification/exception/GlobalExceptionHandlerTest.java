package com.alphatragen.notification.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("POST", "/api/notifications");

    @Test
    void validationErrorUsesSafeCommonResponse() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("title is required"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void unexpectedErrorDoesNotExposeInternalDetails() {
        var response = handler.handleUnexpected(new RuntimeException("database password=secret"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("서버 내부 오류가 발생했습니다.");
        assertThat(response.getBody().message()).doesNotContain("secret");
    }

    @Test
    void statusExceptionMapsToResourceNotFound() {
        var response = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }
}
