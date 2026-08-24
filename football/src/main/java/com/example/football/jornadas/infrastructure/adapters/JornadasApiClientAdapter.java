package com.example.football.jornadas.infrastructure.adapters;

import com.example.football.estadisticas.infrastructure.adapters.ApiFootballClientAdapter;
import com.example.football.jornadas.domain.JornadasApiException;
import com.example.football.jornadas.application.ports.JornadasApiPort;
import com.example.football.jornadas.infrastructure.dtos.JornadaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador de infraestructura: JornadasApiClientAdapter
 * 
 * Implementa JornadasApiPort consumiendo API-Football mediante RestTemplate.
 * Incluye retry logic con exponential backoff y manejo de rate limiting.
 * 
 * Nota: Requiere que ApiFootballClientAdapter esté configurado con RestTemplate
 * en el contexto de Spring.
 */
@Component
public class JornadasApiClientAdapter implements JornadasApiPort {
    private static final Logger logger = LoggerFactory.getLogger(JornadasApiClientAdapter.class);
    private static final int MAX_RETRIES = 3;
    private static final int[] RETRY_DELAYS_MS = {1000, 2000, 4000};

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final String apiKey;

    public JornadasApiClientAdapter(RestTemplate restTemplate,
                                     @Value("${api.football.base-url}") String apiBaseUrl,
                                     @Value("${api.football.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Obtiene todas las jornadas de una liga en una temporada mediante retry.
     * 
     * @param league nombre de la liga
     * @param season temporada
     * @return lista de DTOs de jornadas
     * @throws JornadasApiException si falla tras todos los reintentos
     */
    @Override
    public List<JornadaDto> getJornadas(String league, Integer season) {
        logger.info("Fetching jornadas for league: {} season: {}", league, season);
        
        return executeWithRetry(() -> {
            String leagueId = mapLeagueNameToId(league);
            String url = String.format(
                    "%s/fixtures?league=%s&season=%d",
                    apiBaseUrl, leagueId, season
            );

            JornadasResponse response = restTemplate.getForObject(url, JornadasResponse.class);
            
            if (response == null || response.response() == null) {
                logger.warn("Empty response from API for league: {} season: {}", league, season);
                return Collections.emptyList();
            }

            // Agrupar fixtures por ronda y crear JornadaDtos
            return aggregateFixturesToJornadas(response.response(), league, season);
        });
    }

    /**
     * Obtiene el estado actual de una jornada específica.
     * 
     * @param league nombre de la liga
     * @param season temporada
     * @param round número de ronda
     * @return DTO con estado de la jornada
     * @throws JornadasApiException si falla
     */
    @Override
    public JornadaDto getJornadaStatus(String league, Integer season, Integer round) {
        logger.info("Fetching jornada status for league: {} season: {} round: {}", league, season, round);

        return executeWithRetry(() -> {
            String leagueId = mapLeagueNameToId(league);
            String url = String.format(
                    "%s/fixtures?league=%s&season=%d&round=%s",
                    apiBaseUrl, leagueId, season, "Regular Season - " + round
            );

            JornadasResponse response = restTemplate.getForObject(url, JornadasResponse.class);
            
            if (response == null || response.response() == null || response.response().isEmpty()) {
                throw new JornadasApiException(
                        "No fixtures found for round " + round + " in " + league + " season " + season
                );
            }

            // Retornar primera jornada agregada (todas tienen el mismo round)
            List<JornadaDto> jornadas = aggregateFixturesToJornadas(response.response(), league, season);
            return jornadas.isEmpty() ? null : jornadas.get(0);
        });
    }

    /**
     * Ejecuta una operación con retry automático.
     * 
     * @param operation operación a ejecutar
     * @return resultado de la operación
     * @throws JornadasApiException si falla tras MAX_RETRIES intentos
     */
    private <T> T executeWithRetry(ApiOperation<T> operation) {
        JornadasApiException lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return operation.execute();
            } catch (HttpClientErrorException.TooManyRequests e) {
                lastException = new JornadasApiException(
                        "Rate limit exceeded (HTTP 429)",
                        429,
                        e
                );
                logger.warn("Rate limit hit, attempt {}/{}", attempt + 1, MAX_RETRIES);
                sleepBeforeRetry(attempt);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE ||
                    e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                    lastException = new JornadasApiException(
                            "API temporarily unavailable: " + e.getMessage(),
                            e.getStatusCode().value(),
                            e
                    );
                    logger.warn("API unavailable, attempt {}/{}", attempt + 1, MAX_RETRIES);
                    sleepBeforeRetry(attempt);
                } else {
                    throw new JornadasApiException("API error: " + e.getMessage(), e.getStatusCode().value(), e);
                }
            } catch (Exception e) {
                throw new JornadasApiException("Failed to fetch jornadas: " + e.getMessage(), e);
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new JornadasApiException("Failed after " + MAX_RETRIES + " retries");
    }

    /**
     * Duerme antes de reintentar según exponential backoff.
     */
    private void sleepBeforeRetry(int attemptNumber) {
        if (attemptNumber < RETRY_DELAYS_MS.length) {
            try {
                Thread.sleep(RETRY_DELAYS_MS[attemptNumber]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JornadasApiException("Retry sleep interrupted", e);
            }
        }
    }

    /**
     * Agrupa fixtures por ronda (round) para crear JornadaDtos.
     * 
     * @param fixtures lista de fixture responses de API
     * @param league nombre de la liga
     * @param season temporada
     * @return lista de JornadaDtos agrupadas por round
     */
    private List<JornadaDto> aggregateFixturesToJornadas(List<FixtureResponse> fixtures, 
                                                          String league, Integer season) {
        // En una implementación real, agruparíamos por round
        // Por ahora, retornamos lista simplificada
        List<JornadaDto> jornadas = new ArrayList<>();
        
        if (fixtures.isEmpty()) {
            return jornadas;
        }

        // Placeholder: agrupación simplificada
        // En producción: usar Map<Integer, List<FixtureResponse>>
        logger.debug("Aggregated {} fixtures into jornadas for {} {}", fixtures.size(), league, season);
        
        return jornadas;
    }

    /**
     * Mapea nombres de liga a IDs de API-Football.
     * 
     * @param leagueName nombre legible de liga
     * @return ID para API-Football
     */
    private String mapLeagueNameToId(String leagueName) {
        return switch (leagueName.toLowerCase()) {
            case "laliga" -> "140"; // La Liga España
            case "premier league" -> "39"; // Premier League Inglaterra
            case "serie a" -> "135"; // Serie A Italia
            case "bundesliga" -> "78"; // Bundesliga Alemania
            case "ligue 1" -> "61"; // Ligue 1 Francia
            default -> throw new JornadasApiException("Unknown league: " + leagueName);
        };
    }

    /**
     * DTO para respuesta de API-Football (fixtures).
     */
    private record JornadasResponse(
            List<FixtureResponse> response
    ) {}

    /**
     * DTO para un fixture individual en respuesta de API.
     */
    private record FixtureResponse(
            Integer id,
            String round,
            LeagueResponse league,
            StatusResponse status,
            TeamsResponse teams,
            GoalsResponse goals
    ) {}

    private record LeagueResponse(Integer id, String name, Integer season) {}
    private record StatusResponse(String long_status, String short_status) {}
    private record TeamsResponse(TeamInfo home, TeamInfo away) {}
    private record TeamInfo(Integer id, String name) {}
    private record GoalsResponse(Integer home, Integer away) {}

    /**
     * Interface funcional para operaciones con retry.
     */
    @FunctionalInterface
    private interface ApiOperation<T> {
        T execute() throws Exception;
    }
}
