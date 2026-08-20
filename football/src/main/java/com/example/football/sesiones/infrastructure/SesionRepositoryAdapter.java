package com.example.football.sesiones.infrastructure;

import com.example.football.sesiones.application.SesionRepository;
import com.example.football.sesiones.domain.EstadoSesion;
import com.example.football.sesiones.domain.SesionUsuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SesionRepositoryAdapter implements SesionRepository {
    private final SpringDataSesionRepository repository;

    public SesionRepositoryAdapter(SpringDataSesionRepository repository) {
        this.repository = repository;
    }

    @Override
    public SesionUsuario save(SesionUsuario sesion) {
        return toDomain(repository.save(toEntity(sesion)));
    }

    @Override
    public Optional<SesionUsuario> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SesionUsuario> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public List<SesionUsuario> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void invalidar(UUID sesionId) {
        repository.findById(sesionId).ifPresent(entity -> {
            entity.setEstado(EstadoSesion.EXPIRADA);
            repository.save(entity);
        });
    }

    private SesionJpaEntity toEntity(SesionUsuario sesion) {
        return new SesionJpaEntity(sesion.id(), sesion.usuarioId(), sesion.tokenHash(), sesion.fechaInicio(),
                sesion.fechaExpiracion(), sesion.ultimaActividad(), sesion.estado());
    }

    private SesionUsuario toDomain(SesionJpaEntity entity) {
        return new SesionUsuario(entity.getId(), entity.getUsuarioId(), entity.getTokenHash(),
                entity.getFechaInicio(), entity.getFechaExpiracion(), entity.getUltimaActividad(), entity.getEstado());
    }
}
