package com.example.football.usuario.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRoleAuditLogRepository extends JpaRepository<RoleAuditLogJpaEntity, UUID> {
    @Query("SELECT ral FROM RoleAuditLogJpaEntity ral WHERE ral.usuarioId = :usuarioId ORDER BY ral.fechaCambio DESC")
    List<RoleAuditLogJpaEntity> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT ral FROM RoleAuditLogJpaEntity ral ORDER BY ral.fechaCambio DESC LIMIT :limit")
    List<RoleAuditLogJpaEntity> findLatest(@Param("limit") int limit);
}
