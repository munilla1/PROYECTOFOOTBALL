package com.example.football.usuario.presentation;

import com.example.football.usuario.domain.EstadoJugador;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.TipoMembresia;
import com.example.football.usuario.domain.Usuario;

import java.time.Instant;
import java.util.UUID;

public final class UsuarioDtos {
    private UsuarioDtos() {
    }

    public record RegistroRequest(String nombre, String email, String password) {
    }

    public record ProgresoRequest(Integer nivel, Integer xp, Integer energia, EstadoJugador estado,
                                  String estadisticasAcumuladas, String historialPartidos) {
    }

    public record UsuarioResponse(UUID id, String nombre, String email, Rol rol, TipoMembresia membresia,
                                  Instant fechaCreacion, Instant fechaInicioTrial,
                                  Instant fechaExpiracionMembresia, int nivel, int xp, int energia,
                                  EstadoJugador estadoJugador, String estadisticasAcumuladas,
                                  String historialPartidos) {
        public static UsuarioResponse from(Usuario usuario) {
            var progreso = usuario.progreso();
            return new UsuarioResponse(usuario.id(), usuario.nombre(), usuario.email(), usuario.rol(),
                    usuario.membresia(), usuario.fechaCreacion(), usuario.fechaInicioTrial(),
                    usuario.fechaExpiracionMembresia(), progreso.nivel(), progreso.xp(), progreso.energia(),
                    progreso.estado(), progreso.estadisticasAcumuladas(), progreso.historialPartidos());
        }
    }
}
