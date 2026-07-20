package com.alphatragen.notification.dto;

import java.util.List;

public record RecipientPreviewRespDto(int count, List<Long> userIds) { }
