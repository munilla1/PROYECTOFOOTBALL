package com.example.football.usuario.application;

import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.infrastructure.RoleAuditLogJpaEntity;
import com.example.football.usuario.infrastructure.SpringDataRoleAuditLogRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class RoleAuditLogRepository {
    private final SpringDataRoleAuditLogRepository springDataRepository;

    public RoleAuditLogRepository(SpringDataRoleAuditLogRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    public void registrarCambioDeRol(UUID usuarioId, Rol rolAnterior, Rol rolNuevo, UUID cambiadoPorUsuarioId, Instant fechaCambio) {
        RoleAuditLogJpaEntity log = new RoleAuditLogJpaEntity(usuarioId, rolAnterior, rolNuevo, cambiadoPorUsuarioId, fechaCambio);
        springDataRepository.save(log);
    }

    public List<RoleAuditLogJpaEntity> obtenerHistorial(UUID usuarioId) {
        return springDataRepository.findByUsuarioId(usuarioId);
    }

    public List<RoleAuditLogJpaEntity> obtenerUltimos(int limite) {
        return springDataRepository.findLatest(limite);
    }
}
