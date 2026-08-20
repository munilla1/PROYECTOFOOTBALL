package com.example.football.usuario.infrastructure;

import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.application.UsuarioNotFoundException;
import com.example.football.usuario.domain.ProgresoJugador;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.Usuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {
    private final SpringDataUsuarioRepository repository;

    public UsuarioRepositoryAdapter(SpringDataUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        try {
            return toDomain(repository.save(toEntity(usuario)));
        } catch (DataIntegrityViolationException exception) {
            throw exception;
        }
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Rol obtenerRolDelUsuario(UUID usuarioId) {
        return repository.findById(usuarioId)
                .map(UsuarioJpaEntity::getRol)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
    }

    @Override
    public void actualizarRol(UUID usuarioId, Rol nuevoRol) {
        UsuarioJpaEntity entity = repository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
        entity.setRol(nuevoRol);
        repository.save(entity);
    }

    @Override
    public List<Usuario> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private UsuarioJpaEntity toEntity(Usuario usuario) {
        ProgresoJugador progreso = usuario.progreso();
        return new UsuarioJpaEntity(usuario.id(), usuario.nombre(), usuario.email(), usuario.passwordHash(),
                usuario.rol(), usuario.membresia(), usuario.fechaCreacion(), usuario.fechaInicioTrial(),
                usuario.fechaExpiracionMembresia(), usuario.actualizadoEn(), progreso.nivel(), progreso.xp(),
                progreso.energia(), progreso.estado(), progreso.estadisticasAcumuladas(), progreso.historialPartidos());
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {
        ProgresoJugador progreso = new ProgresoJugador(entity.getNivel(), entity.getXp(), entity.getEnergia(),
                entity.getEstadoJugador(), entity.getEstadisticasAcumuladas(), entity.getHistorialPartidos());
        return new Usuario(entity.getId(), entity.getNombre(), entity.getEmail(), entity.getPasswordHash(),
                entity.getRol(), entity.getMembresia(), entity.getFechaCreacion(), entity.getFechaInicioTrial(),
                entity.getFechaExpiracionMembresia(), entity.getActualizadoEn(), progreso);
    }
}
