package com.example.football.estadisticas.infrastructure.adapters;

import com.example.football.estadisticas.application.ports.MatchesRepositoryPort;
import com.example.football.estadisticas.domain.Match;
import com.example.football.estadisticas.domain.PlayerMatchStats;
import com.example.football.estadisticas.domain.Score;
import com.example.football.estadisticas.infrastructure.persistence.MatchJpaEntity;
import com.example.football.estadisticas.infrastructure.persistence.MatchJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador: Implementación de MatchesRepositoryPort usando JPA.
 * 
 * Convierte entre la entidad de dominio Match y la entidad JPA MatchJpaEntity.
 * Maneja serialización/deserialización JSON de estadísticas de jugadores.
 */
@Service
public class MatchesRepositoryAdapter implements MatchesRepositoryPort {

    private final MatchJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public MatchesRepositoryAdapter(MatchJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Guarda un partido (crea o actualiza).
     */
    @Override
    public void save(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Match no puede ser null");
        }

        // Convertir Match (dominio) → MatchJpaEntity (persistencia)
        MatchJpaEntity entity = toJpaEntity(match);
        jpaRepository.save(entity);
    }

    /**
     * Obtiene un partido por su ID interno.
     */
    @Override
    public Optional<Match> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(id.toString())
                .map(this::toDomainMatch);
    }

    /**
     * Obtiene un partido por su ID externo (API-Football).
     */
    @Override
    public Optional<Match> findByFixtureId(String fixtureId) {
        if (fixtureId == null || fixtureId.isBlank()) {
            return Optional.empty();
        }

        return jpaRepository.findByFixtureId(fixtureId)
                .map(this::toDomainMatch);
    }

    /**
     * Obtiene todos los partidos de una jornada.
     */
    @Override
    public List<Match> findByRound(Integer round) {
        if (round == null || round < 1) {
            return List.of();
        }

        return jpaRepository.findByRound(round)
                .stream()
                .map(this::toDomainMatch)
                .toList();
    }

    /**
     * Obtiene todos los partidos de un equipo (como local o visitante).
     */
    @Override
    public List<Match> findByTeam(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            return List.of();
        }

        return jpaRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId)
                .stream()
                .map(this::toDomainMatch)
                .toList();
    }

    /**
     * Obtiene todos los partidos.
     */
    @Override
    public List<Match> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomainMatch)
                .toList();
    }

    /**
     * Elimina un partido.
     */
    @Override
    public void delete(UUID id) {
        if (id != null) {
            jpaRepository.deleteById(id.toString());
        }
    }

    /**
     * Cuenta cantidad de partidos.
     */
    @Override
    public long count() {
        return jpaRepository.count();
    }

    // ========== Conversiones Dominio ↔ JPA ==========

    /**
     * Convierte Match (dominio) a MatchJpaEntity (persistencia).
     */
    private MatchJpaEntity toJpaEntity(Match match) {
        // Serializar playerStats a JSON
        String playerStatsJson = serializePlayerStats(match.playerStats());

        return new MatchJpaEntity(
                match.id().toString(),
                match.fixtureId(),
                match.round(),
                match.league(),
                match.season(),
                match.date(),
                match.homeTeamId(),
                match.awayTeamId(),
                match.finalScore().homeGoals(),
                match.finalScore().awayGoals(),
                match.status(),
                playerStatsJson,
                match.createdAt(),
                Instant.now() // updatedAt siempre es ahora
        );
    }

    /**
     * Convierte MatchJpaEntity (persistencia) a Match (dominio).
     */
    private Match toDomainMatch(MatchJpaEntity entity) {
        // Deserializar playerStats desde JSON
        List<PlayerMatchStats> playerStats = deserializePlayerStats(entity.getPlayerStatsJson());

        // Reconstruir Score
        Score score = new Score(entity.getHomeGoals(), entity.getAwayGoals());

        // Reconstruir Match
        return new Match(
                UUID.fromString(entity.getId()),
                entity.getFixtureId(),
                entity.getRound(),
                entity.getLeague(),
                entity.getSeason(),
                entity.getMatchDate(),
                entity.getHomeTeamId(),
                entity.getAwayTeamId(),
                score,
                entity.getStatus(),
                playerStats,
                entity.getUpdatedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Serializa List<PlayerMatchStats> a JSON string.
     */
    private String serializePlayerStats(List<PlayerMatchStats> playerStats) {
        if (playerStats == null || playerStats.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(playerStats);
        } catch (Exception e) {
            // En caso de error de serialización, retornar array vacío
            return "[]";
        }
    }

    /**
     * Deserializa JSON string a List<PlayerMatchStats>.
     */
    private List<PlayerMatchStats> deserializePlayerStats(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PlayerMatchStats.class)
            );
        } catch (Exception e) {
            // En caso de error de deserialización, retornar lista vacía
            return new ArrayList<>();
        }
    }
}
