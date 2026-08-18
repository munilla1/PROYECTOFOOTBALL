package com.example.football.sesiones.infrastructure;

import com.example.football.sesiones.domain.EstadoSesion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones")
public class SesionJpaEntity {
    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(name = "ultima_actividad", nullable = false)
    private Instant ultimaActividad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSesion estado;

    protected SesionJpaEntity() {
    }

    public SesionJpaEntity(UUID id, UUID usuarioId, String tokenHash, Instant fechaInicio,
                           Instant fechaExpiracion, Instant ultimaActividad, EstadoSesion estado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.ultimaActividad = ultimaActividad;
        this.estado = estado;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getFechaInicio() { return fechaInicio; }
    public Instant getFechaExpiracion() { return fechaExpiracion; }
    public Instant getUltimaActividad() { return ultimaActividad; }
    public EstadoSesion getEstado() { return estado; }

    public void marcarExpirada() {
        this.estado = EstadoSesion.EXPIRADA;
    }

    public void registrarActividad(Instant ahora) {
        this.ultimaActividad = ahora;
    }
}
