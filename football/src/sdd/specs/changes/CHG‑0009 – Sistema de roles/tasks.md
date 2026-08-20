# CHG-0009 - Tareas Técnicas: Sistema de Roles

**Estado**: `diseño`  
**Fecha**: 2026-08-20  
**Basado en**: requirements.md  
**Arquitectura**: Dominio → Aplicación → Infraestructura → Presentación

---

## 1. Resumen de Tareas

| # | Categoría | Tarea | Requisitos | Prioridad | Dependencias |
|---|-----------|-------|-----------|-----------|--------------|
| T-001 | BD | Crear migración: agregar columna `rol` a tabla usuarios | RF-001.CA-001.1, RF-001.CA-001.2 | Alta | - |
| T-002 | BD | Script de datos iniciales: rol=usuario por defecto | RF-001.CA-001.1 | Media | T-001 |
| T-003 | Dominio | Crear enum `Rol` en dominio | RF-001.CA-001.1 | Alta | - |
| T-004 | Dominio | Extender entidad `Usuario` con atributo `rol` | RF-001.CA-001.1, RF-001.CA-001.3 | Alta | T-003 |
| T-005 | Dominio | Crear servicio de dominio `RoleValidator` | RF-005.CA-005.1 | Alta | T-003 |
| T-006 | Dominio | Crear especificación `UsuarioPuedeCambiarRol` | RF-005.CA-005.2, RF-005.CA-005.3 | Alta | T-004 |
| T-007 | Infraestructura | Extender `UsuarioRepository` para operaciones de rol | RF-001.CA-001.3 | Alta | T-001, T-004 |
| T-008 | Infraestructura | Crear `RoleRepository` interfaz y JPA | RF-001 | Media | T-003 |
| T-009 | Infraestructura | Crear entidad JPA `UsuarioJpa` con columna `rol` | RF-001 | Alta | T-001, T-003 |
| T-010 | Infraestructura | Crear `RoleAuditLogRepository` para registrar cambios | RNF-001.CA-RNF-001.3 | Alta | - |
| T-011 | Infraestructura | Crear mapper `UsuarioMapper` para incluir rol | RF-004 | Media | T-009 |
| T-012 | Aplicación | Crear caso de uso `ObtenerRolDelUsuario` | RF-004 | Alta | T-004, T-007 |
| T-013 | Aplicación | Crear caso de uso `CambiarRolDeUsuario` | RF-001.CA-001.3, RF-005 | Alta | T-004, T-005, T-006, T-007, T-010 |
| T-014 | Aplicación | Crear caso de uso `ListarUsuarios` | RF-003.CA-003.3 | Media | T-007, T-012 |
| T-015 | Aplicación | Integrar rol en `CasoUsoAutenticar` (CHG-0008) | RF-004.CA-004.1 | Alta | T-004, T-012 |
| T-016 | Infraestructura | Extender generador de JWT: incluir `rol` en payload | RF-004.CA-004.1 | Alta | T-012, T-015 |
| T-017 | Infraestructura | Crear `RoleAuthorizationMiddleware` | RF-002, RNF-001.CA-RNF-001.1 | Alta | T-003, T-016 |
| T-018 | Infraestructura | Crear anotación `@RequiresRole` para endpoints | RF-002, RF-003 | Alta | T-017 |
| T-019 | Presentación | Crear controlador `AdminController` | RF-003 | Media | T-013, T-014, T-017 |
| T-020 | Presentación | Endpoint GET `/admin/users` | RF-003.CA-003.3 | Media | T-014, T-017 |
| T-021 | Presentación | Endpoint PATCH `/admin/users/{userId}/role` | RF-003.CA-003.4, RF-005 | Alta | T-013, T-017 |
| T-022 | Presentación | Endpoint GET `/admin/logs` | RF-003.CA-003.1, RNF-001.CA-RNF-001.3 | Media | T-017, T-010 |
| T-023 | Presentación | Endpoint GET `/admin/errors` | RF-003.CA-003.2 | Media | T-017 |
| T-024 | Presentación | Endpoint POST `/admin/config` | RF-003.CA-003.5 | Baja | T-017 |
| T-025 | Sesión | Invalidar sesiones de CHG-0008 al cambiar rol | RNF-003.CA-RNF-003.2 | Alta | T-013, CHG-0008 |
| T-026 | Pruebas | Prueba unitaria: `RoleValidator` | RF-005 | Alta | T-005 |
| T-027 | Pruebas | Prueba unitaria: `UsuarioPuedeCambiarRol` especificación | RF-005 | Alta | T-006 |
| T-028 | Pruebas | Prueba de integración: persistencia de rol | RF-001 | Alta | T-001, T-009 |
| T-029 | Pruebas | Prueba de integración: middleware validación de rol | RF-002, RNF-001 | Alta | T-017 |
| T-030 | Pruebas | Prueba de aceptación: usuario normal rechazado en `/admin/users` | RF-003, RNF-001.CA-RNF-001.1 | Alta | T-020, T-029 |
| T-031 | Pruebas | Prueba de aceptación: admin accede a `/admin/users` | RF-003.CA-003.3 | Alta | T-020 |
| T-032 | Pruebas | Prueba de aceptación: cambio de rol registra en auditoría | RNF-001.CA-RNF-001.3 | Alta | T-021, T-010 |
| T-033 | Pruebas | Prueba de aceptación: sesión se invalida al cambiar rol | RNF-003.CA-RNF-003.2 | Alta | T-025 |
| T-034 | Pruebas | Prueba de aceptación: usuario no puede cambiar su propio rol | RF-005.CA-005.2 | Alta | T-021 |
| T-035 | Pruebas | Prueba de integración: JWT incluye rol | RF-004.CA-004.1 | Alta | T-016 |

---

## 2. Tareas por Categoría

### 2.1 Base de Datos (5 tareas)

#### T-001: Crear migración - agregar columna `rol`

**Requisito Trazable**: RF-001.CA-001.1, RF-001.CA-001.2

**Descripción**: Crear migración de Flyway o Liquibase que agregue columna `rol` a tabla `usuarios`.

**Entrada**:
- Tabla `usuarios` existente

**Salida**:
- Archivo `V###__add_rol_to_usuarios.sql` en `src/main/resources/db/migration/`
- Columna `rol` VARCHAR(20) NOT NULL DEFAULT 'usuario'
- Índice en `rol` para búsquedas rápidas

**Verificación**:
- Migración ejecuta sin errores
- Columna existe con tipo correcto
- Usuarios existentes tienen rol='usuario' por defecto

**Nota**: Ejecutable sin romper datos existentes

---

#### T-002: Script de datos iniciales

**Requisito Trazable**: RF-001.CA-001.1

**Descripción**: Crear script que asegure que todos los usuarios existentes tienen rol='usuario' por defecto.

**Entrada**:
- Tabla `usuarios` con columna `rol` (de T-001)

**Salida**:
- Script SQL: `V###__init_usuario_role.sql`
- UPDATE usuarios SET rol='usuario' WHERE rol IS NULL

**Verificación**:
- Todos los usuarios tienen rol asignado
- No hay valores NULL en columna rol

---

#### T-003: Crear enum `Rol`

**Requisito Trazable**: RF-001.CA-001.1

**Descripción**: Crear enumeración de dominio `Rol` que represente los dos roles válidos.

**Ubicación**: `src/main/java/com/example/football/domain/usuario/value/Rol.java`

**Entrada**:
- Especificación de roles válidos: usuario, admin

**Salida**:
```java
package com.example.football.domain.usuario.value;

public enum Rol {
    USUARIO("usuario"),
    ADMIN("admin");
    
    private final String valor;
    
    Rol(String valor) {
        this.valor = valor;
    }
    
    public String valor() {
        return valor;
    }
    
    public static Rol desde(String valor) {
        // Validación y conversión
    }
}
```

**Verificación**:
- Enum tiene 2 valores: USUARIO, ADMIN
- Método `desde(String)` valida entrada
- Lanza excepción para valores inválidos

---

#### T-004: Extender entidad `Usuario` con atributo `rol`

**Requisito Trazable**: RF-001.CA-001.1, RF-001.CA-001.3

**Descripción**: Agregar atributo `rol` a entidad Usuario del dominio.

**Ubicación**: `src/main/java/com/example/football/domain/usuario/Usuario.java`

**Entrada**:
- Entidad Usuario existente
- Enum Rol (de T-003)

**Salida**:
```java
public class Usuario {
    // ... atributos existentes
    private Rol rol;
    
    private Usuario(..., Rol rol) {
        // constructor con rol
    }
    
    public Rol obtenerRol() {
        return this.rol;
    }
    
    public void cambiarRol(Rol nuevoRol) {
        // validar que cambio sea permitido
        this.rol = nuevoRol;
    }
}
```

**Verificación**:
- Usuario puede crearse con rol
- rol tiene getter
- rol tiene setter (con validación)
- Rol por defecto es USUARIO en factory

---

#### T-005: Crear `RoleValidator` - Servicio de Dominio

**Requisito Trazable**: RF-005.CA-005.1

**Descripción**: Crear servicio de dominio para validar cambios de rol.

**Ubicación**: `src/main/java/com/example/football/domain/usuario/service/RoleValidator.java`

**Entrada**:
- Enum Rol válidos
- Reglas de cambio de rol

**Salida**:
```java
public class RoleValidator {
    
    public void validarRolValido(String rolString) throws RolInvalidoError {
        // Validar que rol sea usuario o admin
    }
    
    public void validarCambioDeRol(Usuario usuario, Rol nuevoRol) 
        throws NoAutorizadoParaCambiarRolError {
        // No puede cambiar su propio rol
    }
}
```

**Verificación**:
- Rechaza roles inválidos con excepción
- Valida antes de persistir
- Mensaje de error claro

**Dominio de Error Requerido**:
- `RolInvalidoError` - rol no es usuario ni admin
- `NoAutorizadoParaCambiarRolError` - usuario intenta cambiar su propio rol

---

#### T-006: Crear especificación `UsuarioPuedeCambiarRol`

**Requisito Trazable**: RF-005.CA-005.2, RF-005.CA-005.3

**Descripción**: Crear especificación de dominio para verificar si usuario puede cambiar rol.

**Ubicación**: `src/main/java/com/example/football/domain/usuario/specification/UsuarioPuedeCambiarRol.java`

**Entrada**:
- Usuario solicitante (quien hace el cambio)
- Usuario objetivo (quien será cambiado)
- Nuevo rol

**Salida**:
```java
public class UsuarioPuedeCambiarRol extends Specification<Usuario> {
    
    private Usuario usuarioSolicitante;
    private Usuario usuarioObjetivo;
    private Rol nuevoRol;
    
    public boolean esValida() {
        // Regla 1: Solo admin puede cambiar roles
        // Regla 2: No puede cambiar su propio rol
        // Regla 3: Rol debe ser válido
        return usuarioSolicitante.obtenerRol() == Rol.ADMIN
            && !usuarioSolicitante.getId().equals(usuarioObjetivo.getId())
            && esRolValido(nuevoRol);
    }
}
```

**Verificación**:
- Especificación rechaza si usuario no es admin
- Especificación rechaza si intenta cambiar su propio rol
- Especificación valida rol correcto

---

### 2.2 Infraestructura (9 tareas)

#### T-007: Extender `UsuarioRepository` para operaciones de rol

**Requisito Trazable**: RF-001.CA-001.3

**Descripción**: Agregar métodos a interfaz `UsuarioRepository` para obtener y actualizar rol.

**Ubicación**: `src/main/java/com/example/football/infrastructure/usuario/repository/UsuarioRepository.java`

**Entrada**:
- Interfaz UsuarioRepository existente
- Entidad Usuario con rol

**Salida**:
```java
public interface UsuarioRepository {
    // métodos existentes...
    
    Rol obtenerRolDelUsuario(UsuarioId usuarioId) throws UsuarioNoEncontradoError;
    
    void actualizarRol(UsuarioId usuarioId, Rol nuevoRol);
    
    List<Usuario> listarTodos(); // para panel admin
}
```

**Verificación**:
- Métodos están en interfaz
- Implementación JPA corresponde
- Queries son eficientes

---

#### T-008: Crear `RoleRepository`

**Requisito Trazable**: RF-001

**Descripción**: Crear repositorio específico para operaciones relacionadas con roles.

**Ubicación**: `src/main/java/com/example/football/infrastructure/usuario/repository/RoleRepository.java`

**Entrada**:
- Enum Rol

**Salida**:
```java
public interface RoleRepository {
    
    Rol obtenerPorValor(String valor) throws RolNoEncontradoError;
    
    List<Rol> listarRolesValidos();
}
```

**Verificación**:
- Método obtener por valor funciona
- Retorna roles válidos
- Lanza excepción para roles inválidos

---

#### T-009: Crear entidad JPA `UsuarioJpa` con `rol`

**Requisito Trazable**: RF-001

**Descripción**: Actualizar mapeo JPA para incluir columna `rol`.

**Ubicación**: `src/main/java/com/example/football/infrastructure/usuario/persistence/UsuarioJpa.java`

**Entrada**:
- Entidad UsuarioJpa existente
- Columna `rol` en tabla (de T-001)

**Salida**:
```java
@Entity
@Table(name = "usuarios")
public class UsuarioJpa {
    
    @Id
    private String id;
    
    // campos existentes...
    
    @Column(name = "rol", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Rol rol;
    
    // getters y setters
}
```

**Verificación**:
- Columna `rol` mapeada correctamente
- Enum usa EnumType.STRING
- NOT NULL constraint

---

#### T-010: Crear `RoleAuditLogRepository`

**Requisito Trazable**: RNF-001.CA-RNF-001.3

**Descripción**: Crear repositorio para registrar cambios de rol en auditoría.

**Ubicación**: `src/main/java/com/example/football/infrastructure/usuario/repository/RoleAuditLogRepository.java`

**Entrada**:
- Especificación de auditoría
- Información de cambio de rol

**Salida**:
```java
public interface RoleAuditLogRepository {
    
    void registrarCambioDeRol(
        UsuarioId usuarioId,
        Rol rolAnterior,
        Rol nuevoRol,
        UsuarioId adminId,
        LocalDateTime timestamp
    );
    
    List<RoleAuditLog> obtenerHistorial(UsuarioId usuarioId);
}
```

**Verificación**:
- Cambios se persisten
- Auditoría incluye: quién, cuándo, rol anterior, rol nuevo
- Logs son inmutables desde aplicación

---

#### T-011: Crear mapper `UsuarioMapper`

**Requisito Trazable**: RF-004

**Descripción**: Crear o actualizar mapper para incluir rol en respuestas HTTP.

**Ubicación**: `src/main/java/com/example/football/infrastructure/usuario/mapper/UsuarioMapper.java`

**Entrada**:
- Entidad Usuario con rol
- DTO UsuarioResponse

**Salida**:
```java
public class UsuarioMapper {
    
    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.obtenerRol().valor(),
            usuario.getFechaCreacion()
        );
    }
    
    public static UsuarioDtoDominio toDomain(UsuarioJpa usuarioJpa) {
        return new UsuarioDtoDominio(
            // ...
            usuarioJpa.obtenerRol()
        );
    }
}
```

**Verificación**:
- Rol incluido en UsuarioResponse
- Valor en formato string ("usuario", "admin")
- Mapeo bidireccional correcto

---

#### T-012: Caso de uso `ObtenerRolDelUsuario`

**Requisito Trazable**: RF-004

**Descripción**: Crear caso de uso para obtener el rol de un usuario.

**Ubicación**: `src/main/java/com/example/football/application/usuario/usecase/ObtenerRolDelUsuario.java`

**Entrada**:
- UsuarioId
- UsuarioRepository

**Salida**:
```java
public class ObtenerRolDelUsuario {
    
    public Rol ejecutar(UsuarioId usuarioId) 
        throws UsuarioNoEncontradoError {
        Usuario usuario = usuarioRepository.obtener(usuarioId);
        return usuario.obtenerRol();
    }
}
```

**Verificación**:
- Retorna rol del usuario
- Lanza excepción si usuario no existe
- No tiene efectos secundarios

---

#### T-013: Caso de uso `CambiarRolDeUsuario`

**Requisito Trazable**: RF-001.CA-001.3, RF-005

**Descripción**: Crear caso de uso para cambiar rol de un usuario (por admin).

**Ubicación**: `src/main/java/com/example/football/application/usuario/usecase/CambiarRolDeUsuario.java`

**Entrada**:
- UsuarioId (objetivo)
- Rol (nuevo)
- UsuarioId (admin solicitante)
- UsuarioRepository
- RoleValidator
- RoleAuditLogRepository

**Salida**:
```java
public class CambiarRolDeUsuario {
    
    public void ejecutar(
        UsuarioId usuarioObjetivo,
        Rol nuevoRol,
        UsuarioId adminSolicitante
    ) throws Exception {
        // 1. Obtener usuario admin solicitante
        Usuario admin = usuarioRepository.obtener(adminSolicitante);
        Usuario objetivo = usuarioRepository.obtener(usuarioObjetivo);
        
        // 2. Validar que admin tiene rol ADMIN
        if (admin.obtenerRol() != Rol.ADMIN) {
            throw new NoAutorizadoParaCambiarRolError();
        }
        
        // 3. Validar que objetivo es diferente de admin
        if (admin.getId().equals(objetivo.getId())) {
            throw new NoAutorizadoParaCambiarRolError();
        }
        
        // 4. Validar que rol nuevo es válido
        roleValidator.validarRolValido(nuevoRol);
        
        // 5. Guardar rol anterior
        Rol rolAnterior = objetivo.obtenerRol();
        
        // 6. Cambiar rol
        objetivo.cambiarRol(nuevoRol);
        
        // 7. Persistir
        usuarioRepository.guardar(objetivo);
        
        // 8. Registrar en auditoría
        auditLogRepository.registrarCambioDeRol(
            usuarioObjetivo,
            rolAnterior,
            nuevoRol,
            adminSolicitante,
            LocalDateTime.now()
        );
        
        // 9. Invalidar sesiones antiguas (delegado a CHG-0008)
        invalidarSesionesDelUsuario(usuarioObjetivo);
    }
}
```

**Verificación**:
- Solo admin puede cambiar roles
- Rol se valida antes de persistir
- Auditoría se registra
- Sesiones se invalidan

---

#### T-014: Caso de uso `ListarUsuarios`

**Requisito Trazable**: RF-003.CA-003.3

**Descripción**: Crear caso de uso para listar usuarios (para panel admin).

**Ubicación**: `src/main/java/com/example/football/application/usuario/usecase/ListarUsuarios.java`

**Entrada**:
- UsuarioRepository
- UsuarioId (admin solicitante)

**Salida**:
```java
public class ListarUsuarios {
    
    public List<UsuarioResponse> ejecutar(UsuarioId adminSolicitante) 
        throws NoAutorizadoError {
        
        // 1. Validar que solicitante es admin
        Usuario admin = usuarioRepository.obtener(adminSolicitante);
        if (admin.obtenerRol() != Rol.ADMIN) {
            throw new NoAutorizadoError("Se requiere rol admin");
        }
        
        // 2. Obtener todos los usuarios
        List<Usuario> usuarios = usuarioRepository.listarTodos();
        
        // 3. Mapear a response
        return usuarios.stream()
            .map(UsuarioMapper::toResponse)
            .collect(Collectors.toList());
    }
}
```

**Verificación**:
- Solo admin puede listar
- Retorna todos los usuarios con su rol
- Response incluye: id, email, nombre, rol, fechas

---

#### T-015: Integrar rol en `CasoUsoAutenticar`

**Requisito Trazable**: RF-004.CA-004.1

**Descripción**: Extender caso de uso de autenticación (CHG-0008) para incluir rol en sesión.

**Ubicación**: `src/main/java/com/example/football/application/sesion/usecase/Autenticar.java`

**Entrada**:
- Caso de uso existente de autenticación
- UsuarioRepository (con rol)
- ObtenerRolDelUsuario (T-012)

**Salida**:
```java
public class Autenticar {
    
    public SesionResponse ejecutar(CredencialesAutenticacion credenciales) 
        throws Exception {
        
        // ... autenticación existente
        
        Usuario usuario = usuarioRepository.obtener(usuarioId);
        
        // Obtener rol
        Rol rol = obtenerRolDelUsuario.ejecutar(usuarioId);
        
        // Incluir rol en sesión
        Sesion sesion = new Sesion(
            usuarioId,
            token,
            rol, // NUEVO
            LocalDateTime.now()
        );
        
        return new SesionResponse(
            sesion.getToken(),
            rol.valor()  // NUEVO: incluir rol en respuesta
        );
    }
}
```

**Verificación**:
- Sesión incluye rol
- Rol se obtiene en momento de autenticación
- Rol correcto en respuesta

---

#### T-016: Extender generador de JWT con `rol`

**Requisito Trazable**: RF-004.CA-004.1

**Descripción**: Actualizar componente que genera token JWT para incluir claim `rol`.

**Ubicación**: `src/main/java/com/example/football/infrastructure/security/JwtTokenProvider.java`

**Entrada**:
- JwtTokenProvider existente
- Rol del usuario

**Salida**:
```java
public class JwtTokenProvider {
    
    public String generarToken(UsuarioId usuarioId, Rol rol) {
        return Jwts.builder()
            .setSubject(usuarioId.valor())
            .claim("rol", rol.valor())  // NUEVO
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRACION))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public String extraerRolDelToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .get("rol", String.class);
    }
}
```

**Verificación**:
- Token incluye claim `rol`
- Rol se puede extraer del token
- Firma es inmutable desde cliente

---

#### T-017: Crear `RoleAuthorizationMiddleware`

**Requisito Trazable**: RF-002, RNF-001.CA-RNF-001.1

**Descripción**: Crear middleware que valide el rol de la solicitud antes de llegar al controlador.

**Ubicación**: `src/main/java/com/example/football/infrastructure/security/RoleAuthorizationMiddleware.java`

**Entrada**:
- HttpServletRequest
- HttpServletResponse
- JwtTokenProvider
- UsuarioRepository

**Salida**:
```java
@Component
public class RoleAuthorizationMiddleware extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // 1. Verificar si ruta requiere rol específico
        String rolRequerido = obtenerRolRequerido(requestPath);
        
        if (rolRequerido == null) {
            // No requiere rol específico
            filterChain.doFilter(request, response);
            return;
        }
        
        // 2. Extraer token
        String token = extraerTokenDelHeader(request);
        if (token == null) {
            denegarAcceso(response, "Token no encontrado");
            return;
        }
        
        // 3. Validar token y extraer rol
        try {
            String rolDelUsuario = jwtTokenProvider.extraerRolDelToken(token);
            
            // 4. Verificar que rol coincide
            if (!rolDelUsuario.equals(rolRequerido)) {
                denegarAcceso(response, "Acceso denegado: se requiere rol " + rolRequerido);
                return;
            }
            
            // 5. Pasar al siguiente filtro
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            denegarAcceso(response, "Token inválido: " + e.getMessage());
        }
    }
    
    private String obtenerRolRequerido(String requestPath) {
        if (requestPath.startsWith("/admin/")) {
            return "admin";
        }
        return null;
    }
    
    private String extraerTokenDelHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
    
    private void denegarAcceso(HttpServletResponse response, String mensaje) 
        throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
```

**Verificación**:
- Rutas `/admin/*` requieren rol `admin`
- Token se extrae del header Authorization
- Acceso denegado retorna HTTP 403
- Acceso permitido continúa al controlador

**Configuración Requerida**:
- Registrar middleware en `SecurityConfiguration`
- Orden de ejecución antes de controladores

---

#### T-018: Crear anotación `@RequiresRole`

**Requisito Trazable**: RF-002, RF-003

**Descripción**: Crear anotación para marcar endpoints que requieren rol específico.

**Ubicación**: `src/main/java/com/example/football/infrastructure/security/RequiresRole.java`

**Entrada**:
- Especificación de rol requerido

**Salida**:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String value(); // "usuario" o "admin"
}
```

**Uso**:
```java
@RequiresRole("admin")
@GetMapping("/admin/users")
public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
    // ...
}
```

**Verificación**:
- Anotación se puede aplicar a métodos
- Middleware puede leerla
- Default es sin restricción

---

### 2.3 Presentación (6 tareas)

#### T-019: Crear controlador `AdminController`

**Requisito Trazable**: RF-003

**Descripción**: Crear controlador REST con endpoints de administración.

**Ubicación**: `src/main/java/com/example/football/presentation/admin/AdminController.java`

**Entrada**:
- Casos de uso (T-012, T-013, T-014)
- Middleware (T-017)
- SecurityContext con usuario actual

**Salida**:
```java
@RestController
@RequestMapping("/admin")
@RequiresRole("admin")
public class AdminController {
    
    private final ObtenerRolDelUsuario obtenerRolDelUsuario;
    private final CambiarRolDeUsuario cambiarRolDeUsuario;
    private final ListarUsuarios listarUsuarios;
    
    public AdminController(
        ObtenerRolDelUsuario obtenerRolDelUsuario,
        CambiarRolDeUsuario cambiarRolDeUsuario,
        ListarUsuarios listarUsuarios
    ) {
        this.obtenerRolDelUsuario = obtenerRolDelUsuario;
        this.cambiarRolDeUsuario = cambiarRolDeUsuario;
        this.listarUsuarios = listarUsuarios;
    }
    
    // Endpoints en T-020, T-021, T-022, T-023, T-024
}
```

**Verificación**:
- Controlador tiene anotación @RequiresRole("admin")
- Todos los endpoints están protegidos
- SecurityContext disponible en métodos

---

#### T-020: Endpoint GET `/admin/users`

**Requisito Trazable**: RF-003.CA-003.3

**Descripción**: Implementar endpoint que lista todos los usuarios del sistema.

**Entrada**:
- ListarUsuarios caso de uso (T-014)

**Salida**:
```java
@GetMapping("/users")
public ResponseEntity<List<UsuarioResponse>> listarUsuarios(
    @AuthenticationPrincipal UsuarioId usuarioId
) {
    List<UsuarioResponse> usuarios = listarUsuarios.ejecutar(usuarioId);
    return ResponseEntity.ok(usuarios);
}
```

**Response (HTTP 200)**:
```json
[
  {
    "id": "usr_123",
    "email": "usuario@example.com",
    "nombre": "Juan Pérez",
    "rol": "usuario",
    "fechaCreacion": "2026-01-15T10:30:00",
    "ultimaSesion": "2026-08-20T14:45:00"
  },
  {
    "id": "usr_456",
    "email": "admin@example.com",
    "nombre": "Admin User",
    "rol": "admin",
    "fechaCreacion": "2026-01-01T08:00:00",
    "ultimaSesion": "2026-08-20T15:00:00"
  }
]
```

**Verificación**:
- Solo admin puede acceder
- Retorna HTTP 200
- Incluye todos los usuarios
- Incluye rol en cada usuario

---

#### T-021: Endpoint PATCH `/admin/users/{userId}/role`

**Requisito Trazable**: RF-003.CA-003.4, RF-005

**Descripción**: Implementar endpoint para cambiar rol de un usuario.

**Entrada**:
- CambiarRolDeUsuario caso de uso (T-013)
- Request DTO con nuevo rol

**Salida**:
```java
@PatchMapping("/users/{userId}/role")
public ResponseEntity<UsuarioResponse> cambiarRol(
    @PathVariable String userId,
    @RequestBody CambiarRolRequest request,
    @AuthenticationPrincipal UsuarioId adminId
) throws Exception {
    
    UsuarioId usuarioObjetivo = new UsuarioId(userId);
    Rol nuevoRol = Rol.desde(request.getNewRole());
    
    cambiarRolDeUsuario.ejecutar(usuarioObjetivo, nuevoRol, adminId);
    
    Usuario usuarioActualizado = usuarioRepository.obtener(usuarioObjetivo);
    return ResponseEntity.ok(UsuarioMapper.toResponse(usuarioActualizado));
}
```

**Request DTO**:
```java
public class CambiarRolRequest {
    private String newRole; // "usuario" o "admin"
}
```

**Response (HTTP 200)**:
```json
{
  "id": "usr_123",
  "email": "usuario@example.com",
  "nombre": "Juan Pérez",
  "rol": "admin",
  "fechaCreacion": "2026-01-15T10:30:00",
  "ultimaSesion": "2026-08-20T14:45:00"
}
```

**Errores**:
- HTTP 400: rol inválido → "Rol inválido. Valores aceptados: usuario, admin"
- HTTP 403: no es admin → "Se requiere rol admin"
- HTTP 403: intenta cambiar su propio rol → "No puedes modificar tu propio rol"
- HTTP 404: usuario no encontrado → "Usuario no encontrado"

**Verificación**:
- Solo admin puede cambiar roles
- Valida rol nuevo
- Retorna usuario actualizado
- Auditoría se registra
- Sesiones se invalidan

---

#### T-022: Endpoint GET `/admin/logs`

**Requisito Trazable**: RF-003.CA-003.1, RNF-001.CA-RNF-001.3

**Descripción**: Implementar endpoint que retorna logs del sistema.

**Entrada**:
- LogRepository (existente)
- RoleAuditLogRepository (T-010)

**Salida**:
```java
@GetMapping("/logs")
public ResponseEntity<List<LogResponse>> obtenerLogs(
    @RequestParam(defaultValue = "100") int limit
) {
    List<Log> logs = logRepository.obtenerUltimos(limit);
    return ResponseEntity.ok(
        logs.stream()
            .map(log -> new LogResponse(
                log.getTimestamp(),
                log.getNivel(),
                log.getMensaje()
            ))
            .collect(Collectors.toList())
    );
}
```

**Response (HTTP 200)**:
```json
[
  {
    "timestamp": "2026-08-20T15:00:00",
    "nivel": "INFO",
    "mensaje": "Cambio de rol: usuario usr_123 -> admin por usr_456"
  },
  {
    "timestamp": "2026-08-20T14:55:00",
    "nivel": "ERROR",
    "mensaje": "Error en autenticación: credenciales inválidas"
  }
]
```

**Verificación**:
- Solo admin puede acceder
- Retorna últimos N logs
- Incluye timestamp, nivel, mensaje

---

#### T-023: Endpoint GET `/admin/errors`

**Requisito Trazable**: RF-003.CA-003.2

**Descripción**: Implementar endpoint que retorna errores recientes.

**Entrada**:
- ErrorRepository o sistema de logging

**Salida**:
```java
@GetMapping("/errors")
public ResponseEntity<List<ErrorResponse>> obtenerErrores(
    @RequestParam(defaultValue = "50") int limit
) {
    List<Error> errores = errorRepository.obtenerRecientes(limit);
    return ResponseEntity.ok(
        errores.stream()
            .map(error -> new ErrorResponse(
                error.getTimestamp(),
                error.getTipo(),
                error.getMensaje(),
                error.getStackTrace(),
                error.getUsuarioAfectadoId()
            ))
            .collect(Collectors.toList())
    );
}
```

**Response (HTTP 200)**:
```json
[
  {
    "timestamp": "2026-08-20T14:50:00",
    "tipo": "UsuarioNoEncontradoError",
    "mensaje": "Usuario con id usr_999 no existe",
    "stackTrace": "...",
    "usuarioAfectado": "usr_123"
  }
]
```

**Verificación**:
- Solo admin puede acceder
- Retorna últimos N errores
- Incluye información completa para diagnóstico

---

#### T-024: Endpoint POST `/admin/config`

**Requisito Trazable**: RF-003.CA-003.5

**Descripción**: Implementar endpoint para modificar configuraciones del sistema.

**Entrada**:
- ConfigRepository o sistema de configuración

**Salida**:
```java
@PostMapping("/config")
public ResponseEntity<ConfigResponse> actualizarConfig(
    @RequestBody ConfigRequest request
) {
    // Validar configuración
    configService.actualizar(request);
    return ResponseEntity.ok(new ConfigResponse("Configuración actualizada"));
}
```

**Request DTO**:
```java
public class ConfigRequest {
    private String clave;    // ej: maxUsersPerDay
    private String valor;    // ej: 500
    private String tipo;     // INT, STRING, BOOLEAN
}
```

**Verificación**:
- Solo admin puede acceder
- Valida configuración antes de guardar
- Persiste en base de datos o sistema de config

---

### 2.4 Integración con CHG-0008 (1 tarea)

#### T-025: Invalidar sesiones de usuario al cambiar rol

**Requisito Trazable**: RNF-003.CA-RNF-003.2

**Descripción**: Integración con CHG-0008 para invalidar sesiones antiguas cuando cambia el rol.

**Entrada**:
- CasoUso CambiarRolDeUsuario (T-013)
- SesionRepository de CHG-0008

**Salida**:
```java
// En CambiarRolDeUsuario.ejecutar() - después de persistir cambio

private void invalidarSesionesDelUsuario(UsuarioId usuarioId) {
    // Obtener todas las sesiones activas del usuario
    List<Sesion> sesionesActivas = sesionRepository.obtenerActivas(usuarioId);
    
    // Invalidar todas
    for (Sesion sesion : sesionesActivas) {
        sesionRepository.invalidar(sesion.getId());
    }
}
```

**Verificación**:
- Sesiones antiguas se invalidan inmediatamente
- Usuario debe re-autenticarse
- Nueva sesión tiene nuevo rol

**Nota**: Requiere que CHG-0008 exponga `SesionRepository` con método `obtenerActivas()` e `invalidar()`

---

### 2.5 Pruebas (10 tareas)

#### T-026: Prueba unitaria: `RoleValidator`

**Requisito Trazable**: RF-005

**Descripción**: Pruebas unitarias del validador de roles.

**Ubicación**: `src/test/java/com/example/football/domain/usuario/service/RoleValidatorTest.java`

**Casos de Prueba**:

1. `validarRolValido_Usuario_NoThrowsException`
   - Dado: rol="usuario"
   - Cuando: validar
   - Entonces: no lanza excepción

2. `validarRolValido_Admin_NoThrowsException`
   - Dado: rol="admin"
   - Cuando: validar
   - Entonces: no lanza excepción

3. `validarRolValido_Invalido_ThrowsException`
   - Dado: rol="superadmin"
   - Cuando: validar
   - Entonces: lanza RolInvalidoError

4. `validarRolValido_Null_ThrowsException`
   - Dado: rol=null
   - Cuando: validar
   - Entonces: lanza RolInvalidoError

---

#### T-027: Prueba unitaria: `UsuarioPuedeCambiarRol`

**Requisito Trazable**: RF-005

**Descripción**: Pruebas unitarias de la especificación de dominio.

**Ubicación**: `src/test/java/com/example/football/domain/usuario/specification/UsuarioPuedeCambiarRolTest.java`

**Casos de Prueba**:

1. `esValida_AdminCambiaAOtroUsuario_RetornaTrue`
   - Dado: usuario admin, objetivo es otro usuario
   - Cuando: especificación valida
   - Entonces: retorna true

2. `esValida_UsuarioIntentaCambiarSuRol_RetornaFalse`
   - Dado: usuario intenta cambiar su propio rol
   - Cuando: especificación valida
   - Entonces: retorna false

3. `esValida_UsuarioIntentaCambiarOtroRol_RetornaFalse`
   - Dado: usuario no-admin intenta cambiar rol de otro
   - Cuando: especificación valida
   - Entonces: retorna false

4. `esValida_AdminCambiaARolInvalido_RetornaFalse`
   - Dado: admin intenta cambiar a rol inválido
   - Cuando: especificación valida
   - Entonces: retorna false

---

#### T-028: Prueba de integración: persistencia de rol

**Requisito Trazable**: RF-001

**Descripción**: Pruebas de integración con base de datos.

**Ubicación**: `src/test/java/com/example/football/infrastructure/usuario/persistence/UsuarioPersistenceTest.java`

**Casos de Prueba**:

1. `crearUsuario_ConRolAdministrador_PersisteBienEnDb`
   - Dado: usuario con rol=admin
   - Cuando: guardar en repositorio
   - Entonces: base de datos contiene rol=admin

2. `actualizarRolUsuario_DeUsuarioAAAdmin_PersisteBien`
   - Dado: usuario existente con rol=usuario
   - Cuando: actualizar rol a admin
   - Entonces: base de datos refleja nuevo rol

3. `obtenerRolUsuario_RetornaRolCorrectamente`
   - Dado: usuario en base de datos con rol conocido
   - Cuando: obtener rol
   - Entonces: retorna rol correcto

4. `listarUsuarios_IncluyeRolEnCadaUsuario`
   - Dado: múltiples usuarios con diferentes roles
   - Cuando: listar todos
   - Entonces: cada usuario tiene su rol correcto

---

#### T-029: Prueba de integración: middleware validación

**Requisito Trazable**: RF-002, RNF-001

**Descripción**: Pruebas del middleware de autorización.

**Ubicación**: `src/test/java/com/example/football/infrastructure/security/RoleAuthorizationMiddlewareTest.java`

**Casos de Prueba**:

1. `middleware_AdminAccedeAEndpointAdmin_Continua`
   - Dado: usuario admin, solicitud a /admin/users
   - Cuando: middleware valida
   - Entonces: solicitud continúa (HTTP 200)

2. `middleware_UsuarioAccedeAEndpointAdmin_Deniega`
   - Dado: usuario con rol usuario, solicitud a /admin/users
   - Cuando: middleware valida
   - Entonces: retorna HTTP 403

3. `middleware_TokenInvalido_Deniega`
   - Dado: token malformado o inválido
   - Cuando: middleware valida
   - Entonces: retorna HTTP 401

4. `middleware_SinToken_Deniega`
   - Dado: sin header Authorization
   - Cuando: middleware valida
   - Entonces: retorna HTTP 403

---

#### T-030: Prueba de aceptación: usuario rechazado en `/admin/users`

**Requisito Trazable**: RF-003, RNF-001.CA-RNF-001.1

**Descripción**: Prueba end-to-end que usuario normal no puede acceder a panel admin.

**Ubicación**: `src/test/acceptance/AdminPanelAcceptanceTest.java`

**Escenario**:
```gherkin
Dado que un usuario con rol "usuario" está autenticado
Y tiene un token JWT válido
Cuando realiza una solicitud GET a "/admin/users"
Entonces recibe HTTP 403 (Forbidden)
Y el mensaje de error es "Acceso denegado: se requiere rol admin"
Y la lista de usuarios no se devuelve
```

---

#### T-031: Prueba de aceptación: admin accede a `/admin/users`

**Requisito Trazable**: RF-003.CA-003.3

**Descripción**: Prueba que admin puede acceder a lista de usuarios.

**Escenario**:
```gherkin
Dado que un usuario con rol "admin" está autenticado
Y tiene un token JWT válido
Cuando realiza una solicitud GET a "/admin/users"
Entonces recibe HTTP 200
Y la respuesta contiene una lista de usuarios
Y cada usuario incluye su rol
Y la lista contiene al menos un usuario
```

---

#### T-032: Prueba de aceptación: cambio de rol se audita

**Requisito Trazable**: RNF-001.CA-RNF-001.3

**Descripción**: Prueba que cambios de rol se registran en auditoría.

**Escenario**:
```gherkin
Dado que existe un usuario "Juan" con rol "usuario"
Y un admin "Ana" está autenticado
Cuando Ana realiza PATCH a "/admin/users/juan_id/role"
Y envía {"newRole": "admin"}
Entonces recibe HTTP 200
Y el usuario Juan ahora tiene rol "admin"
Y existe un registro en auditoría:
  - quién: ana_id
  - cuándo: timestamp actual
  - rol anterior: usuario
  - rol nuevo: admin
Y el registro no se puede modificar desde aplicación
```

---

#### T-033: Prueba de aceptación: sesión se invalida al cambiar rol

**Requisito Trazable**: RNF-003.CA-RNF-003.2

**Descripción**: Prueba que sesión antigua se invalida cuando cambia el rol.

**Escenario**:
```gherkin
Dado que usuario "Juan" está autenticado con token antiguo
Y su rol es "usuario" en la sesión
Cuando admin cambia su rol a "admin"
Entonces la sesión antigua se invalida
Y Juan no puede usar el token antiguo en siguiente solicitud
Y Juan recibe HTTP 401 (Unauthorized)
Y Juan debe autenticarse nuevamente
Y nueva sesión tiene rol "admin"
```

---

#### T-034: Prueba de aceptación: usuario no puede cambiar su propio rol

**Requisito Trazable**: RF-005.CA-005.2

**Descripción**: Prueba que usuario no puede cambiar su propio rol.

**Escenario**:
```gherkin
Dado que usuario "Juan" está autenticado
Y tiene rol "usuario"
Cuando intenta PATCH en "/users/juan_id/role"
Y envía {"newRole": "admin"}
Entonces recibe HTTP 403 (Forbidden)
Y el mensaje es "No puedes modificar tu propio rol"
Y su rol sigue siendo "usuario"
Y no hay registro en auditoría
```

---

#### T-035: Prueba de integración: JWT incluye rol

**Requisito Trazable**: RF-004.CA-004.1

**Descripción**: Prueba que JWT contiene claim de rol.

**Ubicación**: `src/test/java/com/example/football/infrastructure/security/JwtTokenProviderTest.java`

**Casos de Prueba**:

1. `generarToken_Incluye RolEnPayload`
   - Dado: usuario con rol admin
   - Cuando: generar token
   - Entonces: payload contiene "rol": "admin"

2. `extraerRolDelToken_RetornaRolCorrectamente`
   - Dado: token con rol admin
   - Cuando: extraer rol
   - Entonces: retorna "admin"

3. `modificarRolEnToken_InvalídaFirma`
   - Dado: token válido con "rol": "usuario"
   - Cuando: modificar payload a "rol": "admin"
   - Entonces: firma se invalida
   - Y middleware rechaza token

4. `tokenExpired_NoSePuedeExtraerRol`
   - Dado: token expirado
   - Cuando: intentar extraer rol
   - Entonces: lanza excepción TokenExpiredError

---

## 3. Dependencias entre Tareas

```
Base de Datos (T-001, T-002)
    ↓
Dominio (T-003, T-004, T-005, T-006)
    ↓
Infraestructura (T-007, T-008, T-009, T-010, T-011, T-012, T-013, T-014)
    ├── T-015 (integrar con CHG-0008)
    ├── T-016 (JWT)
    ├── T-017 (Middleware)
    └── T-018 (Anotación)
        ↓
Presentación (T-019, T-020, T-021, T-022, T-023, T-024)
    ├── T-025 (invalidar sesiones)
    ↓
Pruebas (T-026 a T-035)
```

**Camino Crítico**:
T-001 → T-003 → T-004 → T-005 → T-006 → T-007 → T-013 → T-021

---

## 4. Matriz de Trazabilidad

| Tarea | RF-001 | RF-002 | RF-003 | RF-004 | RF-005 | RNF-001 | RNF-002 | RNF-003 | RNF-004 |
|-------|--------|--------|--------|--------|--------|---------|---------|---------|---------|
| T-001 | ✓ |  |  |  |  |  |  |  |  |
| T-003 | ✓ |  |  |  | ✓ |  |  |  |  |
| T-004 | ✓ |  |  |  |  |  |  |  |  |
| T-005 |  |  |  |  | ✓ | ✓ |  |  |  |
| T-006 |  |  |  |  | ✓ |  |  |  |  |
| T-007 | ✓ |  |  |  |  |  |  |  |  |
| T-012 |  |  |  | ✓ |  |  |  |  |  |
| T-013 | ✓ |  |  |  | ✓ | ✓ |  |  |  |
| T-014 |  |  | ✓ |  |  |  |  |  |  |
| T-016 |  |  |  | ✓ |  | ✓ |  |  |  |
| T-017 |  | ✓ |  |  |  | ✓ | ✓ |  |  |
| T-020 |  |  | ✓ |  |  |  |  |  |  |
| T-021 |  |  | ✓ |  | ✓ |  |  |  |  |
| T-025 |  |  |  |  |  |  |  | ✓ |  |

---

## 5. Criterios de Completitud

### Por Fase

**Fase 1: Infraestructura (BD + Dominio)**
- ✓ T-001 completado (migración ejecutable)
- ✓ T-003, T-004, T-005, T-006 completados (dominio sólido)
- ✓ T-009 completado (mapeo JPA)

**Fase 2: Lógica de Aplicación**
- ✓ T-012, T-013, T-014 completados (casos de uso)
- ✓ T-015 completado (integración con sesiones)
- ✓ T-016 completado (JWT con rol)

**Fase 3: Seguridad y Control de Acceso**
- ✓ T-017 completado (middleware)
- ✓ T-018 completado (anotación)
- ✓ T-025 completado (invalidación de sesiones)

**Fase 4: API REST**
- ✓ T-019, T-020, T-021, T-022, T-023, T-024 completados (endpoints)

**Fase 5: Pruebas**
- ✓ T-026 a T-035 completados (toda batería de tests)
- ✓ Cobertura > 85%
- ✓ Pruebas de aceptación pasan

---

## 6. Estimaciones de Esfuerzo

| Fase | Tareas | Estimación |
|------|--------|-----------|
| Infraestructura BD | 2 | 2 horas |
| Dominio | 4 | 4 horas |
| Infraestructura App | 8 | 6 horas |
| Aplicación | 4 | 5 horas |
| Seguridad | 3 | 4 horas |
| Presentación | 6 | 4 horas |
| Pruebas | 10 | 8 horas |
| **Total** | **35** | **~33 horas** |

---

## 7. Notas Importantes

### Seguridad
- Todas las validaciones de rol ocurren en servidor
- Token JWT no se puede modificar desde cliente (firma invalida)
- Auditoría es inmutable desde aplicación
- Sesiones se invalidan inmediatamente al cambiar rol

### Compatibilidad
- Requiere CHG-0008 (sesiones) completado y estable
- No rompe cambios anteriores
- Rol por defecto para usuarios existentes es "usuario"

### Arquitectura
- Dominio puro (sin dependencias)
- Aplicación orquesta casos de uso
- Infraestructura adapta a persistencia y JWT
- Presentación solo llama a aplicación

### Testing
- Cobertura mínima 85%
- Pruebas de aceptación validan requisitos
- Pruebas unitarias validan reglas de dominio

---

## 8. Referencias

- `proposal.md`: Definición del cambio
- `requirements.md`: Requisitos funcionales y no funcionales
- `backend-agent.md`: Reglas de arquitectura
- CHG-0008: Sistema de sesiones
- Dominio Usuario: `sdd/specs/domains/usuario/spec.md`
- Dominio Admin: `sdd/specs/domains/admin/spec.md`

---

## 9. Próximos Pasos

1. **Aprobación**: Revisar tasks.md y obtener aprobación de arquitecto
2. **Implementación**: Ejecutar tareas en orden (seguir dependencias)
3. **Evidencia**: Registrar en evidence.md conforme se completen tareas
4. **Validación**: Ejecutar todas las pruebas
5. **Cierre**: Confirmar que todos los criterios de completitud se cumplen

---

**Fin de tasks.md**
