package com.alphatragen.notification.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** 관리자 목록 화면에서 사용하는 공통 페이징 응답 형식. */
public record AdminNotificationPageRespDto(
        List<AdminNotificationRespDto> content,
        PageInfo page
) {
    public static AdminNotificationPageRespDto from(Page<AdminNotificationRespDto> source) {
        return new AdminNotificationPageRespDto(
                source.getContent(),
                new PageInfo(
                        source.getSize(),
                        source.getNumber(),
                        source.getTotalElements(),
                        source.getTotalPages()
                )
        );
    }

    public record PageInfo(
            int size,
            int number,
            long totalElements,
            int totalPages
    ) {
    }
}
