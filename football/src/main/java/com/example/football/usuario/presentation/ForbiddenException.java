package com.example.football.usuario.presentation;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("El usuario no puede acceder a este recurso");
    }
}
