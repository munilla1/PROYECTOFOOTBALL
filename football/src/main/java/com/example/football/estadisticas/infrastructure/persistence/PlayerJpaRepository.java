package com.example.football.estadisticas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data Repository para PlayerJpaEntity.
 * 
 * Proporciona métodos CRUD y queries adicionales para acceder a la tabla "players".
 */
@Repository
public interface PlayerJpaRepository extends JpaRepository<PlayerJpaEntity, String> {

    /**
     * Busca un jugador por su ID externo (API-Football).
     */
    Optional<PlayerJpaEntity> findByExternalId(String externalId);

    /**
     * Busca todos los jugadores de un equipo.
     */
    List<PlayerJpaEntity> findByTeamId(String teamId);

    /**
     * Busca jugadores por liga y temporada.
     */
    List<PlayerJpaEntity> findByLeagueAndSeason(String league, Integer season);

    /**
     * Busca jugadores por posición.
     */
    List<PlayerJpaEntity> findByPosition(String position);

    /**
     * Cuenta jugadores de un equipo.
     */
    long countByTeamId(String teamId);

    /**
     * Busca jugadores con performance score mayor a threshold.
     */
    List<PlayerJpaEntity> findByPerformanceScoreGreaterThanEqual(Integer threshold);
}
