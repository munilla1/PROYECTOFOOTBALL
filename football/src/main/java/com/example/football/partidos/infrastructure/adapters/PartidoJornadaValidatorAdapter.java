package com.example.football.partidos.infrastructure.adapters;

import com.example.football.jornadas.application.ports.JornadasRepositoryPort;
import com.example.football.jornadas.domain.Jornada;
import com.example.football.partidos.domain.JornadaValidatorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de infraestructura: PartidoJornadaValidatorAdapter
 * 
 * Implementa JornadaValidatorPort usando repositorio de jornadas.
 * Conecta la validación de partidos con la sincronización de jornadas.
 */
@Component
public class PartidoJornadaValidatorAdapter implements JornadaValidatorPort {
    private static final Logger logger = LoggerFactory.getLogger(PartidoJornadaValidatorAdapter.class);

    private final JornadasRepositoryPort jornadasRepository;

    public PartidoJornadaValidatorAdapter(JornadasRepositoryPort jornadasRepository) {
        this.jornadasRepository = jornadasRepository;
    }

    /**
     * Verifica si existe una jornada real para los parámetros dados.
     * 
     * @param league nombre de la liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return Optional con la jornada si existe en BD
     */
    @Override
    public Optional<Jornada> validarJornadaExiste(String league, Integer season, Integer roundNumber) {
        try {
            Optional<Jornada> jornada = jornadasRepository.findByRound(league, season, roundNumber);
            if (jornada.isPresent()) {
                logger.debug("Jornada found: {}/{}/{}", league, season, roundNumber);
            } else {
                logger.debug("Jornada not found: {}/{}/{}", league, season, roundNumber);
            }
            return jornada;
        } catch (Exception e) {
            logger.error("Error validating jornada existence: {}/{}/{}", league, season, roundNumber, e);
            return Optional.empty();
        }
    }

    /**
     * Valida si la jornada permite jugar (status IN_PROGRESS).
     * 
     * @param jornada jornada a validar
     * @return true si jornada.isPlayable() es verdadero
     */
    @Override
    public boolean validarJornadaDisponible(Jornada jornada) {
        boolean isPlayable = jornada.isPlayable();
        logger.debug("Jornada {} playable check: {}", jornada.getCompositeId(), isPlayable);
        return isPlayable;
    }
}
