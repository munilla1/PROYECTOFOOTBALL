package com.example.football.usuario.domain;

public class RolInvalidoException extends RuntimeException {
    public RolInvalidoException(String mensaje) {
        super(mensaje);
    }

    public RolInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
