package com.example.football.usuario.application;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException() {
        super("El email ya esta registrado");
    }
}
