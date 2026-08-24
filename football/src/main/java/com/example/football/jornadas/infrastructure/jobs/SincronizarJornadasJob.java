package com.example.football.jornadas.infrastructure.jobs;

import com.example.football.jornadas.application.services.ActualizarEstadoJornadaService;
import com.example.football.jornadas.application.services.SincronizarJornadasService;
import com.example.football.jornadas.domain.SyncResult;
import com.example.football.jornadas.domain.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Job programado: SincronizarJornadasJob
 * 
 * Ejecuta diariamente a las 03:00 UTC sincronización de jornadas desde API-Football.
 * Itera sobre ligas configuradas y realiza:
 * 1. Sincronización inicial de jornadas nuevas y actualizaciones
 * 2. Detección de cambios de estado
 * 
 * Se puede desactivar mediante propiedad: jornadas.sync.enabled=false
 */
@Service
@ConditionalOnProperty(
    name = "jornadas.sync.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class SincronizarJornadasJob {
    private static final Logger logger = LoggerFactory.getLogger(SincronizarJornadasJob.class);
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

    private final SincronizarJornadasService sincronizarService;
    private final ActualizarEstadoJornadaService actualizarEstadoService;
    private final JornadasJobConfiguration config;

    public SincronizarJornadasJob(SincronizarJornadasService sincronizarService,
                                  ActualizarEstadoJornadaService actualizarEstadoService,
                                  JornadasJobConfiguration config) {
        this.sincronizarService = sincronizarService;
        this.actualizarEstadoService = actualizarEstadoService;
        this.config = config;
    }

    /**
     * Ejecuta sincronización de jornadas diaria.
     * 
     * Cron: 0 0 3 * * * = Todos los días a las 03:00 UTC
     * Se puede configurar mediante: jornadas.sync.cron
     * 
     * Algoritmo:
     * 1. Itera sobre ligas configuradas
     * 2. Para cada liga + temporada actual:
     *    - Sincroniza jornadas (nuevas + updates)
     *    - Actualiza estados
     * 3. Registra resultados
     * 4. No lanza excepciones (no interrumpe otros jobs)
     */
    @Scheduled(cron = "${jornadas.sync.cron:0 0 3 * * *}")
    public void ejecutarSincronizacion() {
        Instant startTime = Instant.now();
        String timestamp = FORMATTER.format(startTime);
        logger.info("=== Starting Jornadas Sync Job at {} ===", timestamp);

        try {
            Integer seasonActual = config.getCurrentSeason();
            String[] ligas = config.getLeaguesToSync();

            logger.info("Syncing {} leagues for season {}", ligas.length, seasonActual);

            int totalCreated = 0;
            int totalUpdated = 0;
            int totalErrors = 0;

            for (String liga : ligas) {
                try {
                    logger.info("Processing league: {}", liga);

                    // Fase 1: Sincroniza jornadas
                    SyncResult syncResult = sincronizarService.sincronizarJornadas(liga, seasonActual);
                    totalCreated += syncResult.created();
                    totalUpdated += syncResult.updated();
                    totalErrors += syncResult.errors();

                    logger.info("League {} sync result: {} created, {} updated, {} errors in {}ms",
                            liga, syncResult.created(), syncResult.updated(), 
                            syncResult.errors(), syncResult.durationMs());

                    // Fase 2: Actualiza estados
                    // Nota: En una implementación completa, obtendríamos todas las jornadas
                    // de BD y llamaríamos a actualizarEstadoService
                    logger.debug("Estado update phase for league {} would happen here", liga);

                } catch (Exception e) {
                    totalErrors++;
                    logger.error("Failed to sync league: {}", liga, e);
                    // Continúa con siguientes ligas
                }
            }

            Instant endTime = Instant.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            String endTimestamp = FORMATTER.format(endTime);

            logger.info("=== Jornadas Sync Job completed at {} ===", endTimestamp);
            logger.info("Total results: {} created, {} updated, {} errors in {}ms",
                    totalCreated, totalUpdated, totalErrors, durationMs);

        } catch (Exception e) {
            logger.error("Unexpected error in SincronizarJornadasJob", e);
            // No lanza excepción para no interrumpir scheduler
        }
    }
}
