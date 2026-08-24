package com.example.football.estadisticas.application.services;

import com.example.football.estadisticas.domain.Player;
import com.example.football.estadisticas.domain.RealStats;
import com.example.football.estadisticas.infrastructure.dtos.ApiFootballDtos;
import org.springframework.stereotype.Service;

/**
 * Servicio: Mapea DTOs de API-Football a entidades de dominio Player.
 * 
 * Responsabilidades:
 * - Convertir PlayerDto (API) → Player (dominio)
 * - Normalizar datos
 * - Validar integridad durante mapeo
 */
@Service
public class PlayerMapperService {

    /**
     * Mapea un PlayerDto de API-Football a una entidad Player del dominio.
     */
    public Player mapFromApiDto(ApiFootballDtos.PlayerDto apiDto, String teamId) {
        if (apiDto == null || apiDto.player() == null) {
            throw new MappingException("PlayerDto o player data es null");
        }

        if (apiDto.statistics() == null || apiDto.statistics().isEmpty()) {
            throw new MappingException("Estadísticas del jugador están vacías");
        }

        // Usar la primera estadística (la más reciente por default en API-Football)
        ApiFootballDtos.PlayerStatsData stats = apiDto.statistics().get(0);

        // Extraer datos del jugador
        ApiFootballDtos.PlayerData player = apiDto.player();
        String playerName = (player.firstname() != null ? player.firstname() + " " : "") +
                           (player.lastname() != null ? player.lastname() : "");

        if (playerName.isBlank()) {
            playerName = player.name();
        }

        // Detectar posición desde datos disponibles (heurística simple)
        String position = detectPosition(stats);

        // Mapear estadísticas reales
        Integer goals = stats.goals() != null ? stats.goals().total() : 0;
        Integer assists = stats.goals() != null ? stats.goals().assists() : 0;
        Integer passes = stats.passes() != null ? stats.passes().total() : 0;
        Integer passAccuracy = stats.passes() != null ? stats.passes().accuracy() : 0;
        Integer tackles = stats.tackles() != null ? stats.tackles() : 0;
        Integer dribbles = stats.dribbles() != null ? stats.dribbles().attempts() : 0;
        Integer dribblesSuccess = stats.dribbles() != null ? stats.dribbles().success() : 0;

        // Calcular porcentaje de regates exitosos
        Integer dribblesSuccessPercentage = dribbles > 0 ? 
                Math.round((100f * dribblesSuccess) / dribbles) : 0;

        // Crear RealStats
        RealStats realStats = new RealStats(
                stats.league().season(),
                stats.league().name(),
                stats.games() != null ? stats.games().appearances() : 0,
                goals,
                assists,
                passAccuracy,
                dribblesSuccessPercentage,
                tackles,
                calculatePerformanceScore(goals, passAccuracy, dribblesSuccessPercentage, tackles, position),
                java.time.Instant.now()
        );

        // Crear Player
        return Player.nueva(
                player.id().toString(),
                playerName,
                position,
                player.age() != null ? player.age() : 25,
                player.nationality() != null ? player.nationality() : "Unknown",
                teamId,
                realStats
        );
    }

    /**
     * Detecta la posición del jugador basándose en heurísticas simples.
     * En producción, esto debería venir de los datos de la API.
     */
    private String detectPosition(ApiFootballDtos.PlayerStatsData stats) {
        // Heurística simple:
        // - Si tiene muchos goles, probablemente es atacante (ST/FW)
        // - Si tiene muchos tackles, probablemente es defensor (CB/LB/RB)
        // - Si está equilibrado, probablemente es mediocampista (CM/CAM)

        int goals = stats.goals() != null ? (stats.goals().total() != null ? stats.goals().total() : 0) : 0;
        int tackles = stats.tackles() != null ? stats.tackles() : 0;
        int appearances = stats.games() != null ? (stats.games().appearances() != null ? stats.games().appearances() : 1) : 1;

        double goalsPerMatch = appearances > 0 ? (double) goals / appearances : 0;
        double tacklesPerMatch = appearances > 0 ? (double) tackles / appearances : 0;

        if (goalsPerMatch > 0.5) {
            return "ST"; // Striker/Forward
        } else if (tacklesPerMatch > 2.0) {
            return "CB"; // Center Back/Defender
        } else {
            return "CM"; // Midfielder
        }
    }

    /**
     * Calcula el performance score del jugador basado en sus estadísticas normalizadas.
     */
    private Integer calculatePerformanceScore(Integer goals, Integer passAccuracy, 
                                            Integer dribblesSuccess, Integer tackles,
                                            String position) {
        // Normalizar cada componente a escala 0-100
        Integer normalizedGoals = normalizeGoals(goals, position);
        Integer normalizedPasses = Math.min(passAccuracy, 100);
        Integer normalizedDribbles = Math.min(dribblesSuccess, 100);
        Integer normalizedTackles = normalizeTackles(tackles, position);

        // Calcular promedio ponderado según posición
        double score;
        if ("ST".equals(position)) {
            // Para delanteros: goles (40%), regates (30%), pases (20%), tackles (10%)
            score = (normalizedGoals * 0.4) + (normalizedDribbles * 0.3) + 
                    (normalizedPasses * 0.2) + (normalizedTackles * 0.1);
        } else if ("CB".equals(position)) {
            // Para defensas: tackles (40%), pases (30%), goles (20%), regates (10%)
            score = (normalizedTackles * 0.4) + (normalizedPasses * 0.3) + 
                    (normalizedGoals * 0.2) + (normalizedDribbles * 0.1);
        } else {
            // Para mediocampistas: pases (35%), tackles (30%), regates (20%), goles (15%)
            score = (normalizedPasses * 0.35) + (normalizedTackles * 0.3) + 
                    (normalizedDribbles * 0.2) + (normalizedGoals * 0.15);
        }

        return Math.min((int) score, 100);
    }

    /**
     * Normaliza goles a escala 0-100 según posición.
     */
    private Integer normalizeGoals(Integer goals, String position) {
        if (goals == null || goals < 0) {
            return 0;
        }

        // Máximos históricos razonables por temporada según posición
        int max = "ST".equals(position) ? 50 : ("CM".equals(position) ? 20 : 5);
        return Math.min((goals * 100) / max, 100);
    }

    /**
     * Normaliza tackles a escala 0-100 según posición.
     */
    private Integer normalizeTackles(Integer tackles, String position) {
        if (tackles == null || tackles < 0) {
            return 0;
        }

        // Máximos históricos razonables por temporada según posición
        int max = "CB".equals(position) ? 30 : ("CM".equals(position) ? 20 : 5);
        return Math.min((tackles * 100) / max, 100);
    }
}

/**
 * Excepción lanzada cuando el mapeo falla.
 */
class MappingException extends RuntimeException {
    public MappingException(String message) {
        super(message);
    }

    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
