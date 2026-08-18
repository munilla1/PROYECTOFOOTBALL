package com.example.football.usuario.infrastructure;

import com.example.football.usuario.domain.EstadoJugador;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.TipoMembresia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMembresia membresia;

    @Column(nullable = false)
    private Instant fechaCreacion;

    private Instant fechaInicioTrial;
    private Instant fechaExpiracionMembresia;
    private Instant actualizadoEn;

    @Column(nullable = false)
    private int nivel;

    @Column(nullable = false)
    private int xp;

    @Column(nullable = false)
    private int energia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoJugador estadoJugador;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String estadisticasAcumuladas;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String historialPartidos;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id, String nombre, String email, String passwordHash, Rol rol,
                            TipoMembresia membresia, Instant fechaCreacion, Instant fechaInicioTrial,
                            Instant fechaExpiracionMembresia, Instant actualizadoEn, int nivel, int xp,
                            int energia, EstadoJugador estadoJugador, String estadisticasAcumuladas,
                            String historialPartidos) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.membresia = membresia;
        this.fechaCreacion = fechaCreacion;
        this.fechaInicioTrial = fechaInicioTrial;
        this.fechaExpiracionMembresia = fechaExpiracionMembresia;
        this.actualizadoEn = actualizadoEn;
        this.nivel = nivel;
        this.xp = xp;
        this.energia = energia;
        this.estadoJugador = estadoJugador;
        this.estadisticasAcumuladas = estadisticasAcumuladas;
        this.historialPartidos = historialPartidos;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public TipoMembresia getMembresia() { return membresia; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaInicioTrial() { return fechaInicioTrial; }
    public Instant getFechaExpiracionMembresia() { return fechaExpiracionMembresia; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public int getNivel() { return nivel; }
    public int getXp() { return xp; }
    public int getEnergia() { return energia; }
    public EstadoJugador getEstadoJugador() { return estadoJugador; }
    public String getEstadisticasAcumuladas() { return estadisticasAcumuladas; }
    public String getHistorialPartidos() { return historialPartidos; }
}
