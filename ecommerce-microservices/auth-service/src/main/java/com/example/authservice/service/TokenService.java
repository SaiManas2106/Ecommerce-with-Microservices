package com.example.authservice.service;

import com.example.authservice.exception.AuthException;
import com.example.authservice.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {

    private final String secret;
    private final long ttlSeconds;

    public TokenService(@Value("${app.auth.token-secret}") String secret,
                        @Value("${app.auth.token-ttl-seconds:86400}") long ttlSeconds) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(AppUser user) {
        long expiresAt = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        String payload = user.getId() + "|" + user.getEmail() + "|" + user.getRole() + "|" + expiresAt;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public Long validateAndGetUserId(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            throw new AuthException("Invalid token");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|");
        if (values.length != 4 || Long.parseLong(values[3]) < Instant.now().getEpochSecond()) {
            throw new AuthException("Expired token");
        }
        return Long.parseLong(values[0]);
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encode(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
