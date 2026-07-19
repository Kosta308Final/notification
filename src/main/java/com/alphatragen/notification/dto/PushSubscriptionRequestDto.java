package com.alphatragen.notification.dto;

import jakarta.validation.constraints.NotBlank;

public class PushSubscriptionRequestDto {

    @NotBlank(message = "endpoint is required")
    private String endpoint;

    @NotBlank(message = "p256dh is required")
    private String p256dh;

    @NotBlank(message = "auth is required")
    private String auth;

    private String browser;
    private String deviceType;

    public PushSubscriptionRequestDto() {
    }

    public PushSubscriptionRequestDto(String endpoint, String p256dh, String auth, String browser, String deviceType) {
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.browser = browser;
        this.deviceType = deviceType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getP256dh() {
        return p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
