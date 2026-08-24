package com.example.football.estadisticas.application.ports;

/**
 * Excepción base para errores de conectividad con API.
 */
public class ApiConnectivityException extends RuntimeException {
    public ApiConnectivityException(String message) {
        super(message);
    }

    public ApiConnectivityException(String message, Throwable cause) {
        super(message, cause);
    }
}
