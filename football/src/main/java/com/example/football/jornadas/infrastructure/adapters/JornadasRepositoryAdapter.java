package com.example.football.jornadas.infrastructure.adapters;

import com.example.football.jornadas.application.ports.JornadasRepositoryPort;
import com.example.football.jornadas.domain.Jornada;
import com.example.football.jornadas.domain.JornadasException;
import com.example.football.jornadas.infrastructure.persistence.JornadaJpaEntity;
import com.example.football.jornadas.infrastructure.persistence.JornadaJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura: JornadasRepositoryAdapter
 * 
 * Implementa JornadasRepositoryPort usando Spring Data JPA.
 * Convierte entre entidades de dominio y JPA entities.
 * Sigue el patrón de CHG-0001 usando ObjectMapper para conversiones.
 */
@Component
public class JornadasRepositoryAdapter implements JornadasRepositoryPort {
    private static final Logger logger = LoggerFactory.getLogger(JornadasRepositoryAdapter.class);

    private final JornadaJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public JornadasRepositoryAdapter(JornadaJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Persiste una nueva jornada en la base de datos.
     * 
     * @param jornada entidad de dominio a guardar
     * @return jornada guardada con ID asignado
     * @throws JornadasException si hay error en persistencia
     */
    @Override
    @Transactional
    public Jornada save(Jornada jornada) {
        try {
            JornadaJpaEntity entity = domainToJpaEntity(jornada);
            JornadaJpaEntity saved = jpaRepository.save(entity);
            logger.info("Jornada created: {}", jornada.getCompositeId());
            return jpaEntityToDomain(saved);
        } catch (Exception e) {
            logger.error("Failed to save jornada: {}", jornada.getCompositeId(), e);
            throw new JornadasException("Failed to save jornada: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza una jornada existente.
     * Actualiza automáticamente el timestamp de synchronizedAt.
     * 
     * @param jornada entidad de dominio a actualizar
     * @return jornada actualizada
     * @throws JornadasException si no existe o hay error en persistencia
     */
    @Override
    @Transactional
    public Jornada update(Jornada jornada) {
        try {
            Optional<JornadaJpaEntity> existing = jpaRepository.findById(jornada.id());
            if (existing.isEmpty()) {
                throw new JornadasException("Jornada not found with id: " + jornada.id());
            }

            JornadaJpaEntity entity = existing.get();
            entity.setStatus(jornada.status());
            entity.setMatchCount(jornada.matchCount());
            entity.setSynchronizedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            JornadaJpaEntity updated = jpaRepository.save(entity);
            logger.info("Jornada updated: {} status changed to {}", 
                    jornada.getCompositeId(), jornada.status());
            return jpaEntityToDomain(updated);
        } catch (JornadasException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update jornada: {}", jornada.getCompositeId(), e);
            throw new JornadasException("Failed to update jornada: " + e.getMessage(), e);
        }
    }

    /**
     * Busca una jornada por su identidad compuesta.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return Optional con la jornada si existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Jornada> findByRound(String league, Integer season, Integer roundNumber) {
        try {
            Optional<JornadaJpaEntity> entity = jpaRepository
                    .findByLeagueAndSeasonAndRoundNumber(league, season, roundNumber);
            return entity.map(this::jpaEntityToDomain);
        } catch (Exception e) {
            logger.error("Failed to find jornada: {}/{}/{}", league, season, roundNumber, e);
            throw new JornadasException("Failed to find jornada: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las jornadas de una liga en una temporada.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @return lista de jornadas, vacía si no hay
     */
    @Override
    @Transactional(readOnly = true)
    public List<Jornada> findAllByLeagueAndSeason(String league, Integer season) {
        try {
            List<JornadaJpaEntity> entities = jpaRepository.findByLeagueAndSeason(league, season);
            return entities.stream()
                    .map(this::jpaEntityToDomain)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to find jornadas for {}/{}", league, season, e);
            throw new JornadasException("Failed to find jornadas: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si existe una jornada con identidad compuesta.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return true si existe
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByRound(String league, Integer season, Integer roundNumber) {
        try {
            return jpaRepository.existsByLeagueAndSeasonAndRoundNumber(league, season, roundNumber);
        } catch (Exception e) {
            logger.error("Failed to check jornada existence: {}/{}/{}", league, season, roundNumber, e);
            return false;
        }
    }

    /**
     * Busca una jornada específica por todos sus identificadores.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @param jornadaId UUID único
     * @return Optional con la jornada si existe y coinciden todos los identificadores
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Jornada> findByRoundAndId(String league, Integer season, 
                                              Integer roundNumber, UUID jornadaId) {
        try {
            Optional<JornadaJpaEntity> entity = jpaRepository.findById(jornadaId);
            if (entity.isPresent()) {
                JornadaJpaEntity e = entity.get();
                if (e.getLeague().equals(league) && e.getSeason().equals(season) 
                    && e.getRoundNumber().equals(roundNumber)) {
                    return Optional.of(jpaEntityToDomain(e));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find jornada by round and id: {}/{}/{}/{}", 
                    league, season, roundNumber, jornadaId, e);
            throw new JornadasException("Failed to find jornada: " + e.getMessage(), e);
        }
    }

    /**
     * Convierte entidad de dominio a JPA entity.
     */
    private JornadaJpaEntity domainToJpaEntity(Jornada jornada) {
        return new JornadaJpaEntity(
                jornada.id(),
                jornada.roundNumber(),
                jornada.league(),
                jornada.season(),
                jornada.status(),
                jornada.matchCount(),
                jornada.createdAt(),
                jornada.synchronizedAt()
        );
    }

    /**
     * Convierte JPA entity a entidad de dominio.
     */
    private Jornada jpaEntityToDomain(JornadaJpaEntity entity) {
        return new Jornada(
                entity.getId(),
                entity.getRoundNumber(),
                entity.getLeague(),
                entity.getSeason(),
                entity.getStatus(),
                entity.getMatchCount(),
                entity.getCreatedAt(),
                entity.getSynchronizedAt()
        );
    }
}
