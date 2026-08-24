package com.example.football.estadisticas.infrastructure.adapters;

import com.example.football.estadisticas.application.ports.PlayersRepositoryPort;
import com.example.football.estadisticas.domain.Player;
import com.example.football.estadisticas.domain.RealStats;
import com.example.football.estadisticas.infrastructure.persistence.PlayerJpaEntity;
import com.example.football.estadisticas.infrastructure.persistence.PlayerJpaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador: Implementación de PlayersRepositoryPort usando JPA.
 * 
 * Convierte entre la entidad de dominio Player y la entidad JPA PlayerJpaEntity.
 * Proporciona aislamiento entre la lógica de negocio (dominio) y la persistencia (JPA).
 */
@Service
public class PlayersRepositoryAdapter implements PlayersRepositoryPort {

    private final PlayerJpaRepository jpaRepository;

    public PlayersRepositoryAdapter(PlayerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Guarda un jugador (crea o actualiza).
     */
    @Override
    public void save(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player no puede ser null");
        }

        // Convertir Player (dominio) → PlayerJpaEntity (persistencia)
        PlayerJpaEntity entity = toJpaEntity(player);
        jpaRepository.save(entity);
    }

    /**
     * Obtiene un jugador por su ID interno.
     */
    @Override
    public Optional<Player> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(id.toString())
                .map(this::toDomainPlayer);
    }

    /**
     * Obtiene un jugador por su ID externo (API-Football).
     */
    @Override
    public Optional<Player> findByExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return Optional.empty();
        }

        return jpaRepository.findByExternalId(externalId)
                .map(this::toDomainPlayer);
    }

    /**
     * Obtiene todos los jugadores de un equipo.
     */
    @Override
    public List<Player> findByTeam(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            return List.of();
        }

        return jpaRepository.findByTeamId(teamId)
                .stream()
                .map(this::toDomainPlayer)
                .toList();
    }

    /**
     * Obtiene todos los jugadores.
     */
    @Override
    public List<Player> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomainPlayer)
                .toList();
    }

    /**
     * Elimina un jugador.
     */
    @Override
    public void delete(UUID id) {
        if (id != null) {
            jpaRepository.deleteById(id.toString());
        }
    }

    /**
     * Cuenta cantidad de jugadores.
     */
    @Override
    public long count() {
        return jpaRepository.count();
    }

    // ========== Conversiones Dominio ↔ JPA ==========

    /**
     * Convierte Player (dominio) a PlayerJpaEntity (persistencia).
     */
    private PlayerJpaEntity toJpaEntity(Player player) {
        RealStats stats = player.realStats();

        return new PlayerJpaEntity(
                player.id().toString(),
                player.externalId(),
                player.name(),
                player.position(),
                player.age(),
                player.nationality(),
                player.teamId(),
                stats.season(),
                stats.league(),
                stats.appearances(),
                stats.goals(),
                stats.assists(),
                stats.passesAccuracy(),
                stats.dribblesSuccess(),
                stats.tackles(),
                stats.performanceScore(),
                stats.lastUpdated(),
                player.createdAt(),
                Instant.now() // updatedAt siempre es ahora
        );
    }

    /**
     * Convierte PlayerJpaEntity (persistencia) a Player (dominio).
     */
    private Player toDomainPlayer(PlayerJpaEntity entity) {
        // Reconstruir RealStats desde los campos de PlayerJpaEntity
        RealStats realStats = new RealStats(
                entity.getSeason(),
                entity.getLeague(),
                entity.getAppearances(),
                entity.getGoals(),
                entity.getAssists(),
                entity.getPassesAccuracy(),
                entity.getDribblesSuccess(),
                entity.getTackles(),
                entity.getPerformanceScore(),
                entity.getLastStatsUpdated()
        );

        // Reconstruir Player desde la entidad JPA
        return new Player(
                UUID.fromString(entity.getId()),
                entity.getExternalId(),
                entity.getName(),
                entity.getPosition(),
                entity.getAge(),
                entity.getNationality(),
                entity.getTeamId(),
                realStats,
                entity.getLastStatsUpdated(),
                entity.getCreatedAt()
        );
    }
}
