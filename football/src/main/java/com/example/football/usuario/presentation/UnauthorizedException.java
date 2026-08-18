package com.example.football.usuario.presentation;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Se requiere una sesion autenticada");
    }
}
