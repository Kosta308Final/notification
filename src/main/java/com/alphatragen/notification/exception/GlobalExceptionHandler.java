package com.alphatragen.notification.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("request_failed status=409 code=DUPLICATE_RESOURCE path={}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다.", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = switch (status) {
            case UNAUTHORIZED -> "AUTHENTICATION_ERROR";
            case FORBIDDEN -> "AUTHORIZATION_ERROR";
            case NOT_FOUND -> "RESOURCE_NOT_FOUND";
            default -> "REQUEST_ERROR";
        };
        String message = status.is5xxServerError() ? "요청을 처리할 수 없습니다." : exception.getReason();
        return response(status, code, message, request);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", "인증이 필요합니다.", request);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "AUTHORIZATION_ERROR", "접근 권한이 없습니다.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("request_failed status=500 code=INTERNAL_ERROR path={}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", request);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + (error.getDefaultMessage() == null ? "잘못된 값입니다." : error.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message, HttpServletRequest request) {
        String safeMessage = message == null || message.isBlank() ? "요청을 처리할 수 없습니다." : message;
        log.warn("request_failed status={} code={} path={}", status.value(), code, request.getRequestURI());
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(), code, safeMessage, request.getRequestURI()));
    }
}
