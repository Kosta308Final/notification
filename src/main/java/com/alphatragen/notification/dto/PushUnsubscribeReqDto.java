package com.alphatragen.notification.dto;

import jakarta.validation.constraints.NotBlank;

public class PushUnsubscribeReqDto {

    @NotBlank(message = "endpoint is required")
    private String endpoint;

    public PushUnsubscribeReqDto() {
    }

    public PushUnsubscribeReqDto(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
