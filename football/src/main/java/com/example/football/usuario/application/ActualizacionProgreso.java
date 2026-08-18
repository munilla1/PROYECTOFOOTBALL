package com.example.football.usuario.application;

import com.example.football.usuario.domain.EstadoJugador;

public record ActualizacionProgreso(
        Integer nivel,
        Integer xp,
        Integer energia,
        EstadoJugador estado,
        String estadisticasAcumuladas,
        String historialPartidos) {
}
