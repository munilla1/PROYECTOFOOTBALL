package com.example.football.estadisticas.application.ports;

import com.example.football.estadisticas.domain.SyncLog;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Puerto: Interfaz de repositorio para Logs de Sincronización.
 * 
 * Define operaciones de persistencia para la entidad SyncLog.
 * Usada para auditoría y recuperación ante fallos.
 */
public interface SyncLogsRepositoryPort {

    /**
     * Guarda un log de sincronización.
     */
    void save(SyncLog log);

    /**
     * Obtiene un log por timestamp.
     */
    Optional<SyncLog> findByTimestamp(Instant timestamp);

    /**
     * Obtiene todos los logs con status específico.
     */
    List<SyncLog> findByStatus(String status);

    /**
     * Obtiene logs en un rango de fechas.
     */
    List<SyncLog> findByDateRange(Instant from, Instant to);

    /**
     * Obtiene el último log de sincronización exitosa.
     */
    Optional<SyncLog> findLastSync();

    /**
     * Obtiene el último log de sincronización (exitosa o fallida).
     */
    Optional<SyncLog> findLastSyncAttempt();

    /**
     * Cuenta todos los logs.
     */
    long count();
}
