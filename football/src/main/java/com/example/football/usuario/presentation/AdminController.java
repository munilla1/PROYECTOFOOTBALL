package com.example.football.usuario.presentation;

import com.example.football.usuario.application.CambiarRolDeUsuario;
import com.example.football.usuario.application.ListarUsuarios;
import com.example.football.usuario.application.RoleAuditLogRepository;
import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.infrastructure.RoleAuditLogJpaEntity;
import com.example.football.usuario.infrastructure.security.RequiresRole;
import com.example.football.usuario.presentation.UsuarioDtos.CambiarRolRequest;
import com.example.football.usuario.presentation.UsuarioDtos.ConfigRequest;
import com.example.football.usuario.presentation.UsuarioDtos.ConfigResponse;
import com.example.football.usuario.presentation.UsuarioDtos.ErrorResponse;
import com.example.football.usuario.presentation.UsuarioDtos.LogResponse;
import com.example.football.usuario.presentation.UsuarioDtos.UsuarioListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CambiarRolDeUsuario cambiarRolDeUsuario;
    private final ListarUsuarios listarUsuarios;
    private final UsuarioRepository usuarioRepository;
    private final RoleAuditLogRepository auditLogRepository;

    public AdminController(
            CambiarRolDeUsuario cambiarRolDeUsuario,
            ListarUsuarios listarUsuarios,
            UsuarioRepository usuarioRepository,
            RoleAuditLogRepository auditLogRepository) {
        this.cambiarRolDeUsuario = cambiarRolDeUsuario;
        this.listarUsuarios = listarUsuarios;
        this.usuarioRepository = usuarioRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/users")
    @RequiresRole("admin")
    public ResponseEntity<List<UsuarioListResponse>> listarUsuarios(Principal principal) {
        UUID usuarioId = UUID.fromString(principal.getName());
        var usuarios = listarUsuarios.ejecutar(usuarioId);
        List<UsuarioListResponse> respuesta = usuarios.stream()
                .map(UsuarioListResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/users/{userId}/role")
    @RequiresRole("admin")
    public ResponseEntity<UsuarioListResponse> cambiarRol(
            @PathVariable String userId,
            @RequestBody CambiarRolRequest request,
            Principal principal) {

        UUID usuarioObjetivoId = UUID.fromString(userId);
        UUID adminId = UUID.fromString(principal.getName());
        Rol nuevoRol = Rol.desde(request.newRole());

        cambiarRolDeUsuario.ejecutar(usuarioObjetivoId, nuevoRol, adminId);

        var usuarioActualizado = usuarioRepository.findById(usuarioObjetivoId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado después de actualización"));

        return ResponseEntity.ok(UsuarioListResponse.from(usuarioActualizado));
    }

    @GetMapping("/logs")
    @RequiresRole("admin")
    public ResponseEntity<List<LogResponse>> obtenerLogs(
            @RequestParam(defaultValue = "100") int limit) {
        // Nota: Esta es una implementación simplificada
        // En producción, se requeriría un sistema de logging real
        List<LogResponse> logs = List.of(
                new LogResponse(Instant.now(), "INFO", "Sistema de roles inicializado")
        );
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/errors")
    @RequiresRole("admin")
    public ResponseEntity<List<ErrorResponse>> obtenerErrores(
            @RequestParam(defaultValue = "50") int limit) {
        // Nota: Esta es una implementación simplificada
        // En producción, se requeriría un sistema de manejo de errores real
        List<ErrorResponse> errores = List.of();
        return ResponseEntity.ok(errores);
    }

    @PostMapping("/config")
    @RequiresRole("admin")
    public ResponseEntity<ConfigResponse> actualizarConfig(
            @RequestBody ConfigRequest request) {
        // Nota: Esta es una implementación simplificada
        // En producción, se requeriría persistencia de configuración
        return ResponseEntity.ok(new ConfigResponse("Configuración actualizada: " + request.clave()));
    }
}
