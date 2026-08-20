package com.example.football.usuario.application;

import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarUsuarios {
    private final UsuarioRepository usuarioRepository;

    public ListarUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> ejecutar(UUID adminSolicitanteId) {
        // 1. Validar que solicitante es admin
        Usuario admin = usuarioRepository.findById(adminSolicitanteId)
                .orElseThrow(() -> new UsuarioNotFoundException(adminSolicitanteId));

        if (admin.rol() != Rol.ADMIN) {
            throw new RoleChangeNotAuthorizedException("Se requiere rol admin");
        }

        // 2. Retornar todos los usuarios
        return usuarioRepository.findAll();
    }
}
