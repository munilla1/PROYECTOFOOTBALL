package com.example.football.usuario.infrastructure;

import com.example.football.usuario.domain.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_audit_logs")
public class RoleAuditLogJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rolAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rolNuevo;

    @Column(nullable = false)
    private UUID cambiadoPorUsuarioId;

    @Column(nullable = false)
    private Instant fechaCambio;

    @Column(length = 500)
    private String motivo;

    // Constructores
    public RoleAuditLogJpaEntity() {
    }

    public RoleAuditLogJpaEntity(UUID usuarioId, Rol rolAnterior, Rol rolNuevo, UUID cambiadoPorUsuarioId, Instant fechaCambio) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.rolAnterior = rolAnterior;
        this.rolNuevo = rolNuevo;
        this.cambiadoPorUsuarioId = cambiadoPorUsuarioId;
        this.fechaCambio = fechaCambio;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Rol getRolAnterior() {
        return rolAnterior;
    }

    public void setRolAnterior(Rol rolAnterior) {
        this.rolAnterior = rolAnterior;
    }

    public Rol getRolNuevo() {
        return rolNuevo;
    }

    public void setRolNuevo(Rol rolNuevo) {
        this.rolNuevo = rolNuevo;
    }

    public UUID getCambiadoPorUsuarioId() {
        return cambiadoPorUsuarioId;
    }

    public void setCambiadoPorUsuarioId(UUID cambiadoPorUsuarioId) {
        this.cambiadoPorUsuarioId = cambiadoPorUsuarioId;
    }

    public Instant getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(Instant fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
