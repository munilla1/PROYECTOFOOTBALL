package com.example.football.estadisticas.domain;

/**
 * Value Object: Resultado final de un partido.
 */
public record Score(
        Integer homeGoals,
        Integer awayGoals) {

    public Score {
        if (homeGoals == null || homeGoals < 0) {
            throw new IllegalArgumentException("Home goals cannot be null or negative");
        }
        if (awayGoals == null || awayGoals < 0) {
            throw new IllegalArgumentException("Away goals cannot be null or negative");
        }
    }

    /**
     * Retorna true si el marcador es válido (ambos goles >= 0).
     */
    public boolean isValid() {
        return homeGoals >= 0 && awayGoals >= 0;
    }
}
