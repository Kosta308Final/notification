package com.alphatragen.notification.response;


import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Controller가 반환한 값을 가로채서 자동으로 ApiResponse.success(...)로 감싸주는 공통 응답 처리기
 * [전체 흐름]
 * Controller 메서드 실행
 *         ↓
 * Controller가 body 반환
 *         ↓
 * ResponseBodyAdvice가 응답 body를 가로챔
 *         ↓
 * ApiResponse로 감싸야 하는지 판단
 *         ↓
 * 최종 JSON 응답 생성
 *
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    //이 Advice를 적용할지 말지 결정하는 메서드
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
//        return true; // 모든 Controller 응답에 적용
        // @IgnoreApiResponse가 붙은 Controller 또는 메서드는 ApiResponse로 감싸지 않겠다.
        return !returnType.hasMethodAnnotation(IgnoreApiResponse.class)
                && !returnType.getContainingClass().isAnnotationPresent(IgnoreApiResponse.class);
    }

    // 응답 body가 실제로 클라이언트에게 나가기 직전에 실행
    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // 다음중 하나의 조건에 해당되면 Wrapping 안하고 skip
        String path = request.getURI().getPath();
        boolean shouldSkipWrapping =
                   body instanceof ApiResponse<?>
                || body instanceof String
                || body instanceof byte[]
                || !selectedContentType.includes(MediaType.APPLICATION_JSON)
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");

        if (shouldSkipWrapping) {
            return body;
        }

        return ApiResponse.success(body);
    }
}
