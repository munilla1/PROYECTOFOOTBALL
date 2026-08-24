package com.example.football.estadisticas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data Repository para MatchJpaEntity.
 * 
 * Proporciona métodos CRUD y queries adicionales para acceder a la tabla "matches".
 */
@Repository
public interface MatchJpaRepository extends JpaRepository<MatchJpaEntity, String> {

    /**
     * Busca un partido por su ID externo (fixture_id de API-Football).
     */
    Optional<MatchJpaEntity> findByFixtureId(String fixtureId);

    /**
     * Busca todos los partidos de una jornada.
     */
    List<MatchJpaEntity> findByRound(Integer round);

    /**
     * Busca todos los partidos de un equipo (como local o visitante).
     */
    List<MatchJpaEntity> findByHomeTeamIdOrAwayTeamId(String homeTeamId, String awayTeamId);

    /**
     * Busca partidos por liga y temporada.
     */
    List<MatchJpaEntity> findByLeagueAndSeason(String league, Integer season);

    /**
     * Busca partidos en un rango de fechas.
     */
    List<MatchJpaEntity> findByMatchDateBetween(Instant startDate, Instant endDate);

    /**
     * Busca partidos con un status específico.
     */
    List<MatchJpaEntity> findByStatus(String status);

    /**
     * Cuenta partidos de una jornada.
     */
    long countByRound(Integer round);

    /**
     * Busca el último partido de un equipo (por fecha).
     */
    Optional<MatchJpaEntity> findFirstByHomeTeamIdOrAwayTeamIdOrderByMatchDateDesc(String homeTeamId, String awayTeamId);
}
