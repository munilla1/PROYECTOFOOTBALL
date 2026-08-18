package com.example.football.usuario.application;

import com.example.football.usuario.domain.ProgresoJugador;
import com.example.football.usuario.domain.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    @Autowired
    public UsuarioService(UsuarioRepository repository, PasswordHasher passwordHasher) {
        this(repository, passwordHasher, Clock.systemUTC());
    }

    UsuarioService(UsuarioRepository repository, PasswordHasher passwordHasher, Clock clock) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    public Usuario registrar(String nombre, String email, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailDuplicadoException();
        }
        return repository.save(Usuario.nuevo(nombre, email, passwordHasher.hash(password), Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    public Usuario actualizarProgreso(UUID id, ProgresoJugador progreso) {
        Usuario usuario = obtener(id);
        return repository.save(usuario.conProgreso(progreso, Instant.now(clock)));
    }
}
