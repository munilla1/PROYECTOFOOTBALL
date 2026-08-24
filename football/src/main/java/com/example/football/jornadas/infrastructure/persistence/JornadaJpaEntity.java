package com.example.football.jornadas.infrastructure.persistence;

import com.example.football.jornadas.domain.JornadaStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA: JornadaJpaEntity
 * 
 * Mapeo de persistencia para jornadas en PostgreSQL.
 * Tabla: jornadas
 * 
 * Índices:
 * - UNIQUE(league, season, round_number): Identidad compuesta
 * - INDEX(league, season): Búsquedas por liga y temporada
 * - INDEX(status): Filtros por estado
 */
@Entity
@Table(
    name = "jornadas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_jornada_league_season_round",
            columnNames = {"league", "season", "round_number"}
        )
    },
    indexes = {
        @Index(name = "idx_jornada_league_season", columnList = "league, season"),
        @Index(name = "idx_jornada_status", columnList = "status")
    }
)
public class JornadaJpaEntity {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "league", nullable = false, length = 50)
    private String league;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private JornadaStatus status;

    @Column(name = "match_count", nullable = false)
    private Integer matchCount;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @Column(name = "synchronized_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant synchronizedAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private Instant updatedAt;

    // Constructores
    public JornadaJpaEntity() {}

    public JornadaJpaEntity(UUID id, Integer roundNumber, String league, Integer season,
                           JornadaStatus status, Integer matchCount, Instant createdAt,
                           Instant synchronizedAt) {
        this.id = id;
        this.roundNumber = roundNumber;
        this.league = league;
        this.season = season;
        this.status = status;
        this.matchCount = matchCount;
        this.createdAt = createdAt;
        this.synchronizedAt = synchronizedAt;
        this.updatedAt = Instant.now();
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
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

    public JornadaStatus getStatus() {
        return status;
    }

    public void setStatus(JornadaStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Integer getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(Integer matchCount) {
        this.matchCount = matchCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getSynchronizedAt() {
        return synchronizedAt;
    }

    public void setSynchronizedAt(Instant synchronizedAt) {
        this.synchronizedAt = synchronizedAt;
        this.updatedAt = Instant.now();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
