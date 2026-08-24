package com.example.football.jornadas.domain;

/**
 * Estados posibles de una jornada sincronizada desde API-Football.
 * Representa el ciclo de vida de una jornada: antes del inicio, en curso, finalizada o aplazada.
 */
public enum JornadaStatus {
    /** La jornada aún no ha comenzado */
    NOT_STARTED("Not Started"),
    /** La jornada está en curso */
    IN_PROGRESS("In Progress"),
    /** La jornada ha finalizado */
    FINISHED("Finished"),
    /** La jornada ha sido aplazada */
    POSTPONED("Postponed");

    private final String apiValue;

    JornadaStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    /**
     * Convierte un valor de API-Football a JornadaStatus.
     * @param apiValue valor desde API-Football
     * @return JornadaStatus correspondiente
     * @throws IllegalArgumentException si el valor no es reconocido
     */
    public static JornadaStatus fromApiValue(String apiValue) {
        if (apiValue == null) {
            throw new IllegalArgumentException("API status value cannot be null");
        }
        for (JornadaStatus status : JornadaStatus.values()) {
            if (status.apiValue.equalsIgnoreCase(apiValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown API status value: " + apiValue);
    }
}
