package com.example.football.partidos.application.services;

import com.example.football.jornadas.domain.Jornada;
import com.example.football.partidos.domain.JornadaValidatorPort;
import com.example.football.partidos.domain.PartidoJornadaBloqueadaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de aplicación: PartidosDisponiblesService
 * 
 * Filtra partidos según disponibilidad de jornada sincronizada.
 * Solo permite ver/crear partidos cuando la jornada correspondiente está en IN_PROGRESS.
 */
@Service
public class PartidosDisponiblesService {
    private static final Logger logger = LoggerFactory.getLogger(PartidosDisponiblesService.class);

    private final JornadaValidatorPort jornadaValidator;

    public PartidosDisponiblesService(JornadaValidatorPort jornadaValidator) {
        this.jornadaValidator = jornadaValidator;
    }

    /**
     * Valida que se pueda crear un partido en una jornada específica.
     * 
     * Algoritmo:
     * 1. Verifica que jornada exista en BD
     * 2. Verifica que jornada esté en estado IN_PROGRESS
     * 3. Si no existe o no está disponible: lanza PartidoJornadaBloqueadaException
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @throws PartidoJornadaBloqueadaException si jornada no existe o no está disponible
     */
    @Transactional(readOnly = true)
    public void validarPartidoDisponibleEnJornada(String league, Integer season, Integer roundNumber) {
        logger.debug("Validating partido availability for {}/{}/{}", league, season, roundNumber);

        // Verifica que jornada existe
        Optional<Jornada> jornadaOpt = jornadaValidator.validarJornadaExiste(league, season, roundNumber);
        
        if (jornadaOpt.isEmpty()) {
            logger.warn("Jornada not found for {}/{}/{}", league, season, roundNumber);
            throw new PartidoJornadaBloqueadaException(
                    PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                    String.format("No existe jornada real para %s season %d round %d. " +
                            "Por favor, espera a que se sincronice desde API-Football.",
                            league, season, roundNumber),
                    roundNumber
            );
        }

        Jornada jornada = jornadaOpt.get();

        // Verifica que jornada esté disponible (IN_PROGRESS)
        if (!jornadaValidator.validarJornadaDisponible(jornada)) {
            logger.warn("Jornada not playable: {} status {}", jornada.getCompositeId(), jornada.status());
            
            String errorCode;
            String message;
            
            switch (jornada.status()) {
                case NOT_STARTED -> {
                    errorCode = PartidoJornadaBloqueadaException.JORNADA_NOT_STARTED;
                    message = String.format("La jornada %d de %s aún no ha comenzado. " +
                            "Los partidos estarán disponibles cuando empiece la ronda.",
                            roundNumber, league);
                }
                case FINISHED -> {
                    errorCode = PartidoJornadaBloqueadaException.JORNADA_FINISHED;
                    message = String.format("La jornada %d de %s ya ha finalizado. " +
                            "No se pueden crear más partidos en esta ronda.",
                            roundNumber, league);
                }
                case POSTPONED -> {
                    errorCode = PartidoJornadaBloqueadaException.JORNADA_POSTPONED;
                    message = String.format("La jornada %d de %s ha sido aplazada. " +
                            "Por favor, intenta más tarde.",
                            roundNumber, league);
                }
                default -> {
                    errorCode = "UNKNOWN_STATUS";
                    message = "Estado desconocido de jornada";
                }
            }

            throw new PartidoJornadaBloqueadaException(errorCode, message, roundNumber);
        }

        logger.debug("Partido validated as available for {}/{}/{}", league, season, roundNumber);
    }

    /**
     * Obtiene la jornada si está disponible para jugar.
     * 
     * @param league nombre de liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return jornada disponible
     * @throws PartidoJornadaBloqueadaException si no está disponible
     */
    @Transactional(readOnly = true)
    public Jornada obtenerJornadaDisponible(String league, Integer season, Integer roundNumber) {
        validarPartidoDisponibleEnJornada(league, season, roundNumber);
        
        Optional<Jornada> jornada = jornadaValidator.validarJornadaExiste(league, season, roundNumber);
        return jornada.orElseThrow(() -> new PartidoJornadaBloqueadaException(
                PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                "Jornada disappeared after validation"
        ));
    }
}
