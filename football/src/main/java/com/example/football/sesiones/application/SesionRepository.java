package com.example.football.sesiones.application;

import com.example.football.sesiones.domain.SesionUsuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionRepository {
    SesionUsuario save(SesionUsuario sesion);
    Optional<SesionUsuario> findById(UUID id);
    Optional<SesionUsuario> findByTokenHash(String tokenHash);
    List<SesionUsuario> findByUsuarioId(UUID usuarioId);
    void invalidar(UUID sesionId);
}
