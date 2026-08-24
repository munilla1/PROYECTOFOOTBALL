package com.example.football.estadisticas.domain;

/**
 * Excepción base del dominio de estadísticas.
 */
public class EstadisticasException extends RuntimeException {
    public EstadisticasException(String message) {
        super(message);
    }

    public EstadisticasException(String message, Throwable cause) {
        super(message, cause);
    }
}
