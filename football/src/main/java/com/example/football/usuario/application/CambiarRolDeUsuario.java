package com.example.football.usuario.application;

import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.RoleValidator;
import com.example.football.usuario.domain.Usuario;
import com.example.football.usuario.domain.UsuarioPuedeCambiarRol;
import com.example.football.sesiones.application.SesionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CambiarRolDeUsuario {
    private final UsuarioRepository usuarioRepository;
    private final RoleValidator roleValidator;
    private final RoleAuditLogRepository auditLogRepository;
    private final SesionRepository sesionRepository;

    public CambiarRolDeUsuario(
            UsuarioRepository usuarioRepository,
            RoleValidator roleValidator,
            RoleAuditLogRepository auditLogRepository,
            SesionRepository sesionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleValidator = roleValidator;
        this.auditLogRepository = auditLogRepository;
        this.sesionRepository = sesionRepository;
    }

    public void ejecutar(UUID usuarioObjetivoId, Rol nuevoRol, UUID adminSolicitanteId) {
        // 1. Obtener usuarios
        Usuario admin = usuarioRepository.findById(adminSolicitanteId)
                .orElseThrow(() -> new UsuarioNotFoundException(adminSolicitanteId));
        Usuario objetivo = usuarioRepository.findById(usuarioObjetivoId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioObjetivoId));

        // 2. Validar permisos usando la especificación
        UsuarioPuedeCambiarRol especificacion = new UsuarioPuedeCambiarRol(admin, objetivo, nuevoRol);
        if (!especificacion.esValida()) {
            throw new RoleChangeNotAuthorizedException(especificacion.obtenerMensajeError());
        }

        // 3. Guardar rol anterior
        Rol rolAnterior = objetivo.rol();

        // 4. Crear usuario con nuevo rol
        Usuario usuarioActualizado = new Usuario(
                objetivo.id(),
                objetivo.nombre(),
                objetivo.email(),
                objetivo.passwordHash(),
                nuevoRol,
                objetivo.membresia(),
                objetivo.fechaCreacion(),
                objetivo.fechaInicioTrial(),
                objetivo.fechaExpiracionMembresia(),
                Instant.now(),
                objetivo.progreso()
        );

        // 5. Persistir
        usuarioRepository.save(usuarioActualizado);

        // 6. Registrar en auditoría
        auditLogRepository.registrarCambioDeRol(usuarioObjetivoId, rolAnterior, nuevoRol, adminSolicitanteId, Instant.now());

        // 7. Invalidar sesiones antiguas del usuario
        invalidarSesionesDelUsuario(usuarioObjetivoId);
    }

    private void invalidarSesionesDelUsuario(UUID usuarioId) {
        try {
            var sesionesActivas = sesionRepository.findByUsuarioId(usuarioId);
            for (var sesion : sesionesActivas) {
                sesionRepository.invalidar(sesion.id());
            }
        } catch (Exception e) {
            // Log pero no fallar si hay error al invalidar sesiones
            System.err.println("Error al invalidar sesiones: " + e.getMessage());
        }
    }
}
