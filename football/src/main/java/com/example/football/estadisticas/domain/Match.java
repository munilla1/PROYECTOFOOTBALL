package com.example.football.estadisticas.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio: Partido con estadísticas reales.
 * 
 * Representa un partido de fútbol real cuyos datos proceden de API-Football.
 */
public record Match(
        UUID id,
        String fixtureId,               // ID desde API-Football
        Integer round,
        String league,
        Integer season,
        Instant date,
        String homeTeamId,
        String awayTeamId,
        Score finalScore,
        String status,                  // "Match Finished", "Not Started", etc.
        List<PlayerMatchStats> playerStats,
        Instant lastUpdated,
        Instant createdAt) {

    public Match {
        if (id == null) {
            throw new IllegalArgumentException("Match ID cannot be null");
        }
        if (fixtureId == null || fixtureId.isBlank()) {
            throw new IllegalArgumentException("Fixture ID cannot be blank");
        }
        if (round == null || round < 1) {
            throw new IllegalArgumentException("Round must be >= 1");
        }
        if (league == null || league.isBlank()) {
            throw new IllegalArgumentException("League cannot be blank");
        }
        if (season == null || season < 2000) {
            throw new IllegalArgumentException("Season must be valid");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (homeTeamId == null || homeTeamId.isBlank()) {
            throw new IllegalArgumentException("Home team ID cannot be blank");
        }
        if (awayTeamId == null || awayTeamId.isBlank()) {
            throw new IllegalArgumentException("Away team ID cannot be blank");
        }
        if (homeTeamId.equals(awayTeamId)) {
            throw new IllegalArgumentException("Home and away teams cannot be the same");
        }
        if (finalScore == null) {
            throw new IllegalArgumentException("Final score cannot be null");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        if (playerStats == null) {
            playerStats = List.of();
        }
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Crea un nuevo partido desde datos de API-Football.
     */
    public static Match nueva(
            String fixtureId,
            Integer round,
            String league,
            Integer season,
            Instant date,
            String homeTeamId,
            String awayTeamId,
            Score finalScore,
            String status,
            List<PlayerMatchStats> playerStats) {
        return new Match(
                UUID.randomUUID(),
                fixtureId,
                round,
                league,
                season,
                date,
                homeTeamId,
                awayTeamId,
                finalScore,
                status,
                playerStats,
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Verifica si el partido está terminado.
     */
    public boolean isFinished() {
        return "Match Finished".equals(status);
    }
}
