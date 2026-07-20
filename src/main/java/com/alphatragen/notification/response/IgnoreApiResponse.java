package com.alphatragen.notification.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ApiResponse 자동 감싸는 Advisor 타지 않는 제외용 어노테이션
 * [샘플 예제]
 * @IgnoreApiResponse
 * @GetMapping("/health")
 * public String health() {
 *     return "OK";
 * }
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreApiResponse {
}
