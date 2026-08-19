package com.example.football.sesiones.application;

import com.example.football.sesiones.domain.EstadoSesion;
import com.example.football.sesiones.domain.SesionUsuario;
import com.example.football.sesiones.infrastructure.TokenService;
import com.example.football.usuario.application.PasswordHasher;
import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class SesionService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final SesionRepository sesionRepository;
    private final TokenService tokenService;
    private final Duration duracion;
    private final Duration inactividadMaxima;

    public SesionService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher,
                         SesionRepository sesionRepository, TokenService tokenService,
                         @Value("${app.session.duration:PT8H}") Duration duracion,
                         @Value("${app.session.inactivity-timeout:PT2H}") Duration inactividadMaxima) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.sesionRepository = sesionRepository;
        this.tokenService = tokenService;
        this.duracion = duracion;
        this.inactividadMaxima = inactividadMaxima;
    }

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------
    @Transactional
    public LoginResult login(String email, String password) {

        // 🔴 CORRECCIÓN 1: datos vacíos → 400 sesion.datos-invalidos
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new SesionException("sesion.datos-invalidos", "El email y la contraseña son obligatorios");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new SesionException("sesion.datos-invalidos", "El email no es valido");
        }

        Optional<Usuario> usuario = usuarioRepository.findByEmail(email.trim().toLowerCase());

        if (usuario.isEmpty()) {
            throw new SesionException("usuario.no-existe", "El usuario no existe");
        }

        if (!passwordHasher.matches(password, usuario.get().passwordHash())) {
            throw new SesionException("sesion.credenciales-invalidas", "Las credenciales no son validas");
        }

        Instant ahora = Instant.now();
        UUID sesionId = UUID.randomUUID();
        Instant expiracion = ahora.plus(duracion);

        String token = tokenService.issue(sesionId, usuario.get().id(), ahora, expiracion);

        SesionUsuario sesion = SesionUsuario.nueva(
                sesionId,
                usuario.get().id(),
                tokenService.hash(token),
                ahora,
                duracion
        );

        sesionRepository.save(sesion);

        return new LoginResult(token, expiracion, usuario.get().id());
    }

    // ------------------------------------------------------------
    // LOGOUT
    // ------------------------------------------------------------
    @Transactional
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        sesionRepository.findByTokenHash(tokenService.hash(token))
                .ifPresent(s -> sesionRepository.save(s.cerrada()));
    }

    // ------------------------------------------------------------
    // AUTENTICAR (middleware)
    // ------------------------------------------------------------
    @Transactional(noRollbackFor = SesionException.class)
    public UUID autenticar(String token) {

        TokenService.TokenData datos;
        try {
            datos = tokenService.parseAndVerify(token);
        } catch (IllegalArgumentException exception) {
            throw new SesionException("sesion.token-invalido", "El token no es valido");
        }

        SesionUsuario sesion = sesionRepository.findById(datos.sessionId())
                .orElseThrow(() -> new SesionException("sesion.token-invalido", "El token no es valido"));

        Instant ahora = Instant.now();

        // 🔴 CORRECCIÓN 3: si no está ACTIVA → token inválido
        if (sesion.estado() != EstadoSesion.ACTIVA) {
            throw new SesionException("sesion.token-invalido", "El token no es valido");
        }

        // 🔴 CORRECCIÓN 4: expiración por inactividad → persistir EXPIRADA
        if (!sesion.estaActiva(ahora, inactividadMaxima)) {
            sesionRepository.save(sesion.expirada());   // ← aquí se persiste EXPIRADA
            throw new SesionException("sesion.expirada", "La sesion ha expirado");
        }

        // 🔴 CORRECCIÓN 5: token manipulado
        if (!sesion.usuarioId().equals(datos.userId()) ||
            !sesion.tokenHash().equals(tokenService.hash(token))) {
            throw new SesionException("sesion.token-invalido", "El token no es valido");
        }

        // 🔴 CORRECCIÓN 6: registrar actividad y persistir
        sesionRepository.save(sesion.conActividad(ahora));

        return sesion.usuarioId();
    }

    public record LoginResult(String token, Instant expiresAt, UUID userId) {}
}

