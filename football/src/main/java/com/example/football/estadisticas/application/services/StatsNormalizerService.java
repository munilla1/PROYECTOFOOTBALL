package com.example.football.estadisticas.application.services;

import com.example.football.estadisticas.domain.RealStats;
import org.springframework.stereotype.Service;

/**
 * Servicio: Normaliza estadísticas reales a escalas estándar (0-100).
 * 
 * Responsabilidades:
 * - Normalizar goles, asistencias, etc. a escala 0-100 según posición
 * - Aplicar factores de ajuste por liga/nivel
 * - Validar valores normalizados
 * - Proporcionar funciones de desagregación
 */
@Service
public class StatsNormalizerService {

    /**
     * Normaliza el RealStats aplicando factores de estandarización.
     */
    public RealStats normalize(RealStats stats, String position) {
        if (stats == null) {
            throw new IllegalArgumentException("RealStats no puede ser null");
        }

        // Normalizar cada métrica
        Integer normalizedGoals = normalizeGoals(stats.goals(), position, stats.appearances());
        Integer normalizedAssists = normalizeAssists(stats.assists(), position, stats.appearances());
        Integer normalizedTackles = normalizeTackles(stats.tackles(), position, stats.appearances());

        // Recalcular el performance score con valores normalizados
        Integer updatedPerformanceScore = recalculatePerformanceScore(
                normalizedGoals,
                stats.passesAccuracy(),
                stats.dribblesSuccess(),
                normalizedTackles,
                position
        );

        // Retornar stats normalizados
        return new RealStats(
                stats.season(),
                stats.league(),
                stats.appearances(),
                normalizedGoals,
                normalizedAssists,
                stats.passesAccuracy(),
                stats.dribblesSuccess(),
                normalizedTackles,
                updatedPerformanceScore,
                stats.lastUpdated()
        );
    }

    /**
     * Normaliza goles a escala 0-100 según posición y apariciones.
     * 
     * Usa benchmarks históricos reales de Top 5 European Leagues.
     */
    public Integer normalizeGoals(Integer goals, String position, Integer appearances) {
        if (goals == null || goals < 0) {
            return 0;
        }

        if (appearances == null || appearances == 0) {
            return 0;
        }

        // Calcular tasa de goles
        double goalsPerGame = (double) goals / appearances;

        // Aplicar factors máximos históricos según posición
        // Datos basados en top performers de La Liga, Premier League, etc.
        double maxGoalsPerGame = switch (position.toUpperCase()) {
            case "ST", "FW", "CF" -> 0.8;    // Striker: máximo ~0.8 goles/partido
            case "LW", "RW" -> 0.4;          // Winger: máximo ~0.4 goles/partido
            case "CM", "CAM" -> 0.25;        // Midfielder: máximo ~0.25 goles/partido
            case "CB", "LB", "RB" -> 0.05;   // Defender: máximo ~0.05 goles/partido
            default -> 0.2;                  // Default: midfield
        };

        // Normalizar: (actual / máximo) * 100
        double normalizedScore = (goalsPerGame / maxGoalsPerGame) * 100;
        return Math.min((int) normalizedScore, 100);
    }

    /**
     * Normaliza asistencias a escala 0-100 según posición.
     */
    public Integer normalizeAssists(Integer assists, String position, Integer appearances) {
        if (assists == null || assists < 0) {
            return 0;
        }

        if (appearances == null || appearances == 0) {
            return 0;
        }

        double assistsPerGame = (double) assists / appearances;

        // Máximos históricos según posición
        double maxAssistsPerGame = switch (position.toUpperCase()) {
            case "LW", "RW", "CAM" -> 0.5;   // Winger/Creative: máximo ~0.5 asistencias/partido
            case "CM", "CDM" -> 0.2;         // Midfielder: máximo ~0.2 asistencias/partido
            case "ST", "FW", "CF" -> 0.15;   // Striker: máximo ~0.15 asistencias/partido
            case "CB", "LB", "RB" -> 0.05;   // Defender: máximo ~0.05 asistencias/partido
            default -> 0.15;
        };

        double normalizedScore = (assistsPerGame / maxAssistsPerGame) * 100;
        return Math.min((int) normalizedScore, 100);
    }

    /**
     * Normaliza tackles a escala 0-100 según posición.
     */
    public Integer normalizeTackles(Integer tackles, String position, Integer appearances) {
        if (tackles == null || tackles < 0) {
            return 0;
        }

        if (appearances == null || appearances == 0) {
            return 0;
        }

        double tacklesPerGame = (double) tackles / appearances;

        // Máximos históricos según posición
        double maxTacklesPerGame = switch (position.toUpperCase()) {
            case "CB" -> 4.0;                // Center Back: máximo ~4 tackles/partido
            case "LB", "RB" -> 3.5;          // Full Back: máximo ~3.5 tackles/partido
            case "CM", "CDM" -> 2.5;         // Midfielder: máximo ~2.5 tackles/partido
            case "CAM" -> 1.5;               // Attacking Mid: máximo ~1.5 tackles/partido
            case "LW", "RW" -> 1.2;          // Winger: máximo ~1.2 tackles/partido
            case "ST", "FW", "CF" -> 0.8;    // Striker: máximo ~0.8 tackles/partido
            default -> 2.0;
        };

        double normalizedScore = (tacklesPerGame / maxTacklesPerGame) * 100;
        return Math.min((int) normalizedScore, 100);
    }

    /**
     * Aplica factor de ajuste por liga.
     * Algunas ligas son defensivamente más exigentes que otras.
     */
    public Double applyLeagueAdjustment(String league, Double score) {
        if (score == null || league == null) {
            return score;
        }

        // Factores de ajuste por liga (relativos a La Liga = 1.0)
        double factor = switch (league.toUpperCase()) {
            case "LALIGA" -> 1.0;           // Referencia
            case "PREMIER LEAGUE" -> 1.05;  // Ligeramente más defensiva
            case "SERIE A" -> 1.08;         // Más defensiva
            case "BUNDESLIGA" -> 0.98;      // Ligeramente más ofensiva
            case "LIGUE 1" -> 0.95;         // Menos defensiva
            default -> 1.0;
        };

        double adjusted = score * factor;
        return Math.min(adjusted, 100.0);
    }

    /**
     * Recalcula el performance score con estadísticas normalizadas.
     */
    private Integer recalculatePerformanceScore(
            Integer normalizedGoals,
            Integer passAccuracy,
            Integer dribblesSuccess,
            Integer normalizedTackles,
            String position) {

        double score;
        if ("ST".equals(position) || "FW".equals(position) || "CF".equals(position)) {
            // Para delanteros: goles (40%), regates (30%), pases (20%), tackles (10%)
            score = (normalizedGoals * 0.4) + (dribblesSuccess * 0.3) + 
                    (passAccuracy * 0.2) + (normalizedTackles * 0.1);
        } else if ("CB".equals(position) || "LB".equals(position) || "RB".equals(position)) {
            // Para defensas: tackles (40%), pases (30%), goles (20%), regates (10%)
            score = (normalizedTackles * 0.4) + (passAccuracy * 0.3) + 
                    (normalizedGoals * 0.2) + (dribblesSuccess * 0.1);
        } else if ("CDM".equals(position)) {
            // Para volantes defensivos: tackles (45%), pases (35%), regates (15%), goles (5%)
            score = (normalizedTackles * 0.45) + (passAccuracy * 0.35) + 
                    (dribblesSuccess * 0.15) + (normalizedGoals * 0.05);
        } else if ("CAM".equals(position)) {
            // Para volantes ofensivos: pases (40%), regates (30%), goles (20%), tackles (10%)
            score = (passAccuracy * 0.4) + (dribblesSuccess * 0.3) + 
                    (normalizedGoals * 0.2) + (normalizedTackles * 0.1);
        } else {
            // Para mediocampistas genéricos: pases (35%), tackles (30%), regates (20%), goles (15%)
            score = (passAccuracy * 0.35) + (normalizedTackles * 0.3) + 
                    (dribblesSuccess * 0.2) + (normalizedGoals * 0.15);
        }

        return Math.min((int) score, 100);
    }

    /**
     * Valida que todas las estadísticas normalizadas estén en rango 0-100.
     */
    public boolean validateNormalizedStats(RealStats stats) {
        return stats.goals() >= 0 && stats.goals() <= 100 &&
               stats.assists() >= 0 && stats.assists() <= 100 &&
               stats.passesAccuracy() >= 0 && stats.passesAccuracy() <= 100 &&
               stats.dribblesSuccess() >= 0 && stats.dribblesSuccess() <= 100 &&
               stats.tackles() >= 0 && stats.tackles() <= 100 &&
               stats.performanceScore() >= 0 && stats.performanceScore() <= 100;
    }
}
