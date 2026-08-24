package com.example.football.estadisticas.application.services;

import com.example.football.estadisticas.domain.Match;
import com.example.football.estadisticas.domain.PlayerMatchStats;
import com.example.football.estadisticas.domain.Score;
import com.example.football.estadisticas.infrastructure.dtos.ApiFootballDtos;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio: Mapea DTOs de API-Football a entidades de dominio Match.
 * 
 * Responsabilidades:
 * - Convertir FixtureDto (API) → Match (dominio)
 * - Normalizar datos de partidos
 * - Extraer y estructurar estadísticas de jugadores
 * - Validar integridad durante mapeo
 */
@Service
public class MatchMapperService {

    /**
     * Mapea un FixtureDto de API-Football a una entidad Match del dominio.
     */
    public Match mapFromApiDto(ApiFootballDtos.FixtureDto apiDto, String league, Integer season, String round) {
        if (apiDto == null || apiDto.fixture() == null) {
            throw new MatchMappingException("FixtureDto o fixture data es null");
        }

        if (apiDto.teams() == null || apiDto.goals() == null) {
            throw new MatchMappingException("Teams o Goals data está vacía");
        }

        ApiFootballDtos.FixtureData fixture = apiDto.fixture();
        ApiFootballDtos.TeamsData teams = apiDto.teams();
        ApiFootballDtos.ScoreData scoreData = apiDto.score();

        // Validar equipos
        if (teams.home() == null || teams.away() == null) {
            throw new MatchMappingException("Datos de equipos incompletos");
        }

        // Crear Score
        Score score = mapScore(scoreData);

        // Determinar estado del partido
        String status = normalizeFixtureStatus(fixture.status());

        // Mapear estadísticas de jugadores
        List<PlayerMatchStats> playerStats = mapPlayerStats(apiDto.events());

        // Crear Match
        return Match.nueva(
                fixture.id().toString(),
                Integer.parseInt(round),
                league,
                season,
                Instant.ofEpochSecond(fixture.timestamp() != null ? fixture.timestamp() : System.currentTimeMillis() / 1000),
                teams.home().id().toString(),
                teams.away().id().toString(),
                score,
                status,
                playerStats
        );
    }

    /**
     * Mapea el score del partido.
     */
    private Score mapScore(ApiFootballDtos.ScoreData scoreData) {
        if (scoreData == null) {
            return new Score(0, 0);
        }

        Integer homeGoals = scoreData.fulltime() != null ? scoreData.fulltime() : 0;
        Integer awayGoals = scoreData.fulltime() != null ? scoreData.fulltime() : 0;

        // Si no hay fulltime, usar ET (extratime) si existe
        if ((homeGoals == null || homeGoals == 0) && scoreData.extratime() != null) {
            homeGoals = scoreData.extratime();
        }
        if ((awayGoals == null || awayGoals == 0) && scoreData.extratime() != null) {
            awayGoals = scoreData.extratime();
        }

        // Si aún es null, usar 0
        homeGoals = homeGoals != null ? homeGoals : 0;
        awayGoals = awayGoals != null ? awayGoals : 0;

        return new Score(homeGoals, awayGoals);
    }

    /**
     * Normaliza el status del fixture a formato estándar.
     */
    private String normalizeFixtureStatus(String apiStatus) {
        if (apiStatus == null || apiStatus.isBlank()) {
            return "Not Started";
        }

        // Mapear status de API-Football a valores estándar
        return switch (apiStatus.toUpperCase()) {
            case "NOTSTARTED", "NS" -> "Not Started";
            case "INPLAY", "1H", "2H" -> "In Play";
            case "FINISHED", "PST" -> "Match Finished";
            case "POSTPONED", "PPD" -> "Postponed";
            case "CANCELLED", "CANC" -> "Cancelled";
            case "SUSPENDED", "SUSP" -> "Suspended";
            default -> apiStatus;
        };
    }

    /**
     * Mapea los eventos del partido a estadísticas de jugadores.
     */
    private List<PlayerMatchStats> mapPlayerStats(List<ApiFootballDtos.EventData> events) {
        List<PlayerMatchStats> playerStats = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return playerStats;
        }

        // Agrupar eventos por jugador para agregar stats
        java.util.Map<Integer, List<ApiFootballDtos.EventData>> eventsByPlayer = new java.util.HashMap<>();
        for (ApiFootballDtos.EventData event : events) {
            if (event.player_id() != null) {
                eventsByPlayer.computeIfAbsent(event.player_id(), k -> new ArrayList<>()).add(event);
            }
        }

        // Convertir a PlayerMatchStats
        for (var entry : eventsByPlayer.entrySet()) {
            Integer playerId = entry.getKey();
            List<ApiFootballDtos.EventData> playerEvents = entry.getValue();

            if (playerEvents.isEmpty()) continue;

            ApiFootballDtos.EventData firstEvent = playerEvents.get(0);
            String playerName = firstEvent.player_name() != null ? firstEvent.player_name() : "Unknown";
            String team = firstEvent.team() != null ? firstEvent.team() : "Unknown";

            // Contar goles y asistencias
            int goals = 0;
            int assists = 0;
            for (ApiFootballDtos.EventData e : playerEvents) {
                if ("Goal".equalsIgnoreCase(e.type())) {
                    goals++;
                } else if ("Assist".equalsIgnoreCase(e.type())) {
                    assists++;
                }
            }

            // Estimaciones (desde API no siempre vienen minutos y rating)
            Integer minutesPlayed = estimateMinutesPlayed(playerEvents);
            Double rating = estimateRating(goals, assists, playerEvents.size());

            playerStats.add(new PlayerMatchStats(
                    playerId.toString(),
                    playerName,
                    team,
                    goals,
                    assists,
                    minutesPlayed,
                    rating
            ));
        }

        return playerStats;
    }

    /**
     * Estima minutos jugados basado en eventos (heurística simple).
     */
    private Integer estimateMinutesPlayed(List<ApiFootballDtos.EventData> playerEvents) {
        if (playerEvents.isEmpty()) {
            return 0;
        }

        // Si hay eventos del jugador, asumimos que jugó la mayoría del partido
        int maxMinute = 0;
        for (ApiFootballDtos.EventData event : playerEvents) {
            if (event.minute() != null && event.minute() > maxMinute) {
                maxMinute = event.minute();
            }
        }

        // Si el último evento es después de minuto 45, jugó todo o casi todo
        return maxMinute > 0 ? maxMinute : 90;
    }

    /**
     * Estima el rating del jugador (0-10) basado en eventos.
     */
    private Double estimateRating(int goals, int assists, int totalEvents) {
        // Heurística simple: base 5.0 + bonus por goles/asistencias
        double baseRating = 5.0;
        baseRating += (goals * 2.0);      // +2 por cada gol
        baseRating += (assists * 1.5);    // +1.5 por cada asistencia
        baseRating += Math.min(totalEvents / 5.0, 3.0); // +hasta 3.0 por participación

        return Math.min(baseRating, 10.0);
    }
}

/**
 * Excepción lanzada cuando el mapeo de Match falla.
 */
class MatchMappingException extends RuntimeException {
    public MatchMappingException(String message) {
        super(message);
    }

    public MatchMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
