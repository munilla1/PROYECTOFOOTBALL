package com.example.football.jornadas.domain;

/**
 * DTO de resultado de operación de sincronización.
 * Registra conteos de jornadas creadas, actualizadas y con errores.
 */
public record SyncResult(
        Integer created,
        Integer updated,
        Integer errors,
        Long durationMs
) {
    public SyncResult {
        if (created == null) created = 0;
        if (updated == null) updated = 0;
        if (errors == null) errors = 0;
        if (durationMs == null) durationMs = 0L;
    }

    public Integer getTotalProcessed() {
        return created + updated;
    }

    public boolean wasSuccessful() {
        return errors == 0;
    }
}
