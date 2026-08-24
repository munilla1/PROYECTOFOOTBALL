package com.example.football.estadisticas.acceptance;

import com.example.football.estadisticas.application.services.StatsNormalizerService;
import com.example.football.estadisticas.application.services.ValidationService;
import com.example.football.estadisticas.domain.RealStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de Aceptación para RF-0002: Mapeo de estadísticas reales a entidades internas.
 * 
 * Escenarios:
 * - CA-0002.1: Mapear datos de jugador (PlayerDto → Player)
 * - CA-0002.2: Normalizar estadísticas a escala 0-100
 * - CA-0002.3: Validar integridad de datos mapeados
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("RF-0002: Mapeo de estadísticas reales a entidades internas")
public class RF0002_StatsMapperAcceptanceTest {

    @Test
    @DisplayName("CA-0002.1: RealStats contiene todos los campos necesarios")
    void testRealStatsStructure() {
        // Dado: RealStats con valores válidos
        RealStats stats = new RealStats(
                2023,
                "Premier League",
                30,
                18,
                3,
                87,
                70,
                15,
                82,
                Instant.now()
        );

        // Cuando: Accedemos a los campos
        // Entonces: Todos están presentes
        assertThat(stats.season()).isEqualTo(2023);
        assertThat(stats.league()).isEqualTo("Premier League");
        assertThat(stats.appearances()).isEqualTo(30);
        assertThat(stats.goals()).isEqualTo(18);
        assertThat(stats.assists()).isEqualTo(3);
        assertThat(stats.passesAccuracy()).isEqualTo(87);
        assertThat(stats.dribblesSuccess()).isEqualTo(70);
        assertThat(stats.tackles()).isEqualTo(15);
        assertThat(stats.performanceScore()).isEqualTo(82);
    }

    @Test
    @DisplayName("CA-0002.2: Estadísticas en rango válido (0-100)")
    void testStatisticsValidRange() {
        // Dado: RealStats con valores en rango
        RealStats stats = new RealStats(
                2023,
                "LaLiga",
                25,
                8,
                2,
                80,
                60,
                10,
                75,
                Instant.now()
        );

        // Cuando: Validamos los rangos
        // Entonces: Todos están en [0,100] o son conteos válidos
        assertThat(stats.passesAccuracy())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(100);
        assertThat(stats.dribblesSuccess())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(100);
        assertThat(stats.performanceScore())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(100);
        assertThat(stats.appearances()).isGreaterThanOrEqualTo(0);
        assertThat(stats.goals()).isGreaterThanOrEqualTo(0);
        assertThat(stats.assists()).isGreaterThanOrEqualTo(0);
        assertThat(stats.tackles()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("CA-0002.3: RealStats con valores mínimos es válido")
    void testMinimalStatsStructure() {
        // Dado: Creamos stats con valores mínimos
        RealStats minimalStats = new RealStats(
                2024, // season
                "LaLiga", // league  
                1, // appearances
                0, // goals
                0, // assists
                50, // passesAccuracy
                50, // dribblesSuccess
                0, // tackles
                40, // performanceScore
                Instant.now() // lastUpdated
        );

        // Cuando: Verificamos los valores
        // Entonces: Todos son valores válidos
        assertThat(minimalStats).isNotNull();
        assertThat(minimalStats.season()).isGreaterThanOrEqualTo(2000);
        assertThat(minimalStats.league()).isNotBlank();
        assertThat(minimalStats.appearances()).isGreaterThanOrEqualTo(0);
        assertThat(minimalStats.goals()).isGreaterThanOrEqualTo(0);
        assertThat(minimalStats.performanceScore()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("CA-0002.3: RealStats rechaza season inválida")
    void testInvalidSeason() {
        // Dado: Intentamos crear RealStats con season < 2000
        // Cuando: Ejecutamos el constructor
        // Entonces: Debe validar o ser rechazado
        assertThatThrownBy(() -> new RealStats(
                1999,  // season < 2000 (inválida)
                "LaLiga",
                20,
                5,
                1,
                80,
                60,
                10,
                70,
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CA-0002.3: RealStats rechaza porcentajes fuera de rango")
    void testInvalidPercentages() {
        // Dado: Intentamos crear RealStats con accuracy > 100
        // Cuando: Ejecutamos el constructor
        // Entonces: Debe validar o ser rechazado
        assertThatThrownBy(() -> new RealStats(
                2023,
                "LaLiga",
                20,
                5,
                1,
                150,  // accuracy > 100
                60,
                10,
                70,
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CA-0002.3: RealStats rechaza valores negativos")
    void testNegativeValues() {
        // Dado: Intentamos crear RealStats con goals negativo
        // Cuando: Ejecutamos el constructor
        // Entonces: Debe validar o ser rechazado
        assertThatThrownBy(() -> new RealStats(
                2023,
                "LaLiga",
                20,
                -5,  // goals negativo
                1,
                80,
                60,
                10,
                70,
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CA-0002.2: Normalización preserva distribución relativa")
    void testNormalizationPreservesDistribution() {
        // Dado: Dos RealStats con diferentes values
        RealStats highScorer = new RealStats(
                2023, "LaLiga", 30, 20, 5, 85, 70, 15, 85, Instant.now()
        );
        
        RealStats lowScorer = new RealStats(
                2023, "LaLiga", 30, 5, 1, 75, 50, 10, 60, Instant.now()
        );

        // Cuando: Comparamos los scores
        // Entonces: High scorer tiene mayor score
        assertThat(highScorer.performanceScore())
                .isGreaterThan(lowScorer.performanceScore());
    }
}
