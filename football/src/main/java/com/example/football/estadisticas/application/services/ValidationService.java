package com.example.football.estadisticas.application.services;

import com.example.football.estadisticas.domain.Match;
import com.example.football.estadisticas.domain.Player;
import com.example.football.estadisticas.domain.RealStats;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio: Valida entidades de dominio antes de persistencia.
 * 
 * Responsabilidades:
 * - Validar reglas de negocio de Player
 * - Validar reglas de negocio de Match
 * - Detectar datos inconsistentes o faltantes
 * - Compilar lista de errores de validación
 */
@Service
public class ValidationService {

    /**
     * Valida un jugador antes de guardarlo.
     * Retorna lista de errores (vacía si es válido).
     */
    public List<String> validatePlayer(Player player) {
        List<String> errors = new ArrayList<>();

        if (player == null) {
            errors.add("Player no puede ser null");
            return errors;
        }

        // Validar campos básicos
        if (player.externalId() == null || player.externalId().isBlank()) {
            errors.add("Player externalId no puede estar vacío");
        }

        if (player.name() == null || player.name().isBlank()) {
            errors.add("Player name no puede estar vacío");
        }

        if (player.position() == null || player.position().isBlank()) {
            errors.add("Player position no puede estar vacío");
        }

        if (player.age() == null || player.age() < 16 || player.age() > 50) {
            errors.add("Player age debe estar entre 16 y 50 años");
        }

        if (player.nationality() == null || player.nationality().isBlank()) {
            errors.add("Player nationality no puede estar vacío");
        }

        if (player.teamId() == null || player.teamId().isBlank()) {
            errors.add("Player teamId no puede estar vacío");
        }

        // Validar estadísticas reales
        if (player.realStats() != null) {
            errors.addAll(validateRealStats(player.realStats()));
        } else {
            errors.add("Player realStats no puede ser null");
        }

        return errors;
    }

    /**
     * Valida un partido antes de guardarlo.
     * Retorna lista de errores (vacía si es válido).
     */
    public List<String> validateMatch(Match match) {
        List<String> errors = new ArrayList<>();

        if (match == null) {
            errors.add("Match no puede ser null");
            return errors;
        }

        // Validar campos básicos
        if (match.fixtureId() == null || match.fixtureId().isBlank()) {
            errors.add("Match fixtureId no puede estar vacío");
        }

        if (match.round() == null || match.round() < 1 || match.round() > 100) {
            errors.add("Match round debe estar entre 1 y 100");
        }

        if (match.league() == null || match.league().isBlank()) {
            errors.add("Match league no puede estar vacío");
        }

        if (match.season() == null || match.season() < 2000 || match.season() > 2100) {
            errors.add("Match season debe estar entre 2000 y 2100");
        }

        if (match.homeTeamId() == null || match.homeTeamId().isBlank()) {
            errors.add("Match homeTeamId no puede estar vacío");
        }

        if (match.awayTeamId() == null || match.awayTeamId().isBlank()) {
            errors.add("Match awayTeamId no puede estar vacío");
        }

        // Validar que equipos sean diferentes
        if (match.homeTeamId() != null && match.awayTeamId() != null &&
            match.homeTeamId().equals(match.awayTeamId())) {
            errors.add("Match: homeTeamId y awayTeamId no pueden ser iguales");
        }

        // Validar score
        if (match.finalScore() != null) {
            errors.addAll(validateScore(match.finalScore()));
        } else {
            errors.add("Match finalScore no puede ser null");
        }

        if (match.status() == null || match.status().isBlank()) {
            errors.add("Match status no puede estar vacío");
        }

        // Validar status válido
        String validStatuses = "Not Started, In Play, Match Finished, Postponed, Cancelled, Suspended";
        if (match.status() != null && !isValidStatus(match.status())) {
            errors.add("Match status inválido. Valores válidos: " + validStatuses);
        }

        return errors;
    }

    /**
     * Valida estadísticas reales.
     */
    private List<String> validateRealStats(RealStats stats) {
        List<String> errors = new ArrayList<>();

        if (stats.season() == null || stats.season() < 2000) {
            errors.add("RealStats season debe ser >= 2000");
        }

        if (stats.league() == null || stats.league().isBlank()) {
            errors.add("RealStats league no puede estar vacío");
        }

        if (stats.appearances() == null || stats.appearances() < 0) {
            errors.add("RealStats appearances no puede ser negativo");
        }

        if (stats.goals() == null || stats.goals() < 0 || stats.goals() > 100) {
            errors.add("RealStats goals debe estar entre 0 y 100");
        }

        if (stats.assists() == null || stats.assists() < 0 || stats.assists() > 100) {
            errors.add("RealStats assists debe estar entre 0 y 100");
        }

        if (stats.passesAccuracy() == null || stats.passesAccuracy() < 0 || stats.passesAccuracy() > 100) {
            errors.add("RealStats passesAccuracy debe estar entre 0 y 100");
        }

        if (stats.dribblesSuccess() == null || stats.dribblesSuccess() < 0 || stats.dribblesSuccess() > 100) {
            errors.add("RealStats dribblesSuccess debe estar entre 0 y 100");
        }

        if (stats.tackles() == null || stats.tackles() < 0 || stats.tackles() > 100) {
            errors.add("RealStats tackles debe estar entre 0 y 100");
        }

        if (stats.performanceScore() == null || stats.performanceScore() < 0 || stats.performanceScore() > 100) {
            errors.add("RealStats performanceScore debe estar entre 0 y 100");
        }

        return errors;
    }

    /**
     * Valida un score de partido.
     */
    private List<String> validateScore(com.example.football.estadisticas.domain.Score score) {
        List<String> errors = new ArrayList<>();

        if (score.homeGoals() < 0) {
            errors.add("Score homeGoals no puede ser negativo");
        }

        if (score.awayGoals() < 0) {
            errors.add("Score awayGoals no puede ser negativo");
        }

        return errors;
    }

    /**
     * Valida que el status sea uno de los valores conocidos.
     */
    private boolean isValidStatus(String status) {
        return status.equals("Not Started") ||
               status.equals("In Play") ||
               status.equals("Match Finished") ||
               status.equals("Postponed") ||
               status.equals("Cancelled") ||
               status.equals("Suspended");
    }

    /**
     * Valida si no hay errores (ej: para usar en assertions o checks).
     */
    public boolean isValid(Player player) {
        return validatePlayer(player).isEmpty();
    }

    /**
     * Valida si no hay errores (ej: para usar en assertions o checks).
     */
    public boolean isValid(Match match) {
        return validateMatch(match).isEmpty();
    }

    /**
     * Compila todos los errores en un mensaje de texto.
     */
    public String compileErrors(List<String> errors) {
        return String.join("; ", errors);
    }
}
