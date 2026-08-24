package com.example.football.jornadas.application.ports;

import com.example.football.jornadas.infrastructure.dtos.JornadaDto;
import java.util.List;

/**
 * Puerto de aplicación: JornadasApiPort
 * 
 * Define el contrato para consumir datos de jornadas desde API-Football.
 * Implementada por JornadasApiClientAdapter en infraestructura.
 */
public interface JornadasApiPort {
    /**
     * Obtiene la lista de jornadas para una liga y temporada específica.
     * 
     * @param league nombre de la liga (ej: "LaLiga", "Premier League")
     * @param season año de la temporada
     * @return lista de DTOs de jornadas
     * @throws JornadasApiException si falla la conexión con API-Football
     */
    List<JornadaDto> getJornadas(String league, Integer season);

    /**
     * Obtiene el estado actual de una jornada específica.
     * 
     * @param league nombre de la liga
     * @param season año de la temporada
     * @param round número de ronda
     * @return DTO con estado actual de la jornada
     * @throws JornadasApiException si falla la conexión
     */
    JornadaDto getJornadaStatus(String league, Integer season, Integer round);
}
