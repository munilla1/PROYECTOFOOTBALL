package com.example.football.partidos.domain;

import com.example.football.jornadas.domain.Jornada;
import java.util.Optional;

/**
 * Puerto de dominio: JornadaValidatorPort
 * 
 * Define el contrato que valida si se puede jugar un partido en una jornada.
 * Implementada por PartidoJornadaValidatorAdapter en infraestructura.
 */
public interface JornadaValidatorPort {
    /**
     * Verifica si existe una jornada real para el partido.
     * 
     * @param league nombre de la liga
     * @param season temporada
     * @param roundNumber número de ronda
     * @return Optional con la jornada si existe
     */
    Optional<Jornada> validarJornadaExiste(String league, Integer season, Integer roundNumber);

    /**
     * Valida si la jornada permite jugar (status IN_PROGRESS).
     * 
     * @param jornada jornada a validar
     * @return true si se pueden jugar partidos
     */
    boolean validarJornadaDisponible(Jornada jornada);
}
