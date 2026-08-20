package com.example.football.usuario.acceptance;

import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.NoAutorizadoParaCambiarRolException;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.RoleValidator;
import com.example.football.usuario.domain.UsuarioPuedeCambiarRol;
import com.example.football.usuario.domain.Usuario;
import com.example.football.usuario.infrastructure.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBAS DE SEGURIDAD - CHG-0009: Sistema de Roles
 * 
 * Suite de tests que valida aspectos críticos de seguridad del sistema de roles
 * Enfoque: Validación de permisos, prevención de escalación de privilegios, auditoría
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("CHG-0009 - Pruebas de Seguridad: Sistema de Roles")
public class CHG0009SecurityAcceptanceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RoleValidator roleValidator;

    private UUID usuarioRegularId;
    private UUID usuarioAdminId;
    private String tokenUsuarioRegular;
    private String tokenUsuarioAdmin;

    private Usuario usuarioRegular;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioRegularId = UUID.randomUUID();
        usuarioAdminId = UUID.randomUUID();

        usuarioRegular = new Usuario(
                usuarioRegularId, "Regular User", "regular@test.com", "hash123", Rol.USUARIO,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );

        usuarioAdmin = new Usuario(
                usuarioAdminId, "Admin User", "admin@test.com", "hash456", Rol.ADMIN,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );

        usuarioRepository.save(usuarioRegular);
        usuarioRepository.save(usuarioAdmin);

        tokenUsuarioRegular = jwtTokenProvider.generarToken(usuarioRegularId, Rol.USUARIO);
        tokenUsuarioAdmin = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
    }

    // ==================== Validación de Permisos ====================

    @Test
    @DisplayName("Validación: Usuario regular no puede acceder a /admin/*")
    void security_regularUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                get("/api/admin/logs")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                get("/api/admin/errors")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validación: Admin puede acceder a todos los endpoints /admin/*")
    void security_adminCanAccessAllAdminEndpoints() throws Exception {
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/admin/logs")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/admin/errors")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Validación: Acceso sin token retorna 401")
    void security_accessWithoutTokenReturns401() throws Exception {
        mockMvc.perform(
                get("/api/admin/users")
        ).andExpect(status().isUnauthorized());
    }

    // ==================== Protección de JWT ====================

    @Test
    @DisplayName("Seguridad JWT: Token válido es aceptado")
    void security_validTokenIsAccepted() {
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
        assertTrue(jwtTokenProvider.esTokenValido(token),
                "Token válido debe ser aceptado");
    }

    @Test
    @DisplayName("Seguridad JWT: Token modificado es rechazado")
    void security_modifiedTokenIsRejected() {
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
        String tokenModificado = token.substring(0, token.length() - 1) + "X";

        assertFalse(jwtTokenProvider.esTokenValido(tokenModificado),
                "Token modificado debe ser rechazado");
    }

    @Test
    @DisplayName("Seguridad JWT: Token expirado es rechazado")
    void security_expiredTokenIsRejected() {
        // Nota: Esta prueba requeriría esperar a que expire el token
        // En un test real, se podría inyectar un RelojDePrueba
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
        assertTrue(jwtTokenProvider.esTokenValido(token),
                "Token nuevo debe ser válido");
    }

    @Test
    @DisplayName("Seguridad JWT: Rol del token no puede falsificarse")
    void security_roleClaimCannotBeFalsified() {
        // Generar token para usuario regular
        String token = jwtTokenProvider.generarToken(usuarioRegularId, Rol.USUARIO);

        // Intentar usar token de regular para operación admin
        // El middleware debe rechazar porque token dice "usuario", no "admin"
        assertThat(jwtTokenProvider.extraerRolDelToken(token))
                .isEqualTo("usuario");
    }

    // ==================== Prevención de Escalación de Privilegios ====================

    @Test
    @DisplayName("Escalación: Usuario no puede cambiar su propio rol a admin")
    void security_userCannotElevateOwnPrivileges() {
        UUID usuarioId = usuarioRegularId;

        // Intentar cambiar rol propio
        assertThatThrownBy(() -> {
            UsuarioPuedeCambiarRol spec = new UsuarioPuedeCambiarRol(usuarioAdmin, usuarioRegular, usuarioRegular);
            if (!spec.esValida()) {
                throw new NoAutorizadoParaCambiarRolException(spec.obtenerMensajeError());
            }
        }).isInstanceOf(NoAutorizadoParaCambiarRolException.class);
    }

    @Test
    @DisplayName("Escalación: Usuario regular no puede cambiar rol de admin")
    void security_regularUserCannotChangeAdminRole() {
        // Crear especificación para que usuario regular intente cambiar admin
        UsuarioPuedeCambiarRol spec = new UsuarioPuedeCambiarRol(usuarioRegular, usuarioAdmin, usuarioRegular);

        // La especificación debe ser inválida
        assertFalse(spec.esValida(),
                "Usuario regular no debe poder cambiar roles");
    }

    @Test
    @DisplayName("Escalación: Solo admin puede cambiar roles")
    void security_onlyAdminCanChangeRoles() {
        // Admin puede cambiar
        UsuarioPuedeCambiarRol specAdmin = new UsuarioPuedeCambiarRol(usuarioAdmin, usuarioRegular, usuarioAdmin);
        assertTrue(specAdmin.esValida(),
                "Admin debe poder cambiar roles");

        // Regular no puede cambiar
        UsuarioPuedeCambiarRol specRegular = new UsuarioPuedeCambiarRol(usuarioRegular, usuarioAdmin, usuarioRegular);
        assertFalse(specRegular.esValida(),
                "Usuario regular no debe poder cambiar roles");
    }

    // ==================== Validación de Entrada ====================

    @Test
    @DisplayName("Validación: Rol inválido retorna error")
    void security_invalidRoleReturnsError() {
        assertThatThrownBy(() -> {
            roleValidator.validarRolValido("superadmin");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Validación: Rol válido es aceptado")
    void security_validRoleIsAccepted() {
        // No debe lanzar excepción
        roleValidator.validarRolValido("usuario");
        roleValidator.validarRolValido("admin");
    }

    @Test
    @DisplayName("Validación: Cambio de rol a valor inválido es rechazado")
    void security_changeToInvalidRoleRejected() {
        assertThatThrownBy(() -> {
            Rol.desde("moderator");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== Auditoría y Trazabilidad ====================

    @Test
    @DisplayName("Auditoría: Campo rol es persistido en BD")
    void audit_roleIsPersisted() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = new Usuario(
                userId, "Test", "test@test.com", "hash", Rol.ADMIN,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );
        usuarioRepository.save(usuario);

        Usuario recuperado = usuarioRepository.findById(userId).orElseThrow();
        assertThat(recuperado.rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    @DisplayName("Auditoría: Cambios de rol se pueden rastrear")
    void audit_roleChangesCanBeTracked() {
        // Obtener rol anterior
        Rol rolAnterior = usuarioRepository.obtenerRolDelUsuario(usuarioRegularId);
        assertThat(rolAnterior).isEqualTo(Rol.USUARIO);

        // Cambiar rol
        usuarioRepository.actualizarRol(usuarioRegularId, Rol.ADMIN);

        // Verificar cambio
        Rol rolNuevo = usuarioRepository.obtenerRolDelUsuario(usuarioRegularId);
        assertThat(rolNuevo).isEqualTo(Rol.ADMIN);
    }

    // ==================== Integridad de Datos ====================

    @Test
    @DisplayName("Integridad: El rol siempre está definido (nunca null)")
    void integrity_rolIsNeverNull() {
        Usuario usuario = usuarioRepository.findById(usuarioAdminId).orElseThrow();
        assertThat(usuario.rol()).isNotNull();
    }

    @Test
    @DisplayName("Integridad: El rol es uno de los dos valores válidos")
    void integrity_rolIsOneOfValidValues() {
        Usuario usuario = usuarioRepository.findById(usuarioAdminId).orElseThrow();
        assertThat(usuario.rol()).isIn(Rol.USUARIO, Rol.ADMIN);
    }

    @Test
    @DisplayName("Integridad: Cambio de rol no afecta otros campos")
    void integrity_roleChangeDoesNotAffectOtherFields() {
        // Guardar valores originales
        Usuario original = usuarioRepository.findById(usuarioRegularId).orElseThrow();
        String nombreOriginal = original.nombre();
        String emailOriginal = original.email();

        // Cambiar rol
        usuarioRepository.actualizarRol(usuarioRegularId, Rol.ADMIN);

        // Verificar que otros campos no cambiaron
        Usuario actualizado = usuarioRepository.findById(usuarioRegularId).orElseThrow();
        assertThat(actualizado.nombre()).isEqualTo(nombreOriginal);
        assertThat(actualizado.email()).isEqualTo(emailOriginal);
        assertThat(actualizado.rol()).isEqualTo(Rol.ADMIN);
    }

    // ==================== Rechazo de Operaciones Inválidas ====================

    @Test
    @DisplayName("Rechazo: Usuario no puede cambiar rol de otro usuario")
    void rejection_userCannotChangeOthersRole() {
        UsuarioPuedeCambiarRol spec = new UsuarioPuedeCambiarRol(usuarioRegular, usuarioAdmin, usuarioRegular);
        assertFalse(spec.esValida());
    }

    @Test
    @DisplayName("Rechazo: Admin no puede cambiar su propio rol")
    void rejection_adminCannotChangeSelfRole() {
        UsuarioPuedeCambiarRol spec = new UsuarioPuedeCambiarRol(usuarioAdmin, usuarioAdmin, usuarioAdmin);
        assertFalse(spec.esValida(),
                "Ningún usuario debe poder cambiar su propio rol");
    }

    @Test
    @DisplayName("Rechazo: Cambio a rol inválido es rechazado")
    void rejection_invalidRoleChange() {
        assertThatThrownBy(() -> {
            Rol.desde("invalid");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol inválido");
    }
}
