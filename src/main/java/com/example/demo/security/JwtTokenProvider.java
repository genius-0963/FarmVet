package com.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secretKeyBytes;
    private final long jwtExpirationInMs;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:SuperSecretFarmVetKey2026WithAtLeast256BitsLengthForHmacSha256Security!}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationInMs,
            ObjectMapper objectMapper) {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.objectMapper = objectMapper;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String role = userPrincipal.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_FARMER");

        return generateTokenForUser(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail(), role);
    }

    public String generateTokenForUser(Long userId, String username, String email, String role) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + jwtExpirationInMs;

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", username);
            payload.put("userId", userId);
            payload.put("email", email);
            payload.put("role", role);
            payload.put("iat", now / 1000);
            payload.put("exp", exp / 1000);

            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String contentToSign = encodedHeader + "." + encodedPayload;

            String signature = sign(contentToSign);
            return contentToSign + "." + signature;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", username, e);
            throw new RuntimeException("Could not generate JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content);

            if (!MessageDigest.isEqual(
                    parts[2].getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            Map<String, Object> claims = parsePayload(parts[1]);
            Number exp = (Number) claims.get("exp");
            if (exp == null) {
                return false;
            }

            long expTimeMillis = exp.longValue() * 1000;
            return System.currentTimeMillis() <= expTimeMillis;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromJWT(String token) {
        Map<String, Object> claims = getClaims(token);
        return (String) claims.get("sub");
    }

    public Long getUserIdFromJWT(String token) {
        Map<String, Object> claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public String getRoleFromJWT(String token) {
        Map<String, Object> claims = getClaims(token);
        return (String) claims.get("role");
    }

    private Map<String, Object> getClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            return parsePayload(parts[1]);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse JWT claims", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String base64UrlPayload) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(base64UrlPayload);
        return objectMapper.readValue(bytes, Map.class);
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM));
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(hmacBytes);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
