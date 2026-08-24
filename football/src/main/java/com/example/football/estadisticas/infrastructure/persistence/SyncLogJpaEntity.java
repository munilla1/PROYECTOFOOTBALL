package com.example.football.estadisticas.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidad JPA: Log de Sincronización persistido en BD.
 * 
 * Mapea la entidad de dominio SyncLog a la tabla "sync_logs" en PostgreSQL.
 * Proporciona auditoría y recuperación ante fallos de sincronización.
 */
@Entity
@Table(
    name = "sync_logs",
    indexes = {
        @Index(name = "idx_sync_timestamp", columnList = "sync_timestamp"),
        @Index(name = "idx_sync_status", columnList = "status"),
        @Index(name = "idx_sync_league_season", columnList = "league,season")
    }
)
public class SyncLogJpaEntity {

    @Id
    private String id;

    @Column(name = "sync_timestamp", nullable = false)
    private Instant syncTimestamp;

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILED, PARTIAL

    @Column(name = "league", nullable = false)
    private String league;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "round_synced", nullable = false)
    private Integer roundSynced;

    @Column(name = "players_updated", nullable = false)
    private Integer playersUpdated;

    @Column(name = "matches_updated", nullable = false)
    private Integer matchesUpdated;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "errors_json")
    private String errorsJson; // JSON serializado de List<String>

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Constructores
    public SyncLogJpaEntity() {
    }

    public SyncLogJpaEntity(
            String id,
            Instant syncTimestamp,
            String status,
            String league,
            Integer season,
            Integer roundSynced,
            Integer playersUpdated,
            Integer matchesUpdated,
            Long durationMs,
            String errorsJson,
            Instant createdAt) {
        this.id = id;
        this.syncTimestamp = syncTimestamp;
        this.status = status;
        this.league = league;
        this.season = season;
        this.roundSynced = roundSynced;
        this.playersUpdated = playersUpdated;
        this.matchesUpdated = matchesUpdated;
        this.durationMs = durationMs;
        this.errorsJson = errorsJson;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getSyncTimestamp() {
        return syncTimestamp;
    }

    public void setSyncTimestamp(Instant syncTimestamp) {
        this.syncTimestamp = syncTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getRoundSynced() {
        return roundSynced;
    }

    public void setRoundSynced(Integer roundSynced) {
        this.roundSynced = roundSynced;
    }

    public Integer getPlayersUpdated() {
        return playersUpdated;
    }

    public void setPlayersUpdated(Integer playersUpdated) {
        this.playersUpdated = playersUpdated;
    }

    public Integer getMatchesUpdated() {
        return matchesUpdated;
    }

    public void setMatchesUpdated(Integer matchesUpdated) {
        this.matchesUpdated = matchesUpdated;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorsJson() {
        return errorsJson;
    }

    public void setErrorsJson(String errorsJson) {
        this.errorsJson = errorsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncLogJpaEntity that = (SyncLogJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
