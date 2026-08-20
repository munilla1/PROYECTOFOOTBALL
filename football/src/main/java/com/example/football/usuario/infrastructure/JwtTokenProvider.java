package com.example.football.usuario.infrastructure;

import com.example.football.usuario.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiracionMs;

    public JwtTokenProvider(@Value("${app.jwt.secret:dev-only-jwt-secret-change-before-production-2026}") String secret,
                            @Value("${app.jwt.expiration:86400000}") long expiracionMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(UUID usuarioId, Rol rol) {
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("rol", rol.valor())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracionMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerSujetoDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String extraerRolDelToken(String token) {
        Claims claims = obtenerClaims(token);
        Object rol = claims.get("rol");
        if (rol == null) {
            throw new IllegalArgumentException("Token no contiene claim 'rol'");
        }
        return rol.toString();
    }

    public boolean esTokenValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
