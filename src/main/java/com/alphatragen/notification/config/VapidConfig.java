package com.alphatragen.notification.config;

import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.jose4j.lang.JoseException;

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
            return new FcmCompatiblePushService(publicKey, privateKey, subject);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize PushService with VAPID keys", e);
        }
    }

    private static final class FcmCompatiblePushService extends PushService {

        private static final Pattern P256ECDSA_PATTERN =
                Pattern.compile("(p256ecdsa=)([^;,\\s]+)");

        private FcmCompatiblePushService(String publicKey, String privateKey, String subject)
                throws GeneralSecurityException {
            super(publicKey, privateKey, subject);
        }

        @Override
        public HttpPost preparePost(Notification notification, Encoding encoding)
                throws GeneralSecurityException, IOException, JoseException {
            HttpPost request = super.preparePost(notification, encoding);
            Header cryptoKey = request.getFirstHeader("Crypto-Key");
            if (cryptoKey == null) {
                return request;
            }

            String normalized = normalizeP256Ecdsa(cryptoKey.getValue());
            if (!normalized.equals(cryptoKey.getValue())) {
                request.removeHeaders("Crypto-Key");
                request.setHeader("Crypto-Key", normalized);
            }
            return request;
        }

        private static String normalizeP256Ecdsa(String headerValue) {
            Matcher matcher = P256ECDSA_PATTERN.matcher(headerValue);
            if (!matcher.find()) {
                return headerValue;
            }

            String encodedKey = matcher.group(2);
            if (!encodedKey.endsWith("=")) {
                return headerValue;
            }

            byte[] decodedKey = Base64.getUrlDecoder().decode(encodedKey);
            String unpaddedUrlBase64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(decodedKey);
            return matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + unpaddedUrlBase64Key));
        }
    }
}
