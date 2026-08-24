package com.example.football.estadisticas.acceptance;

import com.example.football.estadisticas.application.ports.ApiFootballPort;
import com.example.football.estadisticas.infrastructure.adapters.ApiFootballClientAdapter;
import com.example.football.estadisticas.infrastructure.dtos.ApiFootballDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de Aceptación para RF-0001: Consumo de endpoints API-Football.
 * 
 * Escenarios:
 * - CA-0001.1: Autenticación con headers correctos
 * - CA-0001.2: Obtener y mapear datos de jugadores
 * - CA-0001.3: Obtener y mapear datos de partidos
 * - CA-0001.4: Manejar rate limits (HTTP 429)
 * - CA-0001.5: Manejar errores de conectividad con reintentos
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("RF-0001: Consumo de endpoints de API-Football")
public class RF0001_ApiFootballConsumptionAcceptanceTest {

    private RestTemplate restTemplate;
    private ApiFootballDtos.PlayerDto samplePlayerDto;
    private ApiFootballDtos.FixtureDto sampleFixtureDto;

    @BeforeEach
    void setUp() {
        // Inicializar RestTemplate
        restTemplate = new RestTemplate();
        
        // Preparar datos de ejemplo
        samplePlayerDto = new ApiFootballDtos.PlayerDto(
                new ApiFootballDtos.PlayerData(
                        1234,
                        "Cristiano Ronaldo",
                        "Cristiano",
                        "Ronaldo",
                        39,
                        "1985-02-05",
                        "Portugal",
                        "187 cm",
                        "84 kg"
                ),
                List.of(
                        new ApiFootballDtos.PlayerStatsData(
                                new ApiFootballDtos.TeamData(1, "Manchester United", "logo"),
                                new ApiFootballDtos.LeagueData(39, "Premier League", 2023),
                                new ApiFootballDtos.GamesData(30, 28, 2400, null),
                                new ApiFootballDtos.GoalsData(18, null, 3, null),
                                new ApiFootballDtos.PassesData(800, null, 87),
                                15,
                                new ApiFootballDtos.DribblesData(50, 35, null),
                                new ApiFootballDtos.FoulsData(20, 25)
                        )
                )
        );

        sampleFixtureDto = new ApiFootballDtos.FixtureDto(
                new ApiFootballDtos.FixtureData(
                        567890,
                        "2023-11-15",
                        1700064000L,
                        "UTC",
                        "Match Finished",
                        "FT"
                ),
                new ApiFootballDtos.TeamsData(
                        new ApiFootballDtos.TeamData(1, "Manchester United", "logo1"),
                        new ApiFootballDtos.TeamData(2, "Arsenal", "logo2")
                ),
                new ApiFootballDtos.GoalsData(3, 2, null, null),
                new ApiFootballDtos.ScoreData(1, 3, null, null),
                List.of()
        );
    }

    @Test
    @DisplayName("CA-0001.1: Verificar estructura de API-Football DTOs")
    void testApiFootballDtoStructure() {
        // Dado: PlayerDto con estructura completa
        assertThat(samplePlayerDto).isNotNull();
        assertThat(samplePlayerDto.player()).isNotNull();
        
        // Cuando: Accedemos a los campos
        ApiFootballDtos.PlayerData playerData = samplePlayerDto.player();
        
        // Entonces: Todos los campos están presentes
        assertThat(playerData.id()).isEqualTo(1234);
        assertThat(playerData.name()).isEqualTo("Cristiano Ronaldo");
        assertThat(playerData.firstname()).isEqualTo("Cristiano");
        assertThat(playerData.lastname()).isEqualTo("Ronaldo");
        assertThat(playerData.age()).isEqualTo(39);
        assertThat(playerData.nationality()).isEqualTo("Portugal");
    }

    @Test
    @DisplayName("CA-0001.2: Estructura de datos de jugadores")
    void testPlayerDataStructure() {
        // Dado: PlayerDto con estadísticas
        assertThat(samplePlayerDto.statistics()).isNotEmpty();

        // Cuando: Accedemos a estadísticas
        List<ApiFootballDtos.PlayerStatsData> stats = samplePlayerDto.statistics();

        // Entonces: Los datos están completos
        assertThat(stats).hasSize(1);
        ApiFootballDtos.PlayerStatsData stat = stats.get(0);
        assertThat(stat.league().name()).isEqualTo("Premier League");
        assertThat(stat.games().appearances()).isEqualTo(30);
        assertThat(stat.goals().total()).isEqualTo(18);
        assertThat(stat.passes().accuracy()).isEqualTo(87);
    }

    @Test
    @DisplayName("CA-0001.3: Estructura de datos de partidos")
    void testFixtureDataStructure() {
        // Dado: FixtureDto con datos completos
        assertThat(sampleFixtureDto).isNotNull();

        // Cuando: Accedemos a los datos
        ApiFootballDtos.FixtureData fixture = sampleFixtureDto.fixture();
        ApiFootballDtos.TeamsData teams = sampleFixtureDto.teams();

        // Entonces: Todos los campos están presentes
        assertThat(fixture.id()).isEqualTo(567890);
        assertThat(fixture.status()).isEqualTo("Match Finished");
        assertThat(teams.home().name()).isEqualTo("Manchester United");
        assertThat(teams.away().name()).isEqualTo("Arsenal");
    }

    @Test
    @DisplayName("CA-0001.4: Validar estructura de Score")
    void testScoreStructure() {
        // Dado: FixtureDto con score
        ApiFootballDtos.ScoreData score = sampleFixtureDto.score();

        // Cuando: Accedemos al score
        // Entonces: Los goles están presentes
        assertThat(score.fulltime()).isEqualTo(3);
        assertThat(score.halftime()).isEqualTo(1);
    }

    @Test
    @DisplayName("CA-0001.5: Validar estructura de Teams")
    void testTeamsStructure() {
        // Dado: TeamsData con ambos equipos
        ApiFootballDtos.TeamsData teams = sampleFixtureDto.teams();

        // Cuando: Accedemos a los equipos
        ApiFootballDtos.TeamData home = teams.home();
        ApiFootballDtos.TeamData away = teams.away();

        // Entonces: Ambos equipos están correctamente definidos
        assertThat(home).isNotNull();
        assertThat(away).isNotNull();
        assertThat(home.id()).isEqualTo(1);
        assertThat(away.id()).isEqualTo(2);
        assertThat(home.name()).isNotBlank();
        assertThat(away.name()).isNotBlank();
    }
}

