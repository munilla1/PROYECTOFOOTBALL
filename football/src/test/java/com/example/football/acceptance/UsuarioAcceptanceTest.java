package com.example.football.acceptance;

import com.example.football.usuario.application.PasswordHasher;
import com.example.football.usuario.infrastructure.SpringDataUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioAcceptanceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @BeforeEach
    void limpiarUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void registrarUsuarioInicializaProgresoMembresiaYProtegeContrasena() throws Exception {
        String password = "Secreta-123";
        String email = emailUnico();

        String body = """
                {"nombre":"Ada","email":"%s","password":"%s"}
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ada"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.membresia").value("TRIAL"))
                .andExpect(jsonPath("$.nivel").value(1))
                .andExpect(jsonPath("$.xp").value(0))
                .andExpect(jsonPath("$.energia").value(100))
                .andExpect(jsonPath("$.estadoJugador").value("NORMAL"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());
        var persisted = usuarioRepository.findById(id).orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo(password);
        assertThat(passwordHasher.matches(password, persisted.getPasswordHash())).isTrue();
        assertThat(persisted.getFechaExpiracionMembresia()).isEqualTo(persisted.getFechaInicioTrial().plusSeconds(7 * 24 * 60 * 60));
    }

    @Test
    void rechazaEmailDuplicadoSinCrearSegundoUsuario() throws Exception {
        String email = emailUnico();
        registrar(email, "Primero");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(email, "Segundo")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("usuario.email-duplicado"));

        assertThat(usuarioRepository.findAll()).hasSize(1);
    }

    @Test
    void recuperaElProgresoPersistidoDelUsuarioAutenticado() throws Exception {
        UUID id = registrar(emailUnico(), "Jugador");

        mockMvc.perform(put("/api/usuarios/me/progreso")
                        .principal(principal(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nivel":4,"xp":850,"energia":63,"estado":"LESIONADO","historialPartidos":"[1,2]"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usuarios/me").principal(principal(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value(4))
                .andExpect(jsonPath("$.xp").value(850))
                .andExpect(jsonPath("$.energia").value(63))
                .andExpect(jsonPath("$.estadoJugador").value("LESIONADO"))
                .andExpect(jsonPath("$.historialPartidos").value("[1,2]"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void actualizacionParcialConservaLosCamposNoModificados() throws Exception {
        UUID id = registrar(emailUnico(), "Jugador");

        mockMvc.perform(put("/api/usuarios/me/progreso")
                        .principal(principal(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"energia\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energia").value(40))
                .andExpect(jsonPath("$.nivel").value(1))
                .andExpect(jsonPath("$.xp").value(0))
                .andExpect(jsonPath("$.estadoJugador").value("NORMAL"));
    }

    @Test
    void rechazaProgresoInvalidoYConservaElUltimoEstadoConfirmado() throws Exception {
        UUID id = registrar(emailUnico(), "Jugador");

        mockMvc.perform(put("/api/usuarios/me/progreso")
                        .principal(principal(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"energia\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("usuario.datos-invalidos"));

        mockMvc.perform(get("/api/usuarios/me").principal(principal(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energia").value(100));
    }

    @Test
    void bloqueaAccesoSinSesionYAccesoAlProgresoDeOtroUsuario() throws Exception {
        UUID propietario = registrar(emailUnico(), "Propietario");
        UUID otroUsuario = registrar(emailUnico(), "Otro");

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.no-autenticado"));

        mockMvc.perform(get("/api/usuarios/{id}", otroUsuario).principal(principal(propietario)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("usuario.no-autorizado"));
    }

    @Test
    void devuelveNoEncontradoSinCrearUsuario() throws Exception {
        UUID inexistente = UUID.randomUUID();

        mockMvc.perform(get("/api/usuarios/me").principal(principal(inexistente)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("usuario.progreso-no-encontrado"));

        assertThat(usuarioRepository.findById(inexistente)).isEmpty();
    }

    private UUID registrar(String email, String nombre) throws Exception {
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(email, nombre)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private String registroJson(String email, String nombre) {
        return "{\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"Secreta-123\"}".formatted(nombre, email);
    }

    private String emailUnico() {
        return "usuario-%s@example.com".formatted(UUID.randomUUID());
    }

    private Principal principal(UUID id) {
        return () -> id.toString();
    }
}
