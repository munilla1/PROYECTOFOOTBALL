package com.example.football.sesiones.application;

import org.springframework.http.HttpStatus;

public class SesionException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public SesionException(String code, HttpStatus status) {
        super(code);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

