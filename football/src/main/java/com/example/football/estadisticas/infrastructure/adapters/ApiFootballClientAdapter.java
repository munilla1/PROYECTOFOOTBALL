package com.example.football.estadisticas.infrastructure.adapters;

import com.example.football.estadisticas.application.ports.ApiConnectivityException;
import com.example.football.estadisticas.application.ports.ApiFootballPort;
import com.example.football.estadisticas.application.ports.RateLimitExceededException;
import com.example.football.estadisticas.infrastructure.dtos.ApiFootballDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador: Cliente HTTP para consumir API-Football.
 * 
 * Implementa ApiFootballPort usando RestTemplate.
 * Maneja autenticación, rate limiting y reintentos exponenciales.
 */
@Service
public class ApiFootballClientAdapter implements ApiFootballPort {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiHost;
    private final String baseUrl;
    private final long timeoutSeconds;
    private final int maxRetries;
    private final long initialRetryDelayMs;

    public ApiFootballClientAdapter(
        RestTemplate restTemplate,
        @Value("${api.football.key:test-key}") String apiKey,
        @Value("${api.football.host:api-football-v3.p.rapidapi.com}") String apiHost,
        @Value("${api.football.base-url:https://api-football-v3.p.rapidapi.com}") String baseUrl,
        @Value("${api.football.timeout.seconds:10}") long timeoutSeconds,
        @Value("${api.football.max-retries:3}") int maxRetries,
        @Value("${api.football.retry-delay-ms:1000}") long initialRetryDelayMs)
 {

        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.baseUrl = baseUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
        this.initialRetryDelayMs = initialRetryDelayMs;
    }

    /**
     * Obtiene lista de jugadores de una liga en una temporada.
     */
    @Override
    public List<ApiFootballDtos.PlayerDto> getPlayers(String league, Integer season)
            throws ApiConnectivityException, RateLimitExceededException {

        String endpoint = "/players";
        String url = String.format("%s%s?league=%s&season=%d", baseUrl, endpoint, league, season);

        try {
            // Realizar llamada con reintentos
            ApiFootballDtos.PlayerDto[] response = executeWithRetry(
                    url,
                    ApiFootballDtos.PlayerDto[].class,
                    endpoint,
                    0
            );

            return response != null ? List.of(response) : Collections.emptyList();
        } catch (HttpClientErrorException.TooManyRequests e) {
            Instant retryAfter = parseRetryAfter(e);
            throw new RateLimitExceededException(
                    "Rate limit exceeded en API-Football",
                    retryAfter,
                    maxRetries,
                    endpoint
            );
        } catch (Exception e) {
            throw new ApiConnectivityException("Error al obtener jugadores de API-Football: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene lista de partidos de una jornada.
     */
    @Override
    public List<ApiFootballDtos.FixtureDto> getFixtures(String league, Integer season, String round)
            throws ApiConnectivityException, RateLimitExceededException {

        String endpoint = "/fixtures";
        String url = String.format("%s%s?league=%s&season=%d&round=%s", 
                baseUrl, endpoint, league, season, round);

        try {
            // Realizar llamada con reintentos
            ApiFootballDtos.FixtureDto[] response = executeWithRetry(
                    url,
                    ApiFootballDtos.FixtureDto[].class,
                    endpoint,
                    0
            );

            return response != null ? List.of(response) : Collections.emptyList();
        } catch (HttpClientErrorException.TooManyRequests e) {
            Instant retryAfter = parseRetryAfter(e);
            throw new RateLimitExceededException(
                    "Rate limit exceeded en API-Football",
                    retryAfter,
                    maxRetries,
                    endpoint
            );
        } catch (Exception e) {
            throw new ApiConnectivityException("Error al obtener partidos de API-Football: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene estadísticas detalladas de un jugador en específico.
     */
    @Override
    public ApiFootballDtos.PlayerDto getPlayerStats(Integer playerId, String league, Integer season)
            throws ApiConnectivityException, RateLimitExceededException {

        String endpoint = "/players";
        String url = String.format("%s%s?id=%d&league=%s&season=%d", 
                baseUrl, endpoint, playerId, league, season);

        try {
            // Realizar llamada con reintentos
            ApiFootballDtos.PlayerDto[] response = executeWithRetry(
                    url,
                    ApiFootballDtos.PlayerDto[].class,
                    endpoint,
                    0
            );

            return (response != null && response.length > 0) ? response[0] : null;
        } catch (HttpClientErrorException.TooManyRequests e) {
            Instant retryAfter = parseRetryAfter(e);
            throw new RateLimitExceededException(
                    "Rate limit exceeded en API-Football",
                    retryAfter,
                    maxRetries,
                    endpoint
            );
        } catch (Exception e) {
            throw new ApiConnectivityException("Error al obtener stats de jugador de API-Football: " + e.getMessage(), e);
        }
    }

    // ========== Métodos auxiliares ==========

    /**
     * Ejecuta una llamada HTTP con reintentos exponenciales y backoff.
     */
    @SuppressWarnings("unchecked")
    private <T> T executeWithRetry(String url, Class<T> responseType, String endpoint, int attempt)
            throws Exception {

        try {
            // Agregar headers de autenticación
            HttpEntity<?> request = new HttpEntity<>(createHeaders());

            // Realizar llamada
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    responseType
            );

            return response.getBody();

        } catch (HttpClientErrorException.TooManyRequests e) {
            // Rate limit: fallar inmediatamente
            throw e;

        } catch (Exception e) {
            // Reintentar con backoff exponencial
            if (attempt < maxRetries) {
                long delayMs = initialRetryDelayMs * (long) Math.pow(2, attempt);
                Thread.sleep(delayMs);
                return executeWithRetry(url, responseType, endpoint, attempt + 1);
            }
            throw e;
        }
    }

    /**
     * Crea headers HTTP necesarios para autenticarse con API-Football.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", apiHost);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Parsea el header Retry-After para obtener cuando reintentar.
     */
    private Instant parseRetryAfter(HttpClientErrorException e) {
        try {
            String retryAfter = e.getResponseHeaders().getFirst("Retry-After");
            if (retryAfter != null) {
                // Intentar parsear como segundos (formato numérico)
                try {
                    long seconds = Long.parseLong(retryAfter);
                    return Instant.now().plus(seconds, ChronoUnit.SECONDS);
                } catch (NumberFormatException ex) {
                    // Si no es numérico, asumir que es una fecha HTTP
                    return Instant.now().plus(60, ChronoUnit.SECONDS); // Default 60 segundos
                }
            }
        } catch (Exception ex) {
            // En caso de error, retornar un tiempo razonable por defecto
        }

        return Instant.now().plus(60, ChronoUnit.SECONDS);
    }
}
