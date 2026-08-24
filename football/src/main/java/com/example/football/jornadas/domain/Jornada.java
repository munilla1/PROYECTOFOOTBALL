package com.example.football.jornadas.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Jornada.
 * Representa una jornada (ronda) de competición sincronizada desde API-Football.
 * 
 * Una jornada es una unidad temporal en la que se juegan múltiples partidos de una liga
 * en una temporada específica.
 * 
 * Invariantes:
 * - roundNumber debe estar entre 1 y 38 (máximo en ligas reales)
 * - league no puede ser vacío
 * - season debe ser >= 2000
 * - matchCount >= 0
 * - createdAt y synchronizedAt no pueden ser nulos
 * - Identidad única por (league, season, roundNumber) - validada en BD
 */
public record Jornada(
        UUID id,
        Integer roundNumber,
        String league,
        Integer season,
        JornadaStatus status,
        Integer matchCount,
        Instant createdAt,
        Instant synchronizedAt
) {
    /**
     * Compact constructor con validaciones de invariantes.
     */
    public Jornada {
        if (roundNumber == null || roundNumber < 1 || roundNumber > 38) {
            throw new JornadasException("Round number must be between 1 and 38, got: " + roundNumber);
        }
        if (league == null || league.isBlank()) {
            throw new JornadasException("League cannot be null or blank");
        }
        if (season == null || season < 2000) {
            throw new JornadasException("Season must be >= 2000, got: " + season);
        }
        if (matchCount == null || matchCount < 0) {
            throw new JornadasException("Match count cannot be negative");
        }
        if (status == null) {
            throw new JornadasException("Status cannot be null");
        }
        if (createdAt == null) {
            throw new JornadasException("createdAt cannot be null");
        }
        if (synchronizedAt == null) {
            throw new JornadasException("synchronizedAt cannot be null");
        }
    }

    /**
     * Factory method para crear una nueva jornada.
     * Genera automáticamente UUID e Instant.now() para timestamps.
     */
    public static Jornada nueva(Integer roundNumber, String league, Integer season, 
                                JornadaStatus status, Integer matchCount) {
        return new Jornada(
                UUID.randomUUID(),
                roundNumber,
                league,
                season,
                status,
                matchCount,
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Indica si se pueden jugar partidos en esta jornada.
     * Solo es posible cuando el estado es IN_PROGRESS.
     */
    public boolean isPlayable() {
        return this.status == JornadaStatus.IN_PROGRESS;
    }

    /**
     * Retorna identificador único compuesto: league/season/round.
     * Útil para logging y auditoría.
     */
    public String getCompositeId() {
        return String.format("%s/%d/R%02d", league, season, roundNumber);
    }
}
