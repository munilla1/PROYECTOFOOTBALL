package com.example.football.usuario.domain;

public enum Rol {
    USUARIO("usuario"),
    ADMIN("admin");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    public String valor() {
        return valor;
    }

    public static Rol desde(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Rol no puede ser nulo");
        }
        for (Rol rol : Rol.values()) {
            if (rol.valor.equals(valor)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol inválido. Valores aceptados: usuario, admin. Recibido: " + valor);
    }
}
