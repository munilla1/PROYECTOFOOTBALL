package com.example.football.usuario.domain;

public record ProgresoJugador(
        int nivel,
        int xp,
        int energia,
        EstadoJugador estado,
        String estadisticasAcumuladas,
        String historialPartidos) {

    public ProgresoJugador {
        if (nivel < 1) {
            throw new IllegalArgumentException("El nivel debe ser mayor que cero");
        }
        if (xp < 0) {
            throw new IllegalArgumentException("La experiencia no puede ser negativa");
        }
        if (energia < 0 || energia > 100) {
            throw new IllegalArgumentException("La energia debe estar entre 0 y 100");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del jugador es obligatorio");
        }
        estadisticasAcumuladas = estadisticasAcumuladas == null ? "{}" : estadisticasAcumuladas;
        historialPartidos = historialPartidos == null ? "[]" : historialPartidos;
    }

    public static ProgresoJugador inicial() {
        return new ProgresoJugador(1, 0, 100, EstadoJugador.NORMAL, "{}", "[]");
    }
}
