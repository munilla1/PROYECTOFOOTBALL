package com.example.football.jornadas.domain;

import com.example.football.estadisticas.domain.EstadisticasException;

/**
 * Excepción base para errores en el dominio de jornadas.
 * Hereda de EstadisticasException para mantener consistencia con CHG-0001.
 */
public class JornadasException extends EstadisticasException {
    public JornadasException(String message) {
        super(message);
    }

    public JornadasException(String message, Throwable cause) {
        super(message, cause);
    }
}
