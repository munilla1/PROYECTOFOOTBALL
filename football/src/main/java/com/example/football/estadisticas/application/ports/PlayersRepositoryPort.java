package com.example.football.estadisticas.application.ports;

import com.example.football.estadisticas.domain.Player;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto: Interfaz de repositorio para Jugadores.
 * 
 * Define operaciones de persistencia para la entidad Player.
 * La implementación está en infraestructura.
 */
public interface PlayersRepositoryPort {

    /**
     * Guarda un jugador (crea o actualiza).
     */
    void save(Player player);

    /**
     * Obtiene un jugador por su ID interno.
     */
    Optional<Player> findById(UUID id);

    /**
     * Obtiene un jugador por su ID externo (API-Football).
     */
    Optional<Player> findByExternalId(String externalId);

    /**
     * Obtiene todos los jugadores de un equipo.
     */
    List<Player> findByTeam(String teamId);

    /**
     * Obtiene todos los jugadores.
     */
    List<Player> findAll();

    /**
     * Elimina un jugador.
     */
    void delete(UUID id);

    /**
     * Cuenta cantidad de jugadores.
     */
    long count();
}
