package com.example.football.usuario.application;

import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UUID id);
    Optional<Usuario> findByEmail(String email);
    Rol obtenerRolDelUsuario(UUID usuarioId);
    void actualizarRol(UUID usuarioId, Rol nuevoRol);
    List<Usuario> findAll();
}
