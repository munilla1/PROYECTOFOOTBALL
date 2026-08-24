package com.example.football.estadisticas.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTOs para consumir API-Football.
 * Estos objetos representan la estructura JSON exacta que retorna API-Football.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiFootballDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerDto(
            PlayerData player,
            List<PlayerStatsData> statistics) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerData(
            Integer id,
            String name,
            String firstname,
            String lastname,
            Integer age,
            String birth,
            String nationality,
            String height,
            String weight) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerStatsData(
            TeamData team,
            LeagueData league,
            GamesData games,
            GoalsData goals,
            PassesData passes,
            Integer tackles,
            DribblesData dribbles,
            FoulsData fouls) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamData(
            Integer id,
            String name,
            String logo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueData(
            Integer id,
            String name,
            Integer season) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GamesData(
            Integer appearances,
            Integer lineups,
            Integer minutes,
            Integer number) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalsData(
            Integer total,
            Integer conceded,
            Integer assists,
            Integer saves) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PassesData(
            Integer total,
            Integer key,
            Integer accuracy) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DribblesData(
            Integer attempts,
            Integer success,
            Integer past) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FoulsData(
            Integer committed,
            Integer drawn) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureDto(
            FixtureData fixture,
            TeamsData teams,
            GoalsData goals,
            ScoreData score,
            List<EventData> events) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureData(
            Integer id,
            String date,
            Long timestamp,
            String timezone,
            String status,
            String statusShort) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamsData(
            TeamData home,
            TeamData away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScoreData(
            Integer halftime,
            Integer fulltime,
            Integer extratime,
            Integer penalty) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventData(
            Integer minute,
            String type,
            String team,
            Integer player_id,
            String player_name,
            String detail) {
    }
}
