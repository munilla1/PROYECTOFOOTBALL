package com.example.football.jornadas.application.services;

import com.example.football.jornadas.domain.Jornada;
import com.example.football.jornadas.domain.JornadaStatus;
import com.example.football.jornadas.domain.JornadasException;
import com.example.football.jornadas.infrastructure.dtos.JornadaDto;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de aplicación: JornadasMapperService
 * 
 * Convierte DTOs de API-Football a entidades de dominio Jornada.
 * Implementa la lógica de mapeo de estados y extracción de números de ronda.
 */
@Service
public class JornadasMapperService {
    private static final Pattern ROUND_PATTERN = Pattern.compile("^.*?([0-9]+)\\s*$");

    /**
     * Mapea un DTO de API-Football a una entidad de dominio Jornada.
     * 
     * @param dto DTO desde API-Football
     * @return entidad Jornada con datos mapeados
     * @throws JornadasException si el DTO contiene datos inválidos
     */
    public Jornada mapDtoToJornada(JornadaDto dto) {
        if (dto == null) {
            throw new JornadasException("JornadaDto cannot be null");
        }

        // Validar estructura
        validateJornadaStructure(dto);

        String league = dto.league().name();
        Integer season = dto.league().season();
        Integer roundNumber = extractRoundNumber(dto.roundIdentifier());
        Integer matchCount = dto.fixtures().total();

        // Determinar estado basado en progreso
        JornadaStatus status = determineStatus(dto);

        return Jornada.nueva(roundNumber, league, season, status, matchCount);
    }

    /**
     * Mapea un estado de texto de API a JornadaStatus del dominio.
     * 
     * @param apiStatus string del estado desde API
     * @return JornadaStatus mapeado
     * @throws JornadasException si el estado es desconocido
     */
    public JornadaStatus mapStatusApiToDomain(String apiStatus) {
        if (apiStatus == null || apiStatus.isBlank()) {
            throw new JornadasException("API status cannot be null or blank");
        }
        return JornadaStatus.fromApiValue(apiStatus);
    }

    /**
     * Valida que el DTO tenga todos los campos requeridos.
     * 
     * @param dto DTO a validar
     * @throws JornadasException si falta algún campo obligatorio
     */
    public void validateJornadaStructure(JornadaDto dto) {
        if (dto.roundIdentifier() == null || dto.roundIdentifier().isBlank()) {
            throw new JornadasException("Round identifier cannot be null or blank");
        }
        if (dto.league() == null) {
            throw new JornadasException("League info cannot be null");
        }
        if (dto.league().name() == null || dto.league().name().isBlank()) {
            throw new JornadasException("League name cannot be null or blank");
        }
        if (dto.league().season() == null) {
            throw new JornadasException("League season cannot be null");
        }
        if (dto.fixtures() == null) {
            throw new JornadasException("Fixtures data cannot be null");
        }
        if (dto.fixtures().total() == null || dto.fixtures().total() < 0) {
            throw new JornadasException("Total fixtures count is invalid");
        }
        if (dto.fixtures().current() == null || dto.fixtures().current() < 0) {
            throw new JornadasException("Current fixtures count is invalid");
        }
    }

    /**
     * Extrae el número de ronda desde el identificador de API.
     * Ejemplo: "Regular Season - 1" → 1
     * 
     * @param roundIdentifier string con formato de API
     * @return número de ronda extraído
     * @throws JornadasException si no se puede extraer número válido
     */
    private Integer extractRoundNumber(String roundIdentifier) {
        if (roundIdentifier == null || roundIdentifier.isBlank()) {
            throw new JornadasException("Cannot extract round number from null/blank identifier");
        }

        Matcher matcher = ROUND_PATTERN.matcher(roundIdentifier.trim());
        if (!matcher.find()) {
            throw new JornadasException("Invalid round identifier format: " + roundIdentifier);
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new JornadasException("Could not parse round number from: " + roundIdentifier, e);
        }
    }

    /**
     * Determina el estado de la jornada basándose en el progreso de fixtures.
     * 
     * Lógica:
     * - Si current == 0 y total > 0: NOT_STARTED
     * - Si current > 0 y current < total: IN_PROGRESS
     * - Si current == total: FINISHED
     * 
     * @param dto DTO con datos de fixtures
     * @return JornadaStatus determinado
     */
    private JornadaStatus determineStatus(JornadaDto dto) {
        int current = dto.fixtures().current();
        int total = dto.fixtures().total();

        if (current == 0 && total > 0) {
            return JornadaStatus.NOT_STARTED;
        } else if (current > 0 && current < total) {
            return JornadaStatus.IN_PROGRESS;
        } else if (current == total && total > 0) {
            return JornadaStatus.FINISHED;
        } else {
            // Estado por defecto o anomalía
            return JornadaStatus.NOT_STARTED;
        }
    }
}
