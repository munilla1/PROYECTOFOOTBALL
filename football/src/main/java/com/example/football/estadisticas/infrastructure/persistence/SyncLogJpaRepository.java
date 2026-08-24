package com.example.football.estadisticas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data Repository para SyncLogJpaEntity.
 * 
 * Proporciona métodos CRUD y queries adicionales para acceder a la tabla "sync_logs".
 */
@Repository
public interface SyncLogJpaRepository extends JpaRepository<SyncLogJpaEntity, String> {

    /**
     * Busca logs con un status específico.
     */
    List<SyncLogJpaEntity> findByStatus(String status);

    /**
     * Busca logs en un rango de fechas.
     */
    List<SyncLogJpaEntity> findBySyncTimestampBetween(Instant from, Instant to);

    /**
     * Busca logs por liga y temporada.
     */
    List<SyncLogJpaEntity> findByLeagueAndSeason(String league, Integer season);

    /**
     * Busca el último log de sincronización exitosa.
     */
    Optional<SyncLogJpaEntity> findFirstByStatusOrderBySyncTimestampDesc(String status);

    /**
     * Busca el último log (cualquier status), ordenado por timestamp descendente.
     */
    Optional<SyncLogJpaEntity> findFirstByOrderBySyncTimestampDesc();

    /**
     * Cuenta logs por status.
     */
    long countByStatus(String status);

    /**
     * Busca logs de una jornada específica.
     */
    List<SyncLogJpaEntity> findByRoundSynced(Integer round);
}
