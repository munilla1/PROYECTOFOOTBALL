package com.example.football.estadisticas.infrastructure.adapters;

import com.example.football.estadisticas.application.ports.SyncLogsRepositoryPort;
import com.example.football.estadisticas.domain.SyncLog;
import com.example.football.estadisticas.infrastructure.persistence.SyncLogJpaEntity;
import com.example.football.estadisticas.infrastructure.persistence.SyncLogJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador: Implementación de SyncLogsRepositoryPort usando JPA.
 * 
 * Convierte entre la entidad de dominio SyncLog y la entidad JPA SyncLogJpaEntity.
 * Maneja serialización/deserialización JSON de lista de errores.
 */
@Service
public class SyncLogsRepositoryAdapter implements SyncLogsRepositoryPort {

    private final SyncLogJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public SyncLogsRepositoryAdapter(SyncLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Guarda un log de sincronización.
     */
    @Override
    public void save(SyncLog log) {
        if (log == null) {
            throw new IllegalArgumentException("SyncLog no puede ser null");
        }

        // Convertir SyncLog (dominio) → SyncLogJpaEntity (persistencia)
        SyncLogJpaEntity entity = toJpaEntity(log);
        jpaRepository.save(entity);
    }

    /**
     * Obtiene un log por timestamp.
     */
    @Override
    public Optional<SyncLog> findByTimestamp(Instant timestamp) {
        if (timestamp == null) {
            return Optional.empty();
        }

        // Buscar logs en rango de 1 segundo del timestamp exacto
        Instant start = timestamp.minusSeconds(1);
        Instant end = timestamp.plusSeconds(1);

        return jpaRepository.findBySyncTimestampBetween(start, end)
                .stream()
                .findFirst()
                .map(this::toDomainSyncLog);
    }

    /**
     * Obtiene todos los logs con status específico.
     */
    @Override
    public List<SyncLog> findByStatus(String status) {
        if (status == null || status.isBlank()) {
            return List.of();
        }

        return jpaRepository.findByStatus(status)
                .stream()
                .map(this::toDomainSyncLog)
                .toList();
    }

    /**
     * Obtiene logs en un rango de fechas.
     */
    @Override
    public List<SyncLog> findByDateRange(Instant from, Instant to) {
        if (from == null || to == null) {
            return List.of();
        }

        return jpaRepository.findBySyncTimestampBetween(from, to)
                .stream()
                .map(this::toDomainSyncLog)
                .toList();
    }

    /**
     * Obtiene el último log de sincronización exitosa.
     */
    @Override
    public Optional<SyncLog> findLastSync() {
        return jpaRepository.findFirstByStatusOrderBySyncTimestampDesc("SUCCESS")
                .map(this::toDomainSyncLog);
    }

    /**
     * Obtiene el último log de sincronización (exitosa o fallida).
     */
    @Override
    public Optional<SyncLog> findLastSyncAttempt() {
        return jpaRepository.findFirstByOrderBySyncTimestampDesc()
                .map(this::toDomainSyncLog);
    }

    /**
     * Cuenta todos los logs.
     */
    @Override
    public long count() {
        return jpaRepository.count();
    }

    // ========== Conversiones Dominio ↔ JPA ==========

    /**
     * Convierte SyncLog (dominio) a SyncLogJpaEntity (persistencia).
     */
    private SyncLogJpaEntity toJpaEntity(SyncLog log) {
        // Serializar errors a JSON
        String errorsJson = serializeErrors(log.errors());

        return new SyncLogJpaEntity(
                log.id().toString(),
                log.timestamp(),
                log.status(),
                log.league(),
                log.season(),
                log.roundSynced(),
                log.playersUpdated(),
                log.matchesUpdated(),
                log.durationMs(),
                errorsJson,
                Instant.now() // createdAt es ahora
        );
    }

    /**
     * Convierte SyncLogJpaEntity (persistencia) a SyncLog (dominio).
     */
    private SyncLog toDomainSyncLog(SyncLogJpaEntity entity) {
        // Deserializar errors desde JSON
        List<String> errors = deserializeErrors(entity.getErrorsJson());

        // Reconstruir SyncLog
        return new SyncLog(
                java.util.UUID.fromString(entity.getId()),
                entity.getSyncTimestamp(),
                entity.getStatus(),
                entity.getLeague(),
                entity.getSeason(),
                entity.getRoundSynced(),
                entity.getPlayersUpdated(),
                entity.getMatchesUpdated(),
                entity.getDurationMs(),
                errors
        );
    }

    /**
     * Serializa List<String> errors a JSON string.
     */
    private String serializeErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(errors);
        } catch (Exception e) {
            // En caso de error de serialización, retornar array vacío
            return "[]";
        }
    }

    /**
     * Deserializa JSON string a List<String> errors.
     */
    private List<String> deserializeErrors(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception e) {
            // En caso de error de deserialización, retornar lista vacía
            return new ArrayList<>();
        }
    }
}
