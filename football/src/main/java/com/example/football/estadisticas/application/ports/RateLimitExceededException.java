package com.example.football.estadisticas.application.ports;

import java.time.Instant;

/**
 * Excepción lanzada cuando se alcanza el rate limit de API-Football.
 */
public class RateLimitExceededException extends RuntimeException {
    private final Instant retryAfter;
    private final int attemptsMade;
    private final String endpoint;

    public RateLimitExceededException(String message, Instant retryAfter, int attemptsMade, String endpoint) {
        super(message);
        this.retryAfter = retryAfter;
        this.attemptsMade = attemptsMade;
        this.endpoint = endpoint;
    }

    public Instant getRetryAfter() {
        return retryAfter;
    }

    public int getAttemptsMade() {
        return attemptsMade;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
