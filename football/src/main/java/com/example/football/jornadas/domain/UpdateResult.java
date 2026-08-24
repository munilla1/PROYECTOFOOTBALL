package com.example.football.jornadas.domain;

/**
 * DTO de resultado de operación de actualización de estados.
 * Registra conteos de cambios y anomalías.
 */
public record UpdateResult(
        Integer totalProcessed,
        Integer statusChanged,
        Integer noChange,
        Integer errors,
        Long durationMs
) {
    public UpdateResult {
        if (totalProcessed == null) totalProcessed = 0;
        if (statusChanged == null) statusChanged = 0;
        if (noChange == null) noChange = 0;
        if (errors == null) errors = 0;
        if (durationMs == null) durationMs = 0L;
    }

    public boolean wasSuccessful() {
        return errors == 0;
    }
}
