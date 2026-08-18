package com.example.football.usuario.application;

import java.util.UUID;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(UUID id) {
        super("No existe el usuario " + id);
    }
}
