package com.example.football.usuario.presentation;

import com.example.football.usuario.application.ActualizacionProgreso;
import com.example.football.usuario.application.UsuarioService;
import com.example.football.usuario.domain.ProgresoJugador;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

import static com.example.football.usuario.presentation.UsuarioDtos.ProgresoRequest;
import static com.example.football.usuario.presentation.UsuarioDtos.RegistroRequest;
import static com.example.football.usuario.presentation.UsuarioDtos.UsuarioResponse;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody RegistroRequest request) {
        var usuario = service.registrar(request.nombre(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.from(usuario));
    }

    @GetMapping("/me")
    public UsuarioResponse obtenerActual(Principal principal) {
        return UsuarioResponse.from(service.obtener(currentUserId(principal)));
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable UUID id, Principal principal) {
        requireOwner(id, principal);
        return UsuarioResponse.from(service.obtener(id));
    }

    @PutMapping("/me/progreso")
    public UsuarioResponse actualizar(@RequestBody ProgresoRequest request, Principal principal) {
        UUID id = currentUserId(principal);
        var actual = service.obtener(id).progreso();
        var progreso = new ProgresoJugador(
                request.nivel() == null ? actual.nivel() : request.nivel(),
                request.xp() == null ? actual.xp() : request.xp(),
                request.energia() == null ? actual.energia() : request.energia(),
                request.estado() == null ? actual.estado() : request.estado(),
                request.estadisticasAcumuladas() == null ? actual.estadisticasAcumuladas() : request.estadisticasAcumuladas(),
                request.historialPartidos() == null ? actual.historialPartidos() : request.historialPartidos());
        return UsuarioResponse.from(service.actualizarProgreso(id, progreso));
    }

    private UUID currentUserId(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException();
        }
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException();
        }
    }

    private void requireOwner(UUID id, Principal principal) {
        if (!id.equals(currentUserId(principal))) {
            throw new ForbiddenException();
        }
    }
}

