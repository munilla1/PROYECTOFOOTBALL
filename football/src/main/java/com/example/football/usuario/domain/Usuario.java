package com.example.football.usuario.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

public record Usuario(
        UUID id,
        String nombre,
        String email,
        String passwordHash,
        Rol rol,
        TipoMembresia membresia,
        Instant fechaCreacion,
        Instant fechaInicioTrial,
        Instant fechaExpiracionMembresia,
        Instant actualizadoEn,
        ProgresoJugador progreso) {

    public Usuario {
        if (id == null || nombre == null || nombre.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Identidad de usuario incompleta");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El email no es valido");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("La contrasena debe estar protegida");
        }
        if (rol == null || membresia == null || fechaCreacion == null || progreso == null) {
            throw new IllegalArgumentException("El usuario contiene datos obligatorios ausentes");
        }
        email = email.toLowerCase(Locale.ROOT);
        actualizadoEn = actualizadoEn == null ? fechaCreacion : actualizadoEn;
    }

    public static Usuario nuevo(String nombre, String email, String passwordHash, Instant ahora) {
        Instant finTrial = ahora.plus(7, ChronoUnit.DAYS);
        return new Usuario(UUID.randomUUID(), nombre.trim(), email, passwordHash, Rol.USUARIO,
                TipoMembresia.TRIAL, ahora, ahora, finTrial, ahora, ProgresoJugador.inicial());
    }

    public Usuario conProgreso(ProgresoJugador nuevoProgreso, Instant ahora) {
        return new Usuario(id, nombre, email, passwordHash, rol, membresia, fechaCreacion,
                fechaInicioTrial, fechaExpiracionMembresia, ahora, nuevoProgreso);
    }
}
