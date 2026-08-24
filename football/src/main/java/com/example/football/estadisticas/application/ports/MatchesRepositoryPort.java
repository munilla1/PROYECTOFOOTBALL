package com.example.football.estadisticas.application.ports;

import com.example.football.estadisticas.domain.Match;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto: Interfaz de repositorio para Partidos.
 * 
 * Define operaciones de persistencia para la entidad Match.
 * La implementación está en infraestructura.
 */
public interface MatchesRepositoryPort {

    /**
     * Guarda un partido (crea o actualiza).
     */
    void save(Match match);

    /**
     * Obtiene un partido por su ID interno.
     */
    Optional<Match> findById(UUID id);

    /**
     * Obtiene un partido por su ID externo (API-Football).
     */
    Optional<Match> findByFixtureId(String fixtureId);

    /**
     * Obtiene todos los partidos de una jornada.
     */
    List<Match> findByRound(Integer round);

    /**
     * Obtiene todos los partidos de un equipo (como local o visitante).
     */
    List<Match> findByTeam(String teamId);

    /**
     * Obtiene todos los partidos.
     */
    List<Match> findAll();

    /**
     * Elimina un partido.
     */
    void delete(UUID id);

    /**
     * Cuenta cantidad de partidos.
     */
    long count();
}
