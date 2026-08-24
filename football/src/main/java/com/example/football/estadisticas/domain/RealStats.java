package com.example.football.estadisticas.domain;

import java.time.Instant;

/**
 * Value Object: Estadísticas reales de un jugador.
 * Encapsula datos normalizados desde API-Football.
 */
public record RealStats(
        Integer season,
        String league,
        Integer appearances,
        Integer goals,
        Integer assists,
        Integer passesAccuracy,         // 0-100
        Integer dribblesSuccess,        // 0-100
        Integer tackles,
        Integer performanceScore,       // 0-100
        Instant lastUpdated) {

    public RealStats {
        if (season == null || season < 2000) {
            throw new IllegalArgumentException("Season must be valid (>= 2000)");
        }
        if (league == null || league.isBlank()) {
            throw new IllegalArgumentException("League cannot be blank");
        }
        if (appearances != null && appearances < 0) {
            throw new IllegalArgumentException("Appearances cannot be negative");
        }
        if (goals != null && goals < 0) {
            throw new IllegalArgumentException("Goals cannot be negative");
        }
        if (assists != null && assists < 0) {
            throw new IllegalArgumentException("Assists cannot be negative");
        }
        if (passesAccuracy != null && (passesAccuracy < 0 || passesAccuracy > 100)) {
            throw new IllegalArgumentException("Passes accuracy must be 0-100");
        }
        if (dribblesSuccess != null && (dribblesSuccess < 0 || dribblesSuccess > 100)) {
            throw new IllegalArgumentException("Dribbles success must be 0-100");
        }
        if (tackles != null && tackles < 0) {
            throw new IllegalArgumentException("Tackles cannot be negative");
        }
        if (performanceScore != null && (performanceScore < 0 || performanceScore > 100)) {
            throw new IllegalArgumentException("Performance score must be 0-100");
        }
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
    }

    /**
     * Crea un RealStats vacío para inicialización.
     */
    public static RealStats empty(Integer season, String league) {
        return new RealStats(
                season,
                league,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                Instant.now()
        );
    }
}
