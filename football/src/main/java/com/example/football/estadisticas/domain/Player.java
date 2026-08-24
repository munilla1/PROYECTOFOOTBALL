package com.example.football.estadisticas.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Jugador con estadísticas reales.
 * 
 * Representa un jugador cuyos datos proceden de API-Football.
 * Esta es una entidad PURA del dominio, sin dependencias externas.
 */
public record Player(
        UUID id,
        String externalId,              // ID desde API-Football
        String name,
        String position,                 // "ST", "CM", "CB", etc.
        Integer age,
        String nationality,
        String teamId,
        RealStats realStats,
        Instant lastUpdated,
        Instant createdAt) {

    public Player {
        if (id == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        if (age == null || age < 16 || age > 50) {
            throw new IllegalArgumentException("Player age must be between 16 and 50");
        }
        if (realStats == null) {
            throw new IllegalArgumentException("RealStats cannot be null");
        }
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Crea un nuevo jugador desde datos de API-Football.
     */
    public static Player nueva(
            String externalId,
            String name,
            String position,
            Integer age,
            String nationality,
            String teamId,
            RealStats realStats) {
        return new Player(
                UUID.randomUUID(),
                externalId,
                name,
                position,
                age,
                nationality,
                teamId,
                realStats,
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Actualiza las estadísticas del jugador.
     */
    public Player conEstadisticas(RealStats nuevoStats) {
        return new Player(
                this.id,
                this.externalId,
                this.name,
                this.position,
                this.age,
                this.nationality,
                this.teamId,
                nuevoStats,
                Instant.now(),
                this.createdAt
        );
    }
}
