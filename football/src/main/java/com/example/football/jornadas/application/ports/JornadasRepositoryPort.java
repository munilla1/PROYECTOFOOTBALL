package com.example.football.jornadas.application.ports;

import com.example.football.jornadas.domain.Jornada;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de aplicación: JornadasRepositoryPort
 * 
 * Define el contrato para persistencia de jornadas en la base de datos.
 * Implementada por JornadasRepositoryAdapter en infraestructura.
 */
public interface JornadasRepositoryPort {
    /**
     * Persiste una nueva jornada en la base de datos.
     * 
     * @param jornada entidad a guardar
     * @return jornada guardada con ID asignado
     */
    Jornada save(Jornada jornada);

    /**
     * Actualiza una jornada existente.
     * Actualiza automáticamente el timestamp de synchronizedAt.
     * 
     * @param jornada entidad a actualizar
     * @return jornada actualizada
     */
    Jornada update(Jornada jornada);

    /**
     * Busca una jornada por su identidad compuesta (league, season, roundNumber).
     * 
     * @param league nombre de la liga
     * @param season año de la temporada
     * @param roundNumber número de ronda
     * @return Optional con la jornada si existe
     */
    Optional<Jornada> findByRound(String league, Integer season, Integer roundNumber);

    /**
     * Obtiene todas las jornadas de una liga en una temporada específica.
     * 
     * @param league nombre de la liga
     * @param season año de la temporada
     * @return lista de jornadas, vacía si no hay
     */
    List<Jornada> findAllByLeagueAndSeason(String league, Integer season);

    /**
     * Verifica si existe una jornada con la identidad compuesta.
     * 
     * @param league nombre de la liga
     * @param season año de la temporada
     * @param roundNumber número de ronda
     * @return true si existe
     */
    boolean existsByRound(String league, Integer season, Integer roundNumber);

    /**
     * Busca una jornada específica por todos sus identificadores.
     * 
     * @param league nombre de la liga
     * @param season año de la temporada
     * @param roundNumber número de ronda
     * @param jornadaId UUID único de la jornada
     * @return Optional con la jornada si existe
     */
    Optional<Jornada> findByRoundAndId(String league, Integer season, Integer roundNumber, java.util.UUID jornadaId);
}
