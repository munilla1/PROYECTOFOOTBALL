package com.example.football.jornadas.application.services;

import com.example.football.estadisticas.application.ports.SyncLogsRepositoryPort;
import com.example.football.estadisticas.domain.SyncLog;
import com.example.football.jornadas.application.ports.JornadasApiPort;
import com.example.football.jornadas.application.ports.JornadasRepositoryPort;
import com.example.football.jornadas.domain.Jornada;
import com.example.football.jornadas.domain.JornadasApiException;
import com.example.football.jornadas.domain.JornadasException;
import com.example.football.jornadas.domain.SyncResult;
import com.example.football.jornadas.infrastructure.dtos.JornadaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación: SincronizarJornadasService
 * 
 * Orquesta la sincronización inicial de jornadas desde API-Football.
 * Implementa lógica incremental: crea nuevas, actualiza existentes.
 * Registra cada operación en sync_logs para auditoría.
 */
@Service
public class SincronizarJornadasService {
    private static final Logger logger = LoggerFactory.getLogger(SincronizarJornadasService.class);

    private final JornadasApiPort jornadasApiPort;
    private final JornadasRepositoryPort jornadasRepository;
    private final JornadasMapperService mapperService;
    private final SyncLogsRepositoryPort syncLogsRepository;

    public SincronizarJornadasService(JornadasApiPort jornadasApiPort,
                                      JornadasRepositoryPort jornadasRepository,
                                      JornadasMapperService mapperService,
                                      SyncLogsRepositoryPort syncLogsRepository) {
        this.jornadasApiPort = jornadasApiPort;
        this.jornadasRepository = jornadasRepository;
        this.mapperService = mapperService;
        this.syncLogsRepository = syncLogsRepository;
    }

    /**
     * Sincroniza todas las jornadas de una liga en una temporada.
     * 
     * Algoritmo:
     * 1. Obtiene lista de jornadas desde API-Football
     * 2. Para cada jornada:
     *    - Verifica si existe localmente por (league, season, roundNumber)
     *    - Si no existe: inserta nueva jornada
     *    - Si existe: actualiza solo si el estado cambió
     * 3. Registra cada operación en sync_logs
     * 4. Retorna resumen de operaciones
     * 
     * @param league nombre de la liga
     * @param season temporada
     * @return SyncResult con conteos de creadas/actualizadas/errores
     */
    @Transactional
    public SyncResult sincronizarJornadas(String league, Integer season) {
        logger.info("Starting jornadas sync for {} season {}", league, season);
        Instant startTime = Instant.now();

        int created = 0;
        int updated = 0;
        int errors = 0;

        try {
            // Obtiene lista de jornadas desde API
            List<JornadaDto> jornadasFromApi = jornadasApiPort.getJornadas(league, season);
            logger.info("Fetched {} jornadas from API for {} {}", 
                    jornadasFromApi.size(), league, season);

            for (JornadaDto dto : jornadasFromApi) {
                try {
                    // Mapea DTO a entidad de dominio
                    Jornada jornada = mapperService.mapDtoToJornada(dto);

                    // Verifica si ya existe
                    Optional<Jornada> existing = jornadasRepository.findByRound(
                            league, season, jornada.roundNumber()
                    );

                    if (existing.isEmpty()) {
                        // Inserta nueva jornada
                        Jornada saved = jornadasRepository.save(jornada);
                        created++;

                        // Registra en sync_logs
                        recordSyncLog("JORNADA_CREATED", league, season, 
                                jornada.roundNumber(), "SUCCESS", 
                                "Jornada " + jornada.getCompositeId() + " created with status " + jornada.status());

                        logger.debug("Created jornada: {}", jornada.getCompositeId());
                    } else {
                        // Actualiza si el estado cambió
                        Jornada oldJornada = existing.get();
                        if (!oldJornada.status().equals(jornada.status()) 
                            || !oldJornada.matchCount().equals(jornada.matchCount())) {
                            
                            // Crea nueva jornada con ID existente para preservar referencias
                            Jornada updated_jornada = new Jornada(
                                    oldJornada.id(),
                                    jornada.roundNumber(),
                                    jornada.league(),
                                    jornada.season(),
                                    jornada.status(),
                                    jornada.matchCount(),
                                    oldJornada.createdAt(),
                                    Instant.now()
                            );
                            
                            jornadasRepository.update(updated_jornada);
                            updated++;

                            // Registra en sync_logs
                            recordSyncLog("JORNADA_UPDATED", league, season, 
                                    jornada.roundNumber(), "SUCCESS",
                                    String.format("Status: %s -> %s, Matches: %d -> %d",
                                            oldJornada.status(), jornada.status(),
                                            oldJornada.matchCount(), jornada.matchCount()));

                            logger.debug("Updated jornada: {} status {} -> {}", 
                                    jornada.getCompositeId(), oldJornada.status(), jornada.status());
                        }
                    }
                } catch (JornadasException e) {
                    errors++;
                    recordSyncLog("JORNADA_ERROR", league, season, 
                            0, "FAILURE", "Mapping error: " + e.getMessage());
                    logger.error("Failed to sync jornada DTO: {}", dto, e);
                } catch (Exception e) {
                    errors++;
                    recordSyncLog("JORNADA_ERROR", league, season, 
                            0, "FAILURE", "Unexpected error: " + e.getMessage());
                    logger.error("Unexpected error syncing jornada", e);
                }
            }
        } catch (JornadasApiException e) {
            errors++;
            recordSyncLog("JORNADA_API_ERROR", league, season, 
                    0, "FAILURE", "API connectivity error: " + e.getMessage());
            logger.error("Failed to fetch jornadas from API for {} {}", league, season, e);
            throw new JornadasException("Failed to sync jornadas: " + e.getMessage(), e);
        }

        long durationMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
        SyncResult result = new SyncResult(created, updated, errors, durationMs);

        logger.info("Jornadas sync completed for {} season {}: {} created, {} updated, {} errors in {}ms",
                league, season, created, updated, errors, durationMs);

        return result;
    }

    /**
     * Registra una operación de sincronización en sync_logs para auditoría.
     */
    private void recordSyncLog(String action, String league, Integer season, 
                               Integer roundNumber, String status, String details) {
        try {
            // Usa la estructura de SyncLog existente
            SyncLog log = "SUCCESS".equals(status) 
                ? SyncLog.exitosa(league, season, roundNumber, 0, 0, 0L)
                : SyncLog.fallida(league, season, roundNumber, 
                        java.util.List.of(details), 0L);
            syncLogsRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to record sync log: {}", action, e);
            // No lanzo excepción para no interrumpir sincronización
        }
    }
}
