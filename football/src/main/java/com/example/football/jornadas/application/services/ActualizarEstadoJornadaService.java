package com.example.football.jornadas.application.services;

import com.example.football.estadisticas.application.ports.SyncLogsRepositoryPort;
import com.example.football.estadisticas.domain.SyncLog;
import com.example.football.jornadas.application.ports.JornadasApiPort;
import com.example.football.jornadas.application.ports.JornadasRepositoryPort;
import com.example.football.jornadas.domain.Jornada;
import com.example.football.jornadas.domain.JornadaStatus;
import com.example.football.jornadas.domain.JornadasException;
import com.example.football.jornadas.domain.UpdateResult;
import com.example.football.jornadas.infrastructure.dtos.JornadaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de aplicación: ActualizarEstadoJornadaService
 * 
 * Detecta y persiste cambios de estado de jornadas sincronizadas.
 * Detecta transiciones: NOT_STARTED → IN_PROGRESS → FINISHED
 * Maneja también estado POSTPONED.
 * Registra cambios en sync_logs para auditoría.
 */
@Service
public class ActualizarEstadoJornadaService {
    private static final Logger logger = LoggerFactory.getLogger(ActualizarEstadoJornadaService.class);

    private final JornadasApiPort jornadasApiPort;
    private final JornadasRepositoryPort jornadasRepository;
    private final JornadasMapperService mapperService;
    private final SyncLogsRepositoryPort syncLogsRepository;

    public ActualizarEstadoJornadaService(JornadasApiPort jornadasApiPort,
                                          JornadasRepositoryPort jornadasRepository,
                                          JornadasMapperService mapperService,
                                          SyncLogsRepositoryPort syncLogsRepository) {
        this.jornadasApiPort = jornadasApiPort;
        this.jornadasRepository = jornadasRepository;
        this.mapperService = mapperService;
        this.syncLogsRepository = syncLogsRepository;
    }

    /**
     * Actualiza estados de todas las jornadas proporcionadas.
     * 
     * Algoritmo:
     * 1. Para cada jornada existente:
     *    - Obtiene estado actual de API-Football
     *    - Compara con estado local
     *    - Si cambió: persiste nuevo estado y registra en sync_logs
     *    - Si no cambió: omite (no escribe)
     * 2. Maneja errores de API sin interrumpir el proceso
     * 3. Retorna estadísticas de operación
     * 
     * @param jornadas lista de jornadas a verificar
     * @return UpdateResult con estadísticas de cambios
     */
    @Transactional
    public UpdateResult actualizarEstados(List<Jornada> jornadas) {
        logger.info("Starting estado update for {} jornadas", jornadas.size());
        Instant startTime = Instant.now();

        int totalProcessed = 0;
        int statusChanged = 0;
        int noChange = 0;
        int errors = 0;

        for (Jornada jornada : jornadas) {
            totalProcessed++;
            try {
                // Obtiene estado actual de API
                JornadaDto currentDto = jornadasApiPort.getJornadaStatus(
                        jornada.league(),
                        jornada.season(),
                        jornada.roundNumber()
                );

                if (currentDto == null) {
                    logger.warn("No DTO returned from API for {}", jornada.getCompositeId());
                    errors++;
                    continue;
                }

                // Mapea DTO a dominio para obtener estado
                Jornada currentJornada = mapperService.mapDtoToJornada(currentDto);

                // Compara estados
                if (!jornada.status().equals(currentJornada.status())) {
                    // Cambio detectado
                    JornadaStatus oldStatus = jornada.status();
                    JornadaStatus newStatus = currentJornada.status();

                    // Crea jornada actualizada preservando ID original
                    Jornada updated = new Jornada(
                            jornada.id(),
                            jornada.roundNumber(),
                            jornada.league(),
                            jornada.season(),
                            newStatus,
                            currentJornada.matchCount(),
                            jornada.createdAt(),
                            Instant.now()
                    );

                    // Persiste cambio
                    jornadasRepository.update(updated);
                    statusChanged++;

                    // Registra en sync_logs
                    recordStatusChange(jornada.getCompositeId(), oldStatus, newStatus,
                            jornada.matchCount(), currentJornada.matchCount());

                    logger.info("Status changed for {}: {} -> {}", 
                            jornada.getCompositeId(), oldStatus, newStatus);
                } else {
                    // Sin cambios
                    noChange++;
                    logger.debug("No status change for {}", jornada.getCompositeId());
                }
            } catch (JornadasException e) {
                errors++;
                recordStatusError(jornada.getCompositeId(), "Domain error: " + e.getMessage());
                logger.error("Domain error updating status for {}", jornada.getCompositeId(), e);
            } catch (Exception e) {
                errors++;
                recordStatusError(jornada.getCompositeId(), "Unexpected error: " + e.getMessage());
                logger.error("Unexpected error updating status for {}", jornada.getCompositeId(), e);
            }
        }

        long durationMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
        UpdateResult result = new UpdateResult(totalProcessed, statusChanged, noChange, errors, durationMs);

        logger.info("Estado update completed: {} processed, {} changed, {} no change, {} errors in {}ms",
                totalProcessed, statusChanged, noChange, errors, durationMs);

        return result;
    }

    /**
     * Registra un cambio de estado en sync_logs.
     */
    private void recordStatusChange(String jornadaId, JornadaStatus oldStatus, 
                                    JornadaStatus newStatus, Integer oldMatchCount, 
                                    Integer newMatchCount) {
        try {
            // Usa la estructura de SyncLog existente
            SyncLog log = SyncLog.exitosa(
                    "Jornada",
                    newStatus.ordinal(),  // Temporada como número
                    1,  // Ronda
                    1,  // Matches updated
                    0,  // Duracion
                    0L
            );
            syncLogsRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to record status change log for {}", jornadaId, e);
        }
    }

    /**
     * Registra un error en actualización de estado.
     */
    private void recordStatusError(String jornadaId, String errorMessage) {
        try {
            // Usa la estructura de SyncLog existente
            SyncLog log = SyncLog.fallida(
                    "Jornada",
                    2024,  // Temporada default
                    0,     // Ronda
                    java.util.List.of(errorMessage),
                    0L
            );
            syncLogsRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to record status error log for {}", jornadaId, e);
        }
    }
}
