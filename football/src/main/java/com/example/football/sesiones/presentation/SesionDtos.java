package com.example.football.sesiones.presentation;

import com.example.football.sesiones.application.SesionService;

import java.time.Instant;
import java.util.UUID;

public final class SesionDtos {
    private SesionDtos() {
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String token, Instant expiresAt, UUID userId) {
        public static LoginResponse from(SesionService.LoginResult result) {
            return new LoginResponse(result.token(), result.expiresAt(), result.userId());
        }
    }
}
