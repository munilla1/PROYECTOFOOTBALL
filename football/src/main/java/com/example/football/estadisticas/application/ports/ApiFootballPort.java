package com.example.football.estadisticas.application.ports;

import com.example.football.estadisticas.infrastructure.dtos.ApiFootballDtos;
import java.util.List;

/**
 * Puerto: Interfaz de comunicación con API-Football.
 * 
 * Esta es la frontera entre la capa de aplicación y la infraestructura.
 * Define qué operaciones se pueden hacer contra API-Football.
 * 
 * La implementación está en infraestructura (ApiFootballClientAdapter).
 */
public interface ApiFootballPort {

    /**
     * Obtiene lista de jugadores de una liga en una temporada.
     * 
     * @param league Liga (ej: "LaLiga" o "135" para API-Football)
     * @param season Temporada (ej: 2023)
     * @return Lista de jugadores desde API
     * @throws ApiConnectivityException Si falla la conectividad
     * @throws RateLimitExceededException Si se alcanza el rate limit
     */
    List<ApiFootballDtos.PlayerDto> getPlayers(String league, Integer season)
            throws ApiConnectivityException, RateLimitExceededException;

    /**
     * Obtiene lista de partidos de una jornada.
     * 
     * @param league Liga
     * @param season Temporada
     * @param round Jornada/Round
     * @return Lista de partidos desde API
     * @throws ApiConnectivityException Si falla la conectividad
     * @throws RateLimitExceededException Si se alcanza el rate limit
     */
    List<ApiFootballDtos.FixtureDto> getFixtures(String league, Integer season, String round)
            throws ApiConnectivityException, RateLimitExceededException;

    /**
     * Obtiene estadísticas detalladas de un jugador en específico.
     * 
     * @param playerId ID del jugador en API-Football
     * @param league Liga
     * @param season Temporada
     * @return Datos del jugador con estadísticas
     * @throws ApiConnectivityException Si falla la conectividad
     * @throws RateLimitExceededException Si se alcanza el rate limit
     */
    ApiFootballDtos.PlayerDto getPlayerStats(Integer playerId, String league, Integer season)
            throws ApiConnectivityException, RateLimitExceededException;
}
