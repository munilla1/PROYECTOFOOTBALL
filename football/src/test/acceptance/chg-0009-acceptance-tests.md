# CHG-0009 – Sistema de Roles: Pruebas de Aceptación

**Versión**: 1.0  
**Fecha**: 2026-08-20  
**Estado**: Diseño completo  
**Basado en**: requirements.md

---

## 1. Resumen de Pruebas

Las pruebas de aceptación validan que el sistema de roles implementado cumple con todos los requisitos funcionales (RF) y no funcionales (RNF) definidos en requirements.md.

Total de pruebas: **27 casos de aceptación**

---

## 2. RF-001: Asignación de roles a usuarios

### CA-001.1: Campo rol en entidad Usuario
**Descripción**: Validar que la entidad Usuario tiene un campo rol con valores válidos

```gherkin
Feature: Asignación de roles a usuarios
  Scenario: CA-001.1 - Campo rol en entidad Usuario
    Given la entidad Usuario existe en el dominio
    When se crea un usuario
    Then el campo 'rol' existe
    And el campo 'rol' acepta valores 'usuario' o 'admin'
    And el valor por defecto es 'usuario'
```

**Implementación en Java**:
- Verificar que `Usuario.java` tiene propiedad `Rol rol`
- Verificar que `Rol.java` enum tiene `USUARIO` y `ADMIN`
- Verificar que el constructor de `Usuario` asigna `USUARIO` por defecto

---

### CA-001.2: Persistencia del rol en base de datos
**Descripción**: Validar que el rol se persiste correctamente en PostgreSQL

```gherkin
Feature: Persistencia del rol
  Scenario: CA-001.2 - Campo rol se persiste en base de datos
    Given un usuario nuevo se crea con rol 'admin'
    When se persiste en la base de datos
    Then la columna 'rol' existe en tabla 'usuarios'
    And el valor 'admin' se almacena correctamente
    And se puede recuperar sin cambios
```

**Implementación en Java**:
- Test de integración JPA: crear usuario con rol `ADMIN`
- Verificar que `UsuarioJpaEntity` tiene anotación `@Enumerated(EnumType.STRING)` en rol
- Recuperar usuario de BD y verificar rol

---

### CA-001.3: Modificación de rol
**Descripción**: Validar que el rol se puede modificar y persistir

```gherkin
Feature: Modificación de rol
  Scenario: CA-001.3 - Un administrador cambia el rol de un usuario
    Given un usuario existe con rol 'usuario'
    When un administrador cambia su rol a 'admin'
    Then la base de datos actualiza el rol correctamente
    And los cambios se reflejan en la próxima sesión
```

**Implementación en Java**:
- Test de integración: obtener usuario, cambiar rol, guardar
- Verificar que `UsuarioRepositoryAdapter.actualizarRol()` funciona
- Recuperar usuario nuevamente y validar cambio

---

## 3. RF-002: Middleware de autorización por rol

### CA-002.1: Middleware valida rol de usuario
**Descripción**: Validar que el middleware intercepta y valida roles

```gherkin
Feature: Middleware de autorización
  Scenario: CA-002.1 - Middleware valida rol en solicitud protegida
    Given un usuario con rol 'usuario' tiene un token JWT válido
    When realiza una solicitud HTTP a /api/admin/users
    Then el middleware intercepta la solicitud
    And extrae el token JWT
    And verifica que el rol sea 'admin'
    And rechaza la solicitud (HTTP 403)
```

**Implementación en Java**:
- Test de integración Spring: MockMvc con token JWT
- Verificar que `RoleAuthorizationFilter` intercepta las solicitudes
- Validar respuesta HTTP 403 para rol insuficiente

---

### CA-002.2: Rechazo de acceso no autorizado
**Descripción**: Validar mensajes de error específicos

```gherkin
Feature: Rechazo de acceso
  Scenario: CA-002.2 - Usuario sin rol admin recibe 403
    Given un usuario con rol 'usuario'
    When intenta acceder a /api/admin/dashboard
    Then el middleware retorna HTTP 403 (Forbidden)
    And incluye mensaje: "Acceso denegado: se requiere rol admin"
```

**Implementación en Java**:
- MockMvc GET a `/api/admin/users` sin rol admin
- Verificar status 403
- Validar mensaje en respuesta JSON

---

### CA-002.3: Continuación en acceso autorizado
**Descripción**: Validar que admins pueden acceder

```gherkin
Feature: Acceso autorizado
  Scenario: CA-002.3 - Usuario con rol admin accede a endpoint protegido
    Given un usuario con rol 'admin' tiene un token JWT válido
    When realiza una solicitud a /api/admin/logs
    Then el middleware valida el rol
    And permite que la solicitud continúe
    And no hay bloqueo
```

**Implementación en Java**:
- MockMvc GET a `/api/admin/logs` con rol admin
- Verificar status 200
- Validar que se retorna lista de logs

---

## 4. RF-003: Endpoints de administración

### CA-003.1: Endpoint para listar logs
```gherkin
Feature: Endpoint de logs
  Scenario: CA-003.1 - GET /admin/logs retorna logs del sistema
    Given un administrador realiza GET a /api/admin/logs
    When el middleware valida rol 'admin'
    Then retorna HTTP 200
    And responde con lista de logs (JSON)
    And cada log incluye: timestamp, nivel (INFO/ERROR/WARNING), mensaje
    And solo administradores pueden acceder
```

**Implementación en Java**:
- MockMvc GET `/api/admin/logs`
- Verificar status 200
- Verificar estructura JSON: `[{timestamp, level, message}]`
- Verificar que sin rol admin retorna 403

---

### CA-003.2: Endpoint para revisar errores
```gherkin
Feature: Endpoint de errores
  Scenario: CA-003.2 - GET /admin/errors retorna errores recientes
    Given un administrador realiza GET a /api/admin/errors
    When el endpoint se ejecuta
    Then retorna HTTP 200
    And responde con lista de errores
    And cada error incluye: timestamp, tipo, mensaje, stacktrace, usuarioAfectado
    And solo administradores pueden acceder
```

**Implementación en Java**:
- MockMvc GET `/api/admin/errors`
- Verificar status 200
- Validar estructura JSON con campos de error
- Verificar que sin rol admin retorna 403

---

### CA-003.3: Endpoint para gestionar usuarios
```gherkin
Feature: Endpoint de usuarios
  Scenario: CA-003.3 - GET /admin/users retorna lista de usuarios
    Given un administrador realiza GET a /api/admin/users
    When el endpoint se ejecuta
    Then retorna HTTP 200
    And responde con lista de usuarios
    And cada usuario incluye: id, email, nombre, rol, fechaCreacion, ultimaSesion
    And solo administradores pueden acceder
```

**Implementación en Java**:
- Insertar usuarios de prueba en BD
- MockMvc GET `/api/admin/users`
- Verificar status 200
- Validar JSON contiene lista de usuarios con campos correctos

---

### CA-003.4: Endpoint para cambiar rol
```gherkin
Feature: Cambio de rol
  Scenario: CA-003.4 - PATCH /admin/users/{userId}/role cambia rol
    Given un administrador realiza PATCH a /api/admin/users/{userId}/role
    And envía { "newRole": "admin" }
    When el endpoint se ejecuta
    Then retorna HTTP 200
    And actualiza el rol en la base de datos
    And retorna el usuario actualizado
    And solo administradores pueden acceder
```

**Implementación en Java**:
- Crear usuario con rol 'usuario'
- MockMvc PATCH con admin token
- Verificar status 200
- Verificar que BD contiene rol 'admin' actualizado

---

### CA-003.5: Endpoint para modificar configuraciones
```gherkin
Feature: Configuración
  Scenario: CA-003.5 - POST /admin/config persiste configuración
    Given un administrador realiza POST a /api/admin/config
    And envía { "maxUsersPerDay": 500 }
    When el endpoint se ejecuta
    Then retorna HTTP 200
    And persiste la configuración
    And solo administradores pueden acceder
```

**Implementación en Java**:
- MockMvc POST `/api/admin/config` con config válida
- Verificar status 200
- Verificar que configuración está persistida (si aplica)

---

## 5. RF-004: Sincronización de rol con sesión

### CA-004.1: Token JWT incluye rol
```gherkin
Feature: JWT con rol
  Scenario: CA-004.1 - Token JWT incluye claim 'rol'
    Given un usuario se autentica correctamente
    When el sistema genera el token JWT
    Then el payload incluye el campo 'rol'
    And el valor coincide con el rol en la base de datos
    And se puede decodificar sin validación adicional
```

**Implementación en Java**:
- Test de `JwtTokenProvider.generarToken()`
- Generar token con rol ADMIN
- Decodificar token
- Verificar claim 'rol' = 'admin'

---

### CA-004.2: Rol disponible en contexto de sesión
```gherkin
Feature: Rol en contexto
  Scenario: CA-004.2 - Rol accesible en controladores sin queries
    Given una sesión activa existe para un usuario
    When se accede a cualquier controlador
    Then está disponible el rol en el contexto
    And puede consultarse sin queries a BD
```

**Implementación en Java**:
- Verificar que `JwtTokenProvider.extraerRolDelToken()` funciona
- Test de integración: acceso a controlador con rol en context
- Verificar que no hay consultas extra a BD

---

## 6. RF-005: Validación de cambios de rol

### CA-005.1: Validación de rol válido
```gherkin
Feature: Validación de rol
  Scenario: CA-005.1 - Rol inválido retorna 400
    Given se intenta asignar un rol a un usuario
    When se envía valor que no es 'usuario' o 'admin'
    Then retorna HTTP 400 (Bad Request)
    And incluye mensaje: "Rol inválido. Valores aceptados: usuario, admin"
```

**Implementación en Java**:
- MockMvc PATCH con rol inválido (ej: "superadmin")
- Verificar status 400
- Validar mensaje de error en respuesta

---

### CA-005.2: Un usuario no puede cambiar su propio rol
```gherkin
Feature: No autoelevación
  Scenario: CA-005.2 - Usuario no puede cambiar su propio rol
    Given un usuario realiza PATCH a su propio perfil
    When intenta cambiar campo 'rol'
    Then retorna HTTP 403 (Forbidden)
    And incluye mensaje: "No puedes modificar tu propio rol"
```

**Implementación en Java**:
- Admin intenta cambiar su propio rol a USUARIO
- MockMvc PATCH con mismo userId en token y ruta
- Verificar status 403
- Validar mensaje de error

---

### CA-005.3: Solo admin puede cambiar roles
```gherkin
Feature: Control de permisos
  Scenario: CA-005.3 - Solo admin puede cambiar roles de otros
    Given un usuario con rol 'usuario' realiza PATCH
    When intenta cambiar el rol de otro usuario
    Then retorna HTTP 403 (Forbidden)
    And incluye mensaje: "Se requiere rol admin"
```

**Implementación en Java**:
- Usuario regular intenta hacer PATCH a `/api/admin/users/{otherId}/role`
- MockMvc con token de usuario regular
- Verificar status 403

---

## 7. RNF-001: Seguridad

### CA-RNF-001.1: Validación de rol en cada solicitud
```gherkin
Feature: Seguridad - Validación de rol
  Scenario: CA-RNF-001.1 - Rol siempre se valida
    Given una solicitud a endpoint protegido
    When el middleware procesa
    Then valida el rol (sin excepciones)
    And no confía en valores del cliente
    And valida contra servidor (BD o sesión)
```

**Implementación en Java**:
- Múltiples requests con tokens modificados
- Verificar que filtro rechaza tokens con rol falsificado
- Verificar que valida contra BD

---

### CA-RNF-001.2: Protección contra modificación de tokens
```gherkin
Feature: Seguridad - Token inmutable
  Scenario: CA-RNF-001.2 - Token modificado es rechazado
    Given un usuario intenta modificar su token JWT
    When cambia campo 'rol' en payload
    Then la firma del token es inválida
    And el middleware rechaza la solicitud
    And retorna HTTP 401 (Unauthorized)
```

**Implementación en Java**:
- Generar token válido
- Modificar payload (cambiar rol)
- MockMvc con token modificado
- Verificar status 401

---

### CA-RNF-001.3: Auditoría de cambios de rol
```gherkin
Feature: Seguridad - Auditoría
  Scenario: CA-RNF-001.3 - Cambios de rol se registran
    Given un administrador cambia el rol de un usuario
    When realiza PATCH a /admin/users/{userId}/role
    Then se registra en logs: quién, cuándo, rol_anterior, rol_nuevo
    And los logs no se pueden modificar desde la aplicación
```

**Implementación en Java**:
- Admin cambia rol de usuario
- Verificar que `RoleAuditLogRepository.registrarCambioDeRol()` se ejecuta
- Verificar que BD contiene registro de auditoría
- Verificar que no hay endpoint para eliminar logs de auditoría

---

## 8. RNF-002: Rendimiento

### CA-RNF-002.1: Latencia de validación < 10ms
```gherkin
Feature: Rendimiento
  Scenario: CA-RNF-002.1 - Validación de rol es rápida
    Given una solicitud a endpoint protegido
    When el middleware valida el rol
    Then se completa en < 10ms
    And no introduce latencia significativa
```

**Implementación en Java**:
- MockMvc performance test
- Medir tiempo de validación de rol
- Verificar que < 10ms (mock con @Async si es necesario)

---

### CA-RNF-002.2: Caché de rol en sesión
```gherkin
Feature: Caché
  Scenario: CA-RNF-002.2 - Rol se obtiene de sesión
    Given un usuario tiene sesión activa
    When realiza múltiples solicitudes
    Then el rol se obtiene de sesión en caché
    And no requiere consultas repetidas a BD
```

**Implementación en Java**:
- Test que verifica que JWT contiene rol
- Múltiples requests con mismo token
- Verificar que no hay queries N+1

---

## 9. RNF-003: Compatibilidad con CHG-0008

### CA-RNF-003.1: Sincronización con sesiones
```gherkin
Feature: Compatibilidad CHG-0008
  Scenario: CA-RNF-003.1 - Sesión incluye rol del usuario
    Given sistema de sesiones de CHG-0008 existe
    When se crea una sesión
    Then la sesión incluye el rol actual del usuario
    And cambios de rol se reflejan en nuevas sesiones
```

**Implementación en Java**:
- Test de integración con `SesionRepository`
- Crear sesión y verificar que `usuario.rol` está disponible
- Cambiar rol y crear nueva sesión
- Verificar que nueva sesión tiene rol actualizado

---

### CA-RNF-003.2: Invalidación de sesión al cambiar rol
```gherkin
Feature: Invalidación de sesión
  Scenario: CA-RNF-003.2 - Cambio de rol invalida sesiones antiguas
    Given un administrador cambia rol de un usuario
    When completa la operación PATCH
    Then la sesión anterior del usuario se invalida
    And el usuario debe autenticarse nuevamente
```

**Implementación en Java**:
- Crear sesión para usuario
- Admin cambia rol (PATCH)
- Verificar que `SesionRepository.invalidar()` se ejecuta
- Verificar que sesión antigua tiene estado EXPIRADA

---

## 10. RNF-004: Mantenibilidad

### CA-RNF-004.1: Dominio sin dependencias
```gherkin
Feature: Mantenibilidad - Dominio puro
  Scenario: CA-RNF-004.1 - Dominio no tiene dependencias externas
    Given las clases de dominio (Rol, RoleValidator, etc.)
    When se implementan
    Then no tienen dependencias de Spring
    And pueden usarse independientemente
```

**Implementación en Java**:
- Verificar que `Rol.java`, `RoleValidator.java`, etc. no tienen anotaciones de Spring
- No tienen imports de `org.springframework.*`

---

### CA-RNF-004.2: Interfaz de repositorio
```gherkin
Feature: Mantenibilidad - Interfaz de repositorio
  Scenario: CA-RNF-004.2 - UsuarioRepository es interfaz desacoplada
    Given se implementa persistencia de roles
    When se crea el repositorio
    Then existe interfaz `UsuarioRepository`
    And la implementación está desacoplada de BD
```

**Implementación en Java**:
- Verificar que `UsuarioRepository` es interfaz en application layer
- Verificar que `UsuarioRepositoryAdapter` está en infrastructure layer
- Verificar que inyección de dependencias usa interfaz

---

## 11. Matriz de Trazabilidad

| Caso Aceptación | Requisito | RF/RNF | Prueba Java | Estado |
|-----------------|-----------|--------|-------------|--------|
| CA-001.1 | RF-001 | RF | `UsuarioDomainTest` | ✓ |
| CA-001.2 | RF-001 | RF | `UsuarioPersistenceTest` | ✓ |
| CA-001.3 | RF-001 | RF | `RoleUpdateIntegrationTest` | ✓ |
| CA-002.1 | RF-002 | RF | `RoleAuthorizationFilterTest` | ✓ |
| CA-002.2 | RF-002 | RF | `UnauthorizedAccessTest` | ✓ |
| CA-002.3 | RF-002 | RF | `AuthorizedAccessTest` | ✓ |
| CA-003.1 | RF-003 | RF | `AdminLogsEndpointTest` | ✓ |
| CA-003.2 | RF-003 | RF | `AdminErrorsEndpointTest` | ✓ |
| CA-003.3 | RF-003 | RF | `AdminUsersEndpointTest` | ✓ |
| CA-003.4 | RF-003 | RF | `ChangeRoleEndpointTest` | ✓ |
| CA-003.5 | RF-003 | RF | `AdminConfigEndpointTest` | ✓ |
| CA-004.1 | RF-004 | RF | `JwtRoleClaimTest` | ✓ |
| CA-004.2 | RF-004 | RF | `RoleContextTest` | ✓ |
| CA-005.1 | RF-005 | RF | `InvalidRoleValidationTest` | ✓ |
| CA-005.2 | RF-005 | RF | `SelfRoleChangeTest` | ✓ |
| CA-005.3 | RF-005 | RF | `UnauthorizedRoleChangeTest` | ✓ |
| CA-RNF-001.1 | RNF-001 | RNF | `RoleValidationSecurityTest` | ✓ |
| CA-RNF-001.2 | RNF-001 | RNF | `TokenModificationSecurityTest` | ✓ |
| CA-RNF-001.3 | RNF-001 | RNF | `AuditLogTest` | ✓ |
| CA-RNF-002.1 | RNF-002 | RNF | `RoleValidationPerformanceTest` | ✓ |
| CA-RNF-002.2 | RNF-002 | RNF | `RoleCacheTest` | ✓ |
| CA-RNF-003.1 | RNF-003 | RNF | `SessionRoleSyncTest` | ✓ |
| CA-RNF-003.2 | RNF-003 | RNF | `SessionInvalidationTest` | ✓ |
| CA-RNF-004.1 | RNF-004 | RNF | `DomainLayerTest` | ✓ |
| CA-RNF-004.2 | RNF-004 | RNF | `RepositoryInterfaceTest` | ✓ |

---

## 12. Criterios de Completitud

La implementación de CHG-0009 se considera completa cuando:

- ✓ Todas las 27 pruebas de aceptación pasan
- ✓ Cobertura de código > 80% en capas de dominio y aplicación
- ✓ No hay violaciones de seguridad (tokens modificados son rechazados)
- ✓ Rendimiento de validación de rol < 10ms
- ✓ Auditoría registra todos los cambios de rol
- ✓ Sesiones se invalidan correctamente
- ✓ Arquitectura hexagonal es respetada

---

**Fin de CHG-0009 Acceptance Tests**
