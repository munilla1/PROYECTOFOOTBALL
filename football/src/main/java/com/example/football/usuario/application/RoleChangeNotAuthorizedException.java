package com.example.football.usuario.application;

public class RoleChangeNotAuthorizedException extends RuntimeException {
    public RoleChangeNotAuthorizedException(String mensaje) {
        super(mensaje);
    }

    public RoleChangeNotAuthorizedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
