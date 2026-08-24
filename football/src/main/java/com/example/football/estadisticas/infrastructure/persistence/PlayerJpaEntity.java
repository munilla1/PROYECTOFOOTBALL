package com.example.football.estadisticas.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidad JPA: Jugador persistido en BD.
 * 
 * Mapea la entidad de dominio Player a la tabla "players" en PostgreSQL.
 * Esta clase es responsable únicamente de persistencia, NO contiene lógica de negocio.
 */
@Entity
@Table(
    name = "players",
    indexes = {
        @Index(name = "idx_external_id", columnList = "external_id", unique = true),
        @Index(name = "idx_team_id", columnList = "team_id"),
        @Index(name = "idx_league_season", columnList = "league,season")
    }
)
public class PlayerJpaEntity {

    @Id
    private String id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    // Estadísticas reales (normalizadas 0-100)
    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "league", nullable = false)
    private String league;

    @Column(name = "appearances", nullable = false)
    private Integer appearances;

    @Column(name = "goals", nullable = false)
    private Integer goals;

    @Column(name = "assists", nullable = false)
    private Integer assists;

    @Column(name = "passes_accuracy", nullable = false)
    private Integer passesAccuracy;

    @Column(name = "dribbles_success", nullable = false)
    private Integer dribblesSuccess;

    @Column(name = "tackles", nullable = false)
    private Integer tackles;

    @Column(name = "performance_score", nullable = false)
    private Integer performanceScore;

    @Column(name = "last_stats_updated")
    private Instant lastStatsUpdated;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructores
    public PlayerJpaEntity() {
    }

    public PlayerJpaEntity(
            String id,
            String externalId,
            String name,
            String position,
            Integer age,
            String nationality,
            String teamId,
            Integer season,
            String league,
            Integer appearances,
            Integer goals,
            Integer assists,
            Integer passesAccuracy,
            Integer dribblesSuccess,
            Integer tackles,
            Integer performanceScore,
            Instant lastStatsUpdated,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.position = position;
        this.age = age;
        this.nationality = nationality;
        this.teamId = teamId;
        this.season = season;
        this.league = league;
        this.appearances = appearances;
        this.goals = goals;
        this.assists = assists;
        this.passesAccuracy = passesAccuracy;
        this.dribblesSuccess = dribblesSuccess;
        this.tackles = tackles;
        this.performanceScore = performanceScore;
        this.lastStatsUpdated = lastStatsUpdated;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public Integer getAppearances() {
        return appearances;
    }

    public void setAppearances(Integer appearances) {
        this.appearances = appearances;
    }

    public Integer getGoals() {
        return goals;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getPassesAccuracy() {
        return passesAccuracy;
    }

    public void setPassesAccuracy(Integer passesAccuracy) {
        this.passesAccuracy = passesAccuracy;
    }

    public Integer getDribblesSuccess() {
        return dribblesSuccess;
    }

    public void setDribblesSuccess(Integer dribblesSuccess) {
        this.dribblesSuccess = dribblesSuccess;
    }

    public Integer getTackles() {
        return tackles;
    }

    public void setTackles(Integer tackles) {
        this.tackles = tackles;
    }

    public Integer getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(Integer performanceScore) {
        this.performanceScore = performanceScore;
    }

    public Instant getLastStatsUpdated() {
        return lastStatsUpdated;
    }

    public void setLastStatsUpdated(Instant lastStatsUpdated) {
        this.lastStatsUpdated = lastStatsUpdated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerJpaEntity that = (PlayerJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
