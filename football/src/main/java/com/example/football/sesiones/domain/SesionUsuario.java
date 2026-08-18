package com.example.football.sesiones.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record SesionUsuario(
        UUID id,
        UUID usuarioId,
        String tokenHash,
        Instant fechaInicio,
        Instant fechaExpiracion,
        Instant ultimaActividad,
        EstadoSesion estado) {

    public SesionUsuario {
        if (id == null || usuarioId == null || tokenHash == null || tokenHash.isBlank()
                || fechaInicio == null || fechaExpiracion == null || ultimaActividad == null || estado == null) {
            throw new IllegalArgumentException("La sesion contiene datos obligatorios ausentes");
        }
        if (!fechaExpiracion.isAfter(fechaInicio) || ultimaActividad.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("Las fechas de la sesion no son validas");
        }
    }

    public static SesionUsuario nueva(UUID usuarioId, String tokenHash, Instant ahora, Duration duracion) {
        return nueva(UUID.randomUUID(), usuarioId, tokenHash, ahora, duracion);
    }

    public static SesionUsuario nueva(UUID id, UUID usuarioId, String tokenHash, Instant ahora, Duration duracion) {
        return new SesionUsuario(id, usuarioId, tokenHash, ahora,
                ahora.plus(duracion), ahora, EstadoSesion.ACTIVA);
    }

    public boolean estaActiva(Instant ahora, Duration inactividadMaxima) {
        return estado == EstadoSesion.ACTIVA
                && ahora.isBefore(fechaExpiracion)
                && ahora.isBefore(ultimaActividad.plus(inactividadMaxima));
    }

    public SesionUsuario conActividad(Instant ahora) {
        return new SesionUsuario(id, usuarioId, tokenHash, fechaInicio, fechaExpiracion,
                ahora, EstadoSesion.ACTIVA);
    }

    public SesionUsuario expirada() {
        return new SesionUsuario(id, usuarioId, tokenHash, fechaInicio, fechaExpiracion,
                ultimaActividad, EstadoSesion.EXPIRADA);
    }

    public SesionUsuario cerrada() {
        return new SesionUsuario(id, usuarioId, tokenHash, fechaInicio, fechaExpiracion,
                ultimaActividad, EstadoSesion.CERRADA);
    }
}
