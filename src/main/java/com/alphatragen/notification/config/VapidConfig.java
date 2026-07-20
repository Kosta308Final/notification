package com.alphatragen.notification.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.security.Security;

@Configuration
public class VapidConfig {

    @Value("${app.vapid.public-key:}")
    private String publicKey;

    @Value("${app.vapid.private-key:}")
    private String privateKey;

    @Value("${app.vapid.subject:}")
    private String subject;

    public String getPublicKey() {
        return publicKey;
    }

    @PostConstruct
    public void initProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Bean
    public PushService pushService() {
        if (publicKey == null || publicKey.trim().isEmpty() ||
            privateKey == null || privateKey.trim().isEmpty() ||
            subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("VAPID configurations (public-key, private-key, subject) must not be empty");
        }
        try {
            return new PushService(publicKey, privateKey, subject);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize PushService with VAPID keys", e);
        }
    }
}
