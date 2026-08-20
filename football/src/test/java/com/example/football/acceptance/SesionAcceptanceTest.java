package com.example.football.acceptance;

import com.example.football.sesiones.infrastructure.SpringDataSesionRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.session.duration=PT1H",
    "app.session.inactivity-timeout=PT1S",
    "app.session.secure-cookie=true"
})
class SesionAcceptanceTest {
    private static final String PASSWORD = "Secreta-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataUsuarioRepository usuarioRepository;

    @Autowired
    private SpringDataSesionRepository sesionRepository;

    @BeforeEach
    void limpiarDatos() {
        sesionRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void iniciaSesionSinExponerCredencialesYPermiteVariasSesiones() throws Exception {
        String email = emailUnico();
        UUID usuarioId = registrar(email);

        String primerToken = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresAt").isString())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(cookie().value("football_session", org.hamcrest.Matchers.notNullValue()))
                .andExpect(cookie().httpOnly("football_session", true))
                .andExpect(cookie().secure("football_session", true))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(primerToken).get("token").asText();

        String segundoToken = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(token).isNotEqualTo(objectMapper.readTree(segundoToken).get("token").asText());
        assertThat(sesionRepository.count()).isEqualTo(2);
    }

    @Test
        void rechazaCredencialesInvalidasYUsuarioInexistenteSinCrearSesion() throws Exception {
        String email = emailUnico();
        registrar(email);

        login(email, "incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.credenciales-invalidas"))
                .andExpect(jsonPath("$.token").doesNotExist());
        login(emailUnico(), PASSWORD)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("usuario.no-existe"))
                .andExpect(jsonPath("$.token").doesNotExist());

        assertThat(sesionRepository.count()).isZero();
    }

    @Test
    void rechazaEmailInvalidoYCamposAusentesSinCrearSesion() throws Exception {
        mockMvc.perform(post("/api/sesiones/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-un-email\",\"password\":\"Secreta-123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("sesion.datos-invalidos"));

        mockMvc.perform(post("/api/sesiones/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jugador@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("sesion.datos-invalidos"));

        assertThat(sesionRepository.count()).isZero();
    }

    @Test
    void rechazaDatosDeLoginInvalidosComoSolicitudInvalida() throws Exception {
        mockMvc.perform(post("/api/sesiones/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("sesion.datos-invalidos"));

        assertThat(sesionRepository.count()).isZero();
    }

    @Test
    void protegeRutasYExponeSoloLaIdentidadAutenticada() throws Exception {
        String email = emailUnico();
        UUID usuarioId = registrar(email);
        String token = tokenDeLogin(email);

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.no-autenticado"));
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer alterado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.token-invalido"));
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId.toString()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void logoutEsIdempotenteRevocaSoloLaSesionCerrada() throws Exception {
        String email = emailUnico();
        registrar(email);
        String primerToken = tokenDeLogin(email);
        String segundoToken = tokenDeLogin(email);

        mockMvc.perform(post("/api/sesiones/logout").header("Authorization", "Bearer " + primerToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/sesiones/logout").header("Authorization", "Bearer " + primerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.token-invalido"));
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + primerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.token-invalido"));
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + segundoToken))
                .andExpect(status().isOk());
    }

        @Test
        void rechazaLogoutSinSesionAutenticada() throws Exception {
                mockMvc.perform(post("/api/sesiones/logout"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("sesion.no-autenticado"));
        }

    @Test
    void expiraSesionPorInactividadYNoLaReactiva() throws Exception {
        String email = emailUnico();
        registrar(email);
        String token = tokenDeLogin(email);
        Thread.sleep(1100);

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("sesion.expirada"));
        assertThat(sesionRepository.findByTokenHash(hashToken(token)).orElseThrow().getEstado().name())
                .isEqualTo("EXPIRADA");
    }

    private UUID registrar(String email) throws Exception {
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Jugador\",\"email\":\"" + email
                                + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private String tokenDeLogin(String email) throws Exception {
        return objectMapper.readTree(login(email, PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/sesiones/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String emailUnico() {
        return "sesion-%s@example.com".formatted(UUID.randomUUID());
    }

    private String hashToken(String token) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
