package com.example.football.partidos.domain;

import com.example.football.estadisticas.domain.EstadisticasException;

/**
 * Excepción de dominio: PartidoJornadaBloqueadaException
 * 
 * Se lanza cuando se intenta crear o jugar un partido fuera del estado permitido de jornada.
 * Contiene código de error específico para manejo en capas superiores.
 */
public class PartidoJornadaBloqueadaException extends EstadisticasException {
    private final String errorCode;
    private final Integer roundNumber;

    public static final String JORNADA_NOT_STARTED = "JORNADA_NOT_STARTED";
    public static final String JORNADA_FINISHED = "JORNADA_FINISHED";
    public static final String JORNADA_POSTPONED = "JORNADA_POSTPONED";
    public static final String JORNADA_NOT_FOUND = "JORNADA_NOT_FOUND";

    public PartidoJornadaBloqueadaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.roundNumber = null;
    }

    public PartidoJornadaBloqueadaException(String errorCode, String message, Integer roundNumber) {
        super(message);
        this.errorCode = errorCode;
        this.roundNumber = roundNumber;
    }

    public PartidoJornadaBloqueadaException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.roundNumber = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }
}
