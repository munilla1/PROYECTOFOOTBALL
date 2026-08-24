package com.example.football.jornadas.infrastructure.persistence;

import com.example.football.jornadas.domain.JornadaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para JornadaJpaEntity.
 * 
 * Define métodos de consulta personalizados para acceso a jornadas.
 */
@Repository
public interface JornadaJpaRepository extends JpaRepository<JornadaJpaEntity, UUID> {
    /**
     * Busca una jornada por su identidad compuesta.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return Optional con la jornada si existe
     */
    Optional<JornadaJpaEntity> findByLeagueAndSeasonAndRoundNumber(
            String league, Integer season, Integer roundNumber
    );

    /**
     * Obtiene todas las jornadas de una liga en una temporada.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @return lista de jornadas
     */
    List<JornadaJpaEntity> findByLeagueAndSeason(String league, Integer season);

    /**
     * Verifica si existe una jornada con identidad compuesta.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return true si existe
     */
    boolean existsByLeagueAndSeasonAndRoundNumber(
            String league, Integer season, Integer roundNumber
    );

    /**
     * Obtiene jornadas por estado.
     * Útil para finding IN_PROGRESS jornadas para actualización automática.
     * 
     * @param status estado a filtrar
     * @return lista de jornadas con ese estado
     */
    List<JornadaJpaEntity> findByStatus(JornadaStatus status);
}
