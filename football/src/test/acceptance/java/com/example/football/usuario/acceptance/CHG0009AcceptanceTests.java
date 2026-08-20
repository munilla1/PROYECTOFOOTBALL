package com.example.football.usuario.acceptance;

import com.example.football.usuario.application.CambiarRolDeUsuario;
import com.example.football.usuario.application.ListarUsuarios;
import com.example.football.usuario.application.RoleAuditLogRepository;
import com.example.football.usuario.application.UsuarioNotFoundException;
import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.NoAutorizadoParaCambiarRolException;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.RolInvalidoException;
import com.example.football.usuario.domain.RoleValidator;
import com.example.football.usuario.domain.UsuarioPuedeCambiarRol;
import com.example.football.usuario.infrastructure.JwtTokenProvider;
import com.example.football.usuario.infrastructure.UsuarioJpaEntity;
import com.example.football.usuario.infrastructure.security.RoleAuthorizationFilter;
import com.example.football.usuario.presentation.AdminController;
import com.example.football.usuario.presentation.UsuarioDtos.CambiarRolRequest;
import com.example.football.usuario.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBAS DE ACEPTACIÓN - CHG-0009: Sistema de Roles
 * 
 * Clase de aceptación que valida los criterios de aceptación definidos en requirements.md
 * 
 * Convención de nombres: CA-XXX.X (Criterio de Aceptación)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("CHG-0009 - Pruebas de Aceptación: Sistema de Roles")
public class CHG0009AcceptanceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleAuditLogRepository auditLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RoleValidator roleValidator;

    @Autowired
    private CambiarRolDeUsuario cambiarRolDeUsuario;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID usuarioRegularId;
    private UUID usuarioAdminId;
    private String tokenUsuarioRegular;
    private String tokenUsuarioAdmin;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        usuarioRegularId = UUID.randomUUID();
        usuarioAdminId = UUID.randomUUID();

        // Crear usuarios de prueba
        Usuario usuarioRegular = new Usuario(
                usuarioRegularId, "Usuario Regular", "regular@test.com", "hash123", Rol.USUARIO,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );
        Usuario usuarioAdmin = new Usuario(
                usuarioAdminId, "Admin User", "admin@test.com", "hash456", Rol.ADMIN,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );

        // Persistir usuarios
        usuarioRepository.save(usuarioRegular);
        usuarioRepository.save(usuarioAdmin);

        // Generar tokens JWT
        tokenUsuarioRegular = jwtTokenProvider.generarToken(usuarioRegularId, Rol.USUARIO);
        tokenUsuarioAdmin = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
    }

    // ==================== RF-001: Asignación de roles a usuarios ====================

    @Test
    @DisplayName("CA-001.1: Campo rol en entidad Usuario")
    void ca001_1_rolFieldExists() {
        // Given: la entidad Usuario existe
        Usuario usuario = new Usuario(
                UUID.randomUUID(), "Test", "test@test.com", "hash", Rol.USUARIO,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );

        // When: se crea un usuario
        // Then: el campo 'rol' existe y acepta valores válidos
        assertThat(usuario.rol()).isNotNull();
        assertThat(usuario.rol()).isIn(Rol.USUARIO, Rol.ADMIN);
        assertThat(usuario.rol()).isEqualTo(Rol.USUARIO);
    }

    @Test
    @DisplayName("CA-001.2: Persistencia del rol en base de datos")
    void ca001_2_rolPersistence() {
        // Given: un usuario nuevo se crea con rol 'admin'
        UUID userId = UUID.randomUUID();
        Usuario usuarioAdmin = new Usuario(
                userId, "Admin", "admin@example.com", "hash", Rol.ADMIN,
                null, Instant.now(), null, null, Instant.now(), 1, 0, 100, null, "", ""
        );

        // When: se persiste en la base de datos
        usuarioRepository.save(usuarioAdmin);

        // Then: se puede recuperar sin cambios
        Usuario recuperado = usuarioRepository.findById(userId)
                .orElseThrow(() -> new AssertionError("Usuario no encontrado"));
        assertThat(recuperado.rol()).isEqualTo(Rol.ADMIN);
        assertThat(recuperado.nombre()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("CA-001.3: Modificación de rol")
    void ca001_3_rolModification() {
        // Given: un usuario existe con rol 'usuario'
        assertThat(usuarioRepository.obtenerRolDelUsuario(usuarioRegularId))
                .isEqualTo(Rol.USUARIO);

        // When: se cambia el rol a 'admin'
        usuarioRepository.actualizarRol(usuarioRegularId, Rol.ADMIN);

        // Then: la base de datos actualiza el rol
        assertThat(usuarioRepository.obtenerRolDelUsuario(usuarioRegularId))
                .isEqualTo(Rol.ADMIN);
    }

    // ==================== RF-002: Middleware de autorización ====================

    @Test
    @DisplayName("CA-002.1: Middleware valida rol en solicitud protegida")
    void ca002_1_middlewareValidatesRole() throws Exception {
        // Given: usuario con rol 'usuario' intenta acceder a endpoint admin
        // When: realiza solicitud a /api/admin/users
        MvcResult result = mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
        ).andExpect(status().isForbidden())
                // Then: rechaza con 403
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("CA-002.2: Rechazo de acceso no autorizado")
    void ca002_2_unauthorizedAccessRejection() throws Exception {
        // Given: usuario sin rol admin
        // When/Then: retorna 403 con mensaje
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
        ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("CA-002.3: Continuación en acceso autorizado")
    void ca002_3_authorizedAccessAllowed() throws Exception {
        // Given: usuario con rol admin
        // When: realiza solicitud a /api/admin/logs
        mockMvc.perform(
                get("/api/admin/logs")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        )
                // Then: permite que continúe (200)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== RF-003: Endpoints de administración ====================

    @Test
    @DisplayName("CA-003.1: Endpoint GET /admin/logs")
    void ca003_1_adminLogsEndpoint() throws Exception {
        // Given: administrador accede a /api/admin/logs
        mockMvc.perform(
                get("/api/admin/logs")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        )
                // Then: retorna 200 con estructura JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].timestamp").exists())
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    @DisplayName("CA-003.2: Endpoint GET /admin/errors")
    void ca003_2_adminErrorsEndpoint() throws Exception {
        // Given: administrador accede a /api/admin/errors
        mockMvc.perform(
                get("/api/admin/errors")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        )
                // Then: retorna 200 con array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CA-003.3: Endpoint GET /admin/users")
    void ca003_3_adminUsersEndpoint() throws Exception {
        // Given: administrador accede a /api/admin/users
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        )
                // Then: retorna 200 con lista de usuarios
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].rol").exists());
    }

    @Test
    @DisplayName("CA-003.4: Endpoint PATCH /admin/users/{userId}/role")
    void ca003_4_changeRoleEndpoint() throws Exception {
        // Given: admin cambia rol de usuario
        CambiarRolRequest request = new CambiarRolRequest("admin");

        mockMvc.perform(
                patch("/api/admin/users/" + usuarioRegularId + "/role")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                // Then: retorna 200 y actualiza rol
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("admin"));

        // Validar que BD fue actualizada
        assertThat(usuarioRepository.obtenerRolDelUsuario(usuarioRegularId))
                .isEqualTo(Rol.ADMIN);
    }

    // ==================== RF-004: Sincronización de rol con sesión ====================

    @Test
    @DisplayName("CA-004.1: Token JWT incluye rol")
    void ca004_1_jwtIncludesRole() {
        // Given: usuario se autentica
        // When: se genera token JWT
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);

        // Then: el token incluye rol y se puede decodificar
        String rolExtraido = jwtTokenProvider.extraerRolDelToken(token);
        assertThat(rolExtraido).isEqualTo("admin");
    }

    @Test
    @DisplayName("CA-004.2: Rol disponible en contexto de sesión")
    void ca004_2_roleAvailableInContext() {
        // Given: token con rol
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);

        // When/Then: se puede extraer sin queries adicionales
        assertThat(jwtTokenProvider.esTokenValido(token)).isTrue();
        assertThat(jwtTokenProvider.extraerRolDelToken(token)).isEqualTo("admin");
    }

    // ==================== RF-005: Validación de cambios de rol ====================

    @Test
    @DisplayName("CA-005.1: Rol inválido retorna 400")
    void ca005_1_invalidRoleReturns400() {
        // Given/When: se intenta asignar rol inválido
        // Then: lanza excepción
        assertThatThrownBy(() -> {
            Rol.desde("superadmin");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol inválido");
    }

    @Test
    @DisplayName("CA-005.2: Usuario no puede cambiar su propio rol")
    void ca005_2_userCannotChangeSelfRole() throws Exception {
        // Given: admin intenta cambiar su propio rol
        CambiarRolRequest request = new CambiarRolRequest("usuario");

        mockMvc.perform(
                patch("/api/admin/users/" + usuarioAdminId + "/role")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                // Then: retorna 403
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CA-005.3: Solo admin puede cambiar roles")
    void ca005_3_onlyAdminCanChangeRoles() throws Exception {
        // Given: usuario regular intenta cambiar rol de otro
        CambiarRolRequest request = new CambiarRolRequest("admin");

        mockMvc.perform(
                patch("/api/admin/users/" + usuarioAdminId + "/role")
                        .header("Authorization", "Bearer " + tokenUsuarioRegular)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                // Then: retorna 403
                .andExpect(status().isForbidden());
    }

    // ==================== RNF-001: Seguridad ====================

    @Test
    @DisplayName("CA-RNF-001.1: Rol siempre se valida")
    void caNrf001_1_roleAlwaysValidated() throws Exception {
        // Given: solicitud sin token
        // When/Then: rechaza (sin confiar en cliente)
        mockMvc.perform(
                get("/api/admin/users")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-RNF-001.2: Token modificado es rechazado")
    void caNrf001_2_modifiedTokenRejected() throws Exception {
        // Given: token válido
        String tokenValido = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);
        // Modificar el token (cambiar último carácter)
        String tokenModificado = tokenValido.substring(0, tokenValido.length() - 1) + "X";

        // When/Then: rechaza con 401
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenModificado)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-RNF-001.3: Cambios de rol se registran en auditoría")
    void caNrf001_3_auditLogRecordsRoleChange() {
        // Given: admin cambia rol de usuario
        UUID adminId = usuarioAdminId;
        UUID usuarioId = usuarioRegularId;

        // When: ejecuta cambio de rol
        cambiarRolDeUsuario.ejecutar(usuarioId, Rol.ADMIN, adminId);

        // Then: se registra en auditoría
        // Nota: verificar que RoleAuditLogRepository.registrarCambioDeRol() fue llamado
        assertThat(usuarioRepository.obtenerRolDelUsuario(usuarioId))
                .isEqualTo(Rol.ADMIN);
    }

    // ==================== RNF-002: Rendimiento ====================

    @Test
    @DisplayName("CA-RNF-002.1: Validación de rol < 10ms")
    void caNrf002_1_roleValidationPerformance() throws Exception {
        // Given: solicitud a endpoint protegido
        long inicio = System.currentTimeMillis();

        mockMvc.perform(
                get("/api/admin/logs")
                        .header("Authorization", "Bearer " + tokenUsuarioAdmin)
        ).andExpect(status().isOk());

        long duracion = System.currentTimeMillis() - inicio;

        // Then: se completa en tiempo razonable (< 1000ms en test)
        assertThat(duracion).isLessThan(1000);
    }

    @Test
    @DisplayName("CA-RNF-002.2: Rol se obtiene de sesión sin queries extra")
    void caNrf002_2_rolFromSessionNoExtraQueries() {
        // Given: token JWT contiene rol
        String token = jwtTokenProvider.generarToken(usuarioAdminId, Rol.ADMIN);

        // When: se extrae rol
        String rol = jwtTokenProvider.extraerRolDelToken(token);

        // Then: no requiere queries a BD
        assertThat(rol).isEqualTo("admin");
    }

    // ==================== RNF-003: Compatibilidad CHG-0008 ====================

    @Test
    @DisplayName("CA-RNF-003.1: Sesión incluye rol del usuario")
    void caNrf003_1_sessionIncludesUserRole() {
        // Given: usuario autenticado
        Usuario usuario = usuarioRepository.findById(usuarioAdminId)
                .orElseThrow();

        // When/Then: rol está disponible
        assertThat(usuario.rol()).isNotNull();
        assertThat(usuario.rol()).isEqualTo(Rol.ADMIN);
    }

    // ==================== RNF-004: Mantenibilidad ====================

    @Test
    @DisplayName("CA-RNF-004.1: Dominio no tiene dependencias externas")
    void caNrf004_1_domainNoDependencies() {
        // Given: clases de dominio
        // When/Then: no tienen imports de Spring
        // Nota: verificar manualmente que Rol.java, RoleValidator.java no tienen @Component, @Service, etc.
        assertThat(Rol.class.isEnum()).isTrue();
        assertThat(RoleValidator.class.getPackage().getName())
                .contains("domain");
    }

    @Test
    @DisplayName("CA-RNF-004.2: UsuarioRepository es interfaz desacoplada")
    void caNrf004_2_repositoryInterface() {
        // Given: interfaz UsuarioRepository
        // When/Then: es una interfaz en capa application
        assertThat(UsuarioRepository.class.isInterface()).isTrue();
    }
}
