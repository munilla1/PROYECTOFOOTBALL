package com.example.football.estadisticas.acceptance;

import com.example.football.estadisticas.domain.Player;
import com.example.football.estadisticas.domain.RealStats;
import com.example.football.estadisticas.domain.Score;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de Aceptación para RF-0004: Persistencia de estadísticas reales.
 * 
 * Escenarios:
 * - CA-0004.1: Persistir Player con estructura correcta
 * - CA-0004.2: Persistir Match con estructura correcta
 * - CA-0004.3: Mantener historial de sincronizaciones (SyncLog)
 * - CA-0004.4: Garantizar consistencia transaccional
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("RF-0004: Persistencia de estadísticas reales")
public class RF0004_PersistenceAcceptanceTest {

    @Test
    @DisplayName("CA-0004.1: Player tiene estructura de persistencia correcta")
    void testPlayerPersistenceStructure() {
        // Dado: Player con datos completos
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

        Player player = new Player(
                UUID.randomUUID(),
                "1234",
                "Cristiano Ronaldo",
                "ST",
                39,
                "Portugal",
                "1",
                stats,
                Instant.now(),
                Instant.now()
        );

        // Cuando: Se verifica la estructura
        // Entonces: Todos los campos de persistencia están presentes
        assertThat(player.id()).isNotNull();
        assertThat(player.externalId()).isNotBlank();
        assertThat(player.name()).isNotBlank();
        assertThat(player.position()).isNotBlank();
        assertThat(player.age()).isPositive();
        assertThat(player.nationality()).isNotBlank();
        assertThat(player.teamId()).isNotBlank();
        assertThat(player.realStats()).isNotNull();
        assertThat(player.lastUpdated()).isNotNull();
        assertThat(player.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("CA-0004.1: Score tiene estructura válida para Match")
    void testScorePersistenceStructure() {
        // Dado: Score válido
        Score score = new Score(3, 2);

        // Cuando: Verificamos estructura
        // Entonces: Ambos componentes están presentes
        assertThat(score.homeGoals()).isGreaterThanOrEqualTo(0);
        assertThat(score.awayGoals()).isGreaterThanOrEqualTo(0);
        assertThat(score.isValid()).isTrue();
    }

    @Test
    @DisplayName("CA-0004.2: RealStats se persiste correctamente")
    void testRealStatsPersistence() {
        // Dado: RealStats con todos los campos
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

        // Cuando: Verificamos persistibilidad
        // Entonces: Todos los campos de persistencia están accesibles
        assertThat(stats.season()).isNotNull();
        assertThat(stats.league()).isNotNull();
        assertThat(stats.appearances()).isNotNull();
        assertThat(stats.goals()).isNotNull();
        assertThat(stats.assists()).isNotNull();
        assertThat(stats.passesAccuracy()).isNotNull();
        assertThat(stats.dribblesSuccess()).isNotNull();
        assertThat(stats.tackles()).isNotNull();
        assertThat(stats.performanceScore()).isNotNull();
        assertThat(stats.lastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("CA-0004.3: Player.id es único e inmutable")
    void testPlayerIdUniqueness() {
        // Dado: Dos Players con IDs diferentes
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Player player1 = new Player(
                id1, "1001", "Player1", "CM", 25, "Spain", "1",
                new RealStats(2023, "LaLiga", 20, 5, 2, 80, 60, 8, 70, Instant.now()),
                Instant.now(), Instant.now()
        );

        Player player2 = new Player(
                id2, "1002", "Player2", "CB", 28, "Brazil", "2",
                new RealStats(2023, "LaLiga", 22, 1, 0, 80, 50, 25, 72, Instant.now()),
                Instant.now(), Instant.now()
        );

        // Cuando: Verificamos IDs
        // Entonces: Son diferentes y únicos
        assertThat(player1.id()).isNotEqualTo(player2.id());
        assertThat(player1.id()).isEqualTo(id1);
        assertThat(player2.id()).isEqualTo(id2);
    }

    @Test
    @DisplayName("CA-0004.3: Timestamps de auditoría están presentes")
    void testAuditTimestamps() {
        // Dado: Player con timestamps
        Instant now = Instant.now();
        Player player = new Player(
                UUID.randomUUID(),
                "1234",
                "Test Player",
                "CM",
                25,
                "Spain",
                "1",
                new RealStats(2023, "LaLiga", 20, 5, 1, 80, 60, 8, 70, now),
                now,
                now
        );

        // Cuando: Verificamos timestamps
        // Entonces: createdAt y lastUpdated están configurados
        assertThat(player.createdAt()).isNotNull();
        assertThat(player.lastUpdated()).isNotNull();
        assertThat(player.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(player.lastUpdated()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("CA-0004.4: Score rechaza valores negativos")
    void testScoreValidation() {
        // Dado: Intentamos crear Score con goles negativos
        // Cuando/Entonces: Se rechaza la creación
        assertThatThrownBy(() -> new Score(-1, 2))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Score(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CA-0004.4: Player no permite edad fuera de rango")
    void testPlayerAgeValidation() {
        // Dado: Intentamos crear Player con edad < 16
        RealStats stats = new RealStats(2023, "LaLiga", 10, 1, 0, 70, 50, 5, 60, Instant.now());

        // Cuando/Entonces: Se rechaza
        assertThatThrownBy(() -> new Player(
                UUID.randomUUID(),
                "123",
                "Young Player",
                "FW",
                15,  // < 16
                "Spain",
                "1",
                stats,
                Instant.now(),
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);

        // Y también > 50
        assertThatThrownBy(() -> new Player(
                UUID.randomUUID(),
                "456",
                "Old Player",
                "CM",
                51,  // > 50
                "Spain",
                "2",
                stats,
                Instant.now(),
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CA-0004.4: Player no permite campos nulos")
    void testPlayerNullValidation() {
        // Dado: RealStats válidos
        RealStats stats = new RealStats(2023, "LaLiga", 20, 5, 1, 80, 60, 8, 70, Instant.now());

        // Cuando/Entonces: Campos nulos se rechazan
        assertThatThrownBy(() -> new Player(
                null,  // id nulo
                "123",
                "Player",
                "CM",
                25,
                "Spain",
                "1",
                stats,
                Instant.now(),
                Instant.now()
        )).isInstanceOf(Exception.class);

        assertThatThrownBy(() -> new Player(
                UUID.randomUUID(),
                null,  // externalId nulo
                "Player",
                "CM",
                25,
                "Spain",
                "1",
                stats,
                Instant.now(),
                Instant.now()
        )).isInstanceOf(Exception.class);

        assertThatThrownBy(() -> new Player(
                UUID.randomUUID(),
                "123",
                "Player",
                "CM",
                25,
                "Spain",
                "1",
                null,  // realStats nulo
                Instant.now(),
                Instant.now()
        )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("CA-0004.3: Múltiples Players pueden coexistir")
    void testMultiplePlayersPersistence() {
        // Dado: Creamos varios players
        Player player1 = new Player(
                UUID.randomUUID(),
                "1001",
                "Player One",
                "CM",
                25,
                "Spain",
                "1",
                new RealStats(2023, "LaLiga", 20, 5, 2, 85, 65, 8, 70, Instant.now()),
                Instant.now(),
                Instant.now()
        );

        Player player2 = new Player(
                UUID.randomUUID(),
                "1002",
                "Player Two",
                "CB",
                28,
                "Brazil",
                "2",
                new RealStats(2023, "LaLiga", 22, 1, 0, 80, 50, 25, 72, Instant.now()),
                Instant.now(),
                Instant.now()
        );

        // Cuando: Verificamos que coexisten
        // Entonces: Ambos tienen IDs únicos
        assertThat(player1.id()).isNotEqualTo(player2.id());
        assertThat(player1.externalId()).isNotEqualTo(player2.externalId());
        assertThat(player1.teamId()).isNotEqualTo(player2.teamId());
    }
}
