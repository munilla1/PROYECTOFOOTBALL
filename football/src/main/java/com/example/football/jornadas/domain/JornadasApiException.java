package com.example.football.jornadas.domain;

/**
 * Excepción específica para errores de conectividad con API-Football en jornadas.
 * Hereda de JornadasException para mantener jerarquía consistente.
 */
public class JornadasApiException extends JornadasException {
    private final Integer httpStatusCode;

    public JornadasApiException(String message) {
        super(message);
        this.httpStatusCode = null;
    }

    public JornadasApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = null;
    }

    public JornadasApiException(String message, Integer httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public JornadasApiException(String message, Integer httpStatusCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
}
