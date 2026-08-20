package com.example.football.usuario.domain;

public class NoAutorizadoParaCambiarRolException extends RuntimeException {
    public NoAutorizadoParaCambiarRolException(String mensaje) {
        super(mensaje);
    }

    public NoAutorizadoParaCambiarRolException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
