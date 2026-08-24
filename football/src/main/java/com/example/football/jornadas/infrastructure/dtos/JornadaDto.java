package com.example.football.jornadas.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para datos de jornada desde API-Football.
 * 
 * Represent a round/matchday as returned by API-Football.
 * Ignora propiedades adicionales de la API para flexibilidad.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JornadaDto(
        @JsonProperty("round")
        String roundIdentifier,  // Ej: "Regular Season - 1", "Playoffs - 1"

        @JsonProperty("league")
        LeagueInfoDto league,

        @JsonProperty("fixtures")
        FixturesDataDto fixtures
) {
    /**
     * DTO con información de la liga desde API-Football.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfoDto(
            @JsonProperty("id")
            Integer id,

            @JsonProperty("name")
            String name,

            @JsonProperty("season")
            Integer season
    ) {}

    /**
     * DTO con datos de fixtures/partidos en una jornada.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixturesDataDto(
            @JsonProperty("current")
            Integer current,  // Número actual de partidos completados

            @JsonProperty("total")
            Integer total     // Total de partidos en la jornada
    ) {}
}
