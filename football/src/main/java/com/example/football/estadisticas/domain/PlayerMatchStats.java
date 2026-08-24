package com.example.football.estadisticas.domain;

/**
 * Value Object: Estadísticas de un jugador en un partido específico.
 */
public record PlayerMatchStats(
        String playerId,
        String playerName,
        String team,
        Integer goals,
        Integer assists,
        Integer minutesPlayed,
        Double rating) {

    public PlayerMatchStats {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player ID cannot be blank");
        }
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        if (goals != null && goals < 0) {
            throw new IllegalArgumentException("Goals cannot be negative");
        }
        if (assists != null && assists < 0) {
            throw new IllegalArgumentException("Assists cannot be negative");
        }
        if (minutesPlayed != null && minutesPlayed < 0) {
            throw new IllegalArgumentException("Minutes played cannot be negative");
        }
        if (rating != null && (rating < 0.0 || rating > 10.0)) {
            throw new IllegalArgumentException("Rating must be 0-10");
        }
    }
}
