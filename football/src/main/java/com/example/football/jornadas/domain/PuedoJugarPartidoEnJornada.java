package com.example.football.jornadas.domain;

/**
 * Especificación de Dominio (DDD): PuedoJugarPartidoEnJornada
 * 
 * Valida si un usuario puede jugar un partido dentro de una jornada real sincronizada.
 * La regla de negocio es: solo se pueden jugar partidos cuando la jornada está en estado IN_PROGRESS.
 * 
 * Estados bloqueados:
 * - NOT_STARTED: La jornada aún no ha comenzado
 * - FINISHED: La jornada ya ha finalizado
 * - POSTPONED: La jornada ha sido aplazada
 */
public class PuedoJugarPartidoEnJornada {
    private final Jornada jornada;

    public PuedoJugarPartidoEnJornada(Jornada jornada) {
        if (jornada == null) {
            throw new JornadasException("Jornada cannot be null");
        }
        this.jornada = jornada;
    }

    /**
     * Valida si es posible jugar en esta jornada.
     * @return true si la jornada está en IN_PROGRESS
     */
    public boolean esValida() {
        return jornada.isPlayable();
    }

    /**
     * Obtiene mensaje de error descriptivo según el estado de la jornada.
     * Usado para respuestas al cliente.
     * @return mensaje explicativo en español
     */
    public String obtenerMensajeError() {
        return switch (jornada.status()) {
            case NOT_STARTED -> String.format(
                    "La jornada %d de %s aún no ha comenzado. Espera a que empiece la ronda.",
                    jornada.roundNumber(), jornada.league());
            case FINISHED -> String.format(
                    "La jornada %d de %s ya ha finalizado. No puedes crear más partidos.",
                    jornada.roundNumber(), jornada.league());
            case POSTPONED -> String.format(
                    "La jornada %d de %s ha sido aplazada. Por favor, intenta más tarde.",
                    jornada.roundNumber(), jornada.league());
            case IN_PROGRESS -> "La jornada está disponible"; // Caso válido
        };
    }

    /**
     * Retorna el estado de la jornada para debugging.
     */
    public JornadaStatus obtenerEstado() {
        return jornada.status();
    }

    /**
     * Retorna la jornada asociada.
     */
    public Jornada obtenerJornada() {
        return jornada;
    }
}
