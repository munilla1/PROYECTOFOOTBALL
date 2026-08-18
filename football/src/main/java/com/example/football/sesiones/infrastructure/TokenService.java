package com.example.football.sesiones.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class TokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final SecureRandom secureRandom = new SecureRandom();
    private final byte[] secret;

    public TokenService(@Value("${app.session.secret:change-me-in-production}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("app.session.secret debe tener al menos 32 bytes");
        }
    }

    public String issue(UUID sessionId, UUID userId, Instant issuedAt, Instant expiresAt) {
        byte[] nonce = new byte[16];
        secureRandom.nextBytes(nonce);
        String payload = String.join(".", sessionId.toString(), userId.toString(),
                Long.toString(issuedAt.getEpochSecond()), Long.toString(expiresAt.getEpochSecond()),
                Base64.getUrlEncoder().withoutPadding().encodeToString(nonce));
        return payload + "." + encode(sign(payload));
    }

    public TokenData parseAndVerify(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token ausente");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Token mal formado");
        }
        String payload = String.join(".", parts[0], parts[1], parts[2], parts[3], parts[4]);
        byte[] expected = sign(payload);
        byte[] actual;
        try {
            actual = Base64.getUrlDecoder().decode(parts[5]);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Firma de token mal formada", exception);
        }
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IllegalArgumentException("Firma de token invalida");
        }
        try {
            return new TokenData(UUID.fromString(parts[0]), UUID.fromString(parts[1]),
                    Instant.ofEpochSecond(Long.parseLong(parts[2])),
                    Instant.ofEpochSecond(Long.parseLong(parts[3])));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Claims de token invalidas", exception);
        }
    }

    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo proteger el token", exception);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar el token", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public record TokenData(UUID sessionId, UUID userId, Instant issuedAt, Instant expiresAt) {
    }
}
