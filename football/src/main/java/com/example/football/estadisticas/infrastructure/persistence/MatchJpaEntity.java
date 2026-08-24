package com.example.football.estadisticas.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidad JPA: Partido persistido en BD.
 * 
 * Mapea la entidad de dominio Match a la tabla "matches" en PostgreSQL.
 * Almacena información básica del partido; estadísticas de jugadores en tabla aparte.
 */
@Entity
@Table(
    name = "matches",
    indexes = {
        @Index(name = "idx_fixture_id", columnList = "fixture_id", unique = true),
        @Index(name = "idx_round", columnList = "round"),
        @Index(name = "idx_home_team", columnList = "home_team_id"),
        @Index(name = "idx_away_team", columnList = "away_team_id"),
        @Index(name = "idx_league_season", columnList = "league,season")
    }
)
public class MatchJpaEntity {

    @Id
    private String id;

    @Column(name = "fixture_id", nullable = false, unique = true)
    private String fixtureId;

    @Column(name = "round", nullable = false)
    private Integer round;

    @Column(name = "league", nullable = false)
    private String league;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "match_date", nullable = false)
    private Instant matchDate;

    @Column(name = "home_team_id", nullable = false)
    private String homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    private String awayTeamId;

    @Column(name = "home_goals", nullable = false)
    private Integer homeGoals;

    @Column(name = "away_goals", nullable = false)
    private Integer awayGoals;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "player_stats_json")
    private String playerStatsJson; // JSON serializado de List<PlayerMatchStats>

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructores
    public MatchJpaEntity() {
    }

    public MatchJpaEntity(
            String id,
            String fixtureId,
            Integer round,
            String league,
            Integer season,
            Instant matchDate,
            String homeTeamId,
            String awayTeamId,
            Integer homeGoals,
            Integer awayGoals,
            String status,
            String playerStatsJson,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.fixtureId = fixtureId;
        this.round = round;
        this.league = league;
        this.season = season;
        this.matchDate = matchDate;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.status = status;
        this.playerStatsJson = playerStatsJson;
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

    public String getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(String fixtureId) {
        this.fixtureId = fixtureId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
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

    public Instant getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Instant matchDate) {
        this.matchDate = matchDate;
    }

    public String getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(String homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public String getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(String awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlayerStatsJson() {
        return playerStatsJson;
    }

    public void setPlayerStatsJson(String playerStatsJson) {
        this.playerStatsJson = playerStatsJson;
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
        MatchJpaEntity that = (MatchJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
