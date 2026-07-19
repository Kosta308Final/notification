package com.alphatragen.notification.dto;

import java.util.List;

public record RecipientPreviewResponseDto(int count, List<Long> userIds) { }
