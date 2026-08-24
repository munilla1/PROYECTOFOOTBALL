package com.example.football.estadisticas.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio: Registro de sincronización.
 * 
 * Registra cada sincronización de estadísticas para auditoría y recuperación ante fallos.
 */
public record SyncLog(
        UUID id,
        Instant timestamp,
        String status,                  // "SUCCESS", "FAILED", "PARTIAL"
        String league,
        Integer season,
        Integer roundSynced,
        Integer playersUpdated,
        Integer matchesUpdated,
        Long durationMs,
        List<String> errors) {

    public SyncLog {
        if (id == null) {
            throw new IllegalArgumentException("SyncLog ID cannot be null");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        if (league == null || league.isBlank()) {
            throw new IllegalArgumentException("League cannot be blank");
        }
        if (season == null || season < 2000) {
            throw new IllegalArgumentException("Season must be valid");
        }
        if (roundSynced == null || roundSynced < 1) {
            throw new IllegalArgumentException("Round must be >= 1");
        }
        if (playersUpdated == null || playersUpdated < 0) {
            throw new IllegalArgumentException("Players updated cannot be negative");
        }
        if (matchesUpdated == null || matchesUpdated < 0) {
            throw new IllegalArgumentException("Matches updated cannot be negative");
        }
        if (durationMs == null || durationMs < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        if (errors == null) {
            errors = List.of();
        }
    }

    /**
     * Crea un nuevo registro de sincronización exitosa.
     */
    public static SyncLog exitosa(
            String league,
            Integer season,
            Integer round,
            Integer playersUpdated,
            Integer matchesUpdated,
            Long durationMs) {
        return new SyncLog(
                UUID.randomUUID(),
                Instant.now(),
                "SUCCESS",
                league,
                season,
                round,
                playersUpdated,
                matchesUpdated,
                durationMs,
                List.of()
        );
    }

    /**
     * Crea un nuevo registro de sincronización fallida.
     */
    public static SyncLog fallida(
            String league,
            Integer season,
            Integer round,
            List<String> errors,
            Long durationMs) {
        return new SyncLog(
                UUID.randomUUID(),
                Instant.now(),
                "FAILED",
                league,
                season,
                round,
                0,
                0,
                durationMs,
                errors
        );
    }

    /**
     * Retorna true si la sincronización fue exitosa.
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
}
