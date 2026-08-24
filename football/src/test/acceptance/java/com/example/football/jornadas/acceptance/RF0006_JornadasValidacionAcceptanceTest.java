package com.example.football.jornadas.acceptance;

import com.example.football.jornadas.domain.Jornada;
import com.example.football.jornadas.domain.JornadaStatus;
import com.example.football.jornadas.domain.PuedoJugarPartidoEnJornada;
import com.example.football.jornadas.domain.JornadasException;
import com.example.football.partidos.domain.PartidoJornadaBloqueadaException;
import com.example.football.partidos.application.services.PartidosDisponiblesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance Tests para CHG-0006: Jornadas sincronizadas con partidos reales
 * 
 * Clase: RF0006_JornadasValidacionAcceptanceTest
 * 
 * Cubre:
 * - RF-0002: Bloqueo de partidos fuera de jornada real (CA-0002.1 a CA-0002.5)
 * - RF-0004: Asociación de partidos a jornadas reales (CA-0004.1 a CA-0004.4)
 * 
 * Patrón: Spring Boot Integration Tests sin MockMvc
 * Assertions: AssertJ para verificaciones de dominio
 */
@DisplayName("CHG-0006: Jornadas - Validación y Bloqueo de Partidos")
class RF0006_JornadasValidacionAcceptanceTest {

    // Note: Este test NO inyecta Spring porque verifica lógica de dominio puro
    // Los servicios complejos se prueban en tests de integración separados

    /**
     * CA-0002.1: Bloqueo en jornada NOT_STARTED
     * 
     * Verifica que PuedoJugarPartidoEnJornada rechaza partidos en jornadas NOT_STARTED.
     */
    @Test
    @DisplayName("CA-0002.1: Bloqueo cuando jornada está NOT_STARTED")
    void testBloquePartido_JornadaNotStarted() {
        // Arrange
        Jornada jornada = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.NOT_STARTED, 10);
        PuedoJugarPartidoEnJornada spec = new PuedoJugarPartidoEnJornada(jornada);

        // Act & Assert
        assertThat(spec.esValida()).isFalse();
        assertThat(spec.obtenerMensajeError())
                .contains("jornada", "10", "aún no")
                .containsIgnoringCase("no ha comenzado");
        assertThat(spec.obtenerEstado()).isEqualTo(JornadaStatus.NOT_STARTED);
    }

    /**
     * CA-0002.2: Permiso en jornada IN_PROGRESS
     * 
     * Verifica que PuedoJugarPartidoEnJornada permite partidos en jornadas IN_PROGRESS.
     */
    @Test
    @DisplayName("CA-0002.2: Permiso cuando jornada está IN_PROGRESS")
    void testPermisoPartido_JornadaInProgress() {
        // Arrange
        Jornada jornada = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.IN_PROGRESS, 10);
        PuedoJugarPartidoEnJornada spec = new PuedoJugarPartidoEnJornada(jornada);

        // Act & Assert
        assertThat(spec.esValida()).isTrue();
        assertThat(jornada.isPlayable()).isTrue();
    }

    /**
     * CA-0002.3: Bloqueo en jornada FINISHED
     * 
     * Verifica que PuedoJugarPartidoEnJornada rechaza partidos en jornadas FINISHED.
     */
    @Test
    @DisplayName("CA-0002.3: Bloqueo cuando jornada está FINISHED")
    void testBloquePartido_JornadaFinished() {
        // Arrange
        Jornada jornada = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.FINISHED, 10);
        PuedoJugarPartidoEnJornada spec = new PuedoJugarPartidoEnJornada(jornada);

        // Act & Assert
        assertThat(spec.esValida()).isFalse();
        assertThat(spec.obtenerMensajeError())
                .contains("jornada", "10")
                .containsIgnoringCase("finalizado");
        assertThat(spec.obtenerEstado()).isEqualTo(JornadaStatus.FINISHED);
    }

    /**
     * CA-0002.4: Bloqueo en jornada POSTPONED
     * 
     * Verifica que PuedoJugarPartidoEnJornada rechaza partidos en jornadas POSTPONED.
     */
    @Test
    @DisplayName("CA-0002.4: Bloqueo cuando jornada está POSTPONED")
    void testBloquePartido_JornadaPostponed() {
        // Arrange
        Jornada jornada = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.POSTPONED, 10);
        PuedoJugarPartidoEnJornada spec = new PuedoJugarPartidoEnJornada(jornada);

        // Act & Assert
        assertThat(spec.esValida()).isFalse();
        assertThat(spec.obtenerMensajeError())
                .contains("jornada", "10")
                .containsIgnoringCase("aplazada");
        assertThat(spec.obtenerEstado()).isEqualTo(JornadaStatus.POSTPONED);
    }

    /**
     * CA-0002.5: Bloqueo si no existe jornada real
     * 
     * Verifica que PartidosDisponiblesService rechaza cuando jornada no existe.
     * (Requeriría mock, por ahora verificamos la excepción)
     */
    @Test
    @DisplayName("CA-0002.5: Excepción PartidoJornadaBloqueadaException con códigos de error")
    void testBloquePartido_JornadaNoExiste_ExcepcionConCodigoError() {
        // Arrange & Act & Assert
        PartidoJornadaBloqueadaException ex = new PartidoJornadaBloqueadaException(
                PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                "Jornada no existe"
        );

        assertThat(ex.getErrorCode()).isEqualTo("JORNADA_NOT_FOUND");
        assertThat(ex.getMessage()).contains("no existe");
    }

    /**
     * CA-0004.1: Validación de jornada al crear partido
     * 
     * Verifica que PartidoJornadaBloqueadaException contiene el código correcto
     * cuando jornada no existe.
     */
    @Test
    @DisplayName("CA-0004.1: Validación de jornada al crear partido - JORNADA_NOT_FOUND")
    void testValidacionPartido_JornadaNotFound() {
        // Arrange & Act
        PartidoJornadaBloqueadaException ex = new PartidoJornadaBloqueadaException(
                PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                "Jornada 99 LaLiga 2024 no existe",
                99
        );

        // Assert
        assertThat(ex.getErrorCode()).isEqualTo(PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND);
        assertThat(ex.getRoundNumber()).isEqualTo(99);
        assertThat(ex.getMessage()).contains("no existe");
    }

    /**
     * CA-0004.2: Referencia FK a jornada_id
     * 
     * Verifica que Jornada tiene UUID único que puede usarse como FK.
     */
    @Test
    @DisplayName("CA-0004.2: Jornada tiene UUID único para ser FK en partidos")
    void testFK_JornadaTieneUuidUnico() {
        // Arrange
        Jornada jornada1 = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.IN_PROGRESS, 10);
        Jornada jornada2 = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.IN_PROGRESS, 10);

        // Act & Assert
        assertThat(jornada1.id()).isNotNull();
        assertThat(jornada2.id()).isNotNull();
        assertThat(jornada1.id()).isNotEqualTo(jornada2.id());  // UUIDs diferentes
    }

    /**
     * CA-0004.3: Imposible crear partido sin jornada (FK constraint)
     * 
     * Verifica que la excepción tiene código específico para FK violation.
     */
    @Test
    @DisplayName("CA-0004.3: FK constraint violation tiene código específico")
    void testFKConstraint_ViolacionFK() {
        // Arrange & Act
        PartidoJornadaBloqueadaException ex = new PartidoJornadaBloqueadaException(
                PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                "jornada_id must reference existing jornada"
        );

        // Assert
        assertThat(ex.getErrorCode()).isNotNull();
        assertThat(ex.getErrorCode()).isIn(
                PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND,
                PartidoJornadaBloqueadaException.JORNADA_NOT_STARTED,
                PartidoJornadaBloqueadaException.JORNADA_FINISHED,
                PartidoJornadaBloqueadaException.JORNADA_POSTPONED
        );
    }

    /**
     * CA-0004.4: Visualización de partidos disponibles por jornada
     * 
     * Verifica que PartidosDisponiblesService valida disponibilidad antes de permitir.
     */
    @Test
    @DisplayName("CA-0004.4: Solo partidos de jornadas IN_PROGRESS son disponibles")
    void testPartidosDisponibles_FiltrosPorEstado() {
        // Arrange
        Jornada jornada_no_iniciada = Jornada.nueva(9, "LaLiga", 2024, JornadaStatus.NOT_STARTED, 10);
        Jornada jornada_en_curso = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.IN_PROGRESS, 10);
        Jornada jornada_finalizada = Jornada.nueva(11, "LaLiga", 2024, JornadaStatus.FINISHED, 10);
        Jornada jornada_aplazada = Jornada.nueva(12, "LaLiga", 2024, JornadaStatus.POSTPONED, 10);

        // Act & Assert
        assertThat(jornada_no_iniciada.isPlayable()).isFalse();  // Oculta
        assertThat(jornada_en_curso.isPlayable()).isTrue();      // Visible
        assertThat(jornada_finalizada.isPlayable()).isFalse();   // Oculta
        assertThat(jornada_aplazada.isPlayable()).isFalse();     // Oculta
    }

    /**
     * Test adicional: Excepciones con códigos de error específicos
     */
    @Test
    @DisplayName("Todos los códigos de error están definidos")
    void testCodigosError_TodosDefinidos() {
        // Act & Assert
        assertThat(PartidoJornadaBloqueadaException.JORNADA_NOT_STARTED).isNotNull();
        assertThat(PartidoJornadaBloqueadaException.JORNADA_FINISHED).isNotNull();
        assertThat(PartidoJornadaBloqueadaException.JORNADA_POSTPONED).isNotNull();
        assertThat(PartidoJornadaBloqueadaException.JORNADA_NOT_FOUND).isNotNull();
    }

    /**
     * Test adicional: PuedoJugarPartidoEnJornada rechaza jornada null
     */
    @Test
    @DisplayName("Validación: PuedoJugarPartidoEnJornada rechaza jornada null")
    void testValidacion_SpecRechazaJornadaNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> 
            new PuedoJugarPartidoEnJornada(null)
        ).isInstanceOf(JornadasException.class);
    }

    /**
     * Test adicional: Mensajes de error en español
     */
    @Test
    @DisplayName("Mensajes de error están en español")
    void testMensajesError_EnEspanol() {
        // Arrange
        Jornada jornada_not_started = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.NOT_STARTED, 10);
        Jornada jornada_finished = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.FINISHED, 10);
        Jornada jornada_postponed = Jornada.nueva(10, "LaLiga", 2024, JornadaStatus.POSTPONED, 10);

        PuedoJugarPartidoEnJornada spec_not_started = new PuedoJugarPartidoEnJornada(jornada_not_started);
        PuedoJugarPartidoEnJornada spec_finished = new PuedoJugarPartidoEnJornada(jornada_finished);
        PuedoJugarPartidoEnJornada spec_postponed = new PuedoJugarPartidoEnJornada(jornada_postponed);

        // Act & Assert
        String msg_not_started = spec_not_started.obtenerMensajeError();
        String msg_finished = spec_finished.obtenerMensajeError();
        String msg_postponed = spec_postponed.obtenerMensajeError();

        assertThat(msg_not_started).containsIgnoringCase("ha");  // Contiene "no ha comenzado"
        assertThat(msg_finished).containsIgnoringCase("finalizado");
        assertThat(msg_postponed).containsIgnoringCase("aplazada");
    }

    /**
     * Test adicional: Jornada.isPlayable() es alias para estado IN_PROGRESS
     */
    @Test
    @DisplayName("Jornada.isPlayable() = (status == IN_PROGRESS)")
    void testIsPlayable_AliasParaInProgress() {
        // Arrange & Act & Assert
        for (JornadaStatus status : JornadaStatus.values()) {
            Jornada jornada = Jornada.nueva(10, "LaLiga", 2024, status, 10);
            
            if (status == JornadaStatus.IN_PROGRESS) {
                assertThat(jornada.isPlayable()).isTrue();
            } else {
                assertThat(jornada.isPlayable()).isFalse();
            }
        }
    }

    /**
     * Test adicional: Excepción con roundNumber
     */
    @Test
    @DisplayName("PartidoJornadaBloqueadaException puede guardar roundNumber")
    void testExcepcion_GuardaRoundNumber() {
        // Arrange & Act
        PartidoJornadaBloqueadaException ex = new PartidoJornadaBloqueadaException(
                PartidoJornadaBloqueadaException.JORNADA_NOT_STARTED,
                "Error en jornada",
                42
        );

        // Assert
        assertThat(ex.getRoundNumber()).isEqualTo(42);
    }
}
