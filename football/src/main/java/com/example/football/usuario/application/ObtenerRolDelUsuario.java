package com.example.football.usuario.application;

import com.example.football.usuario.domain.Rol;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ObtenerRolDelUsuario {
    private final UsuarioRepository usuarioRepository;

    public ObtenerRolDelUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Rol ejecutar(UUID usuarioId) {
        return usuarioRepository.obtenerRolDelUsuario(usuarioId);
    }
}
