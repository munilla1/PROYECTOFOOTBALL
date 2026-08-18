package com.example.football.sesiones.application;

public class SesionException extends RuntimeException {
    private final String code;

    public SesionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
