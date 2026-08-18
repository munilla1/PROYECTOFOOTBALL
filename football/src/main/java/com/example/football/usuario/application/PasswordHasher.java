package com.example.football.usuario.application;

public interface PasswordHasher {
    String hash(String password);
    boolean matches(String password, String hash);
}
