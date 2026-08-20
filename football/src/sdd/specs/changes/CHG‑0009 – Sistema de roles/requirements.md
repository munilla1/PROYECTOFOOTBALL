# CHG-0009 - Requisitos: Sistema de Roles

**Estado**: `aprobado`  
**Fecha**: 2026-08-20  
**Basado en**: proposal.md  
**Dominio**: Usuario, Roles, Admin

---

## 1. Resumen Ejecutivo

Implementar un sistema de roles que permita distinguir entre usuarios normales y administradores, controlando acceso a funciones internas del juego mediante autorización basada en roles.

---

## 2. Requisitos Funcionales (RF)

### RF-001: Asignación de roles a usuarios

**Descripción**: El sistema debe permitir asignar un rol (usuario o admin) a cada usuario en la base de datos.

**Criterios de Aceptación**:

#### CA-001.1: Campo rol en entidad Usuario
- **Dado** que existe la entidad Usuario en el dominio
- **Cuando** se crea o actualiza un usuario
- **Entonces** debe existir un campo `rol` que acepte valores: `usuario` o `admin`
- **Y** el rol debe tener un valor por defecto: `usuario`

#### CA-001.2: Persistencia del rol en base de datos
- **Dado** que se crea un usuario con rol `admin`
- **Cuando** se persiste en la base de datos
- **Entonces** la base de datos contiene una columna `rol` en la tabla `usuarios`
- **Y** el valor `admin` se almacena correctamente
- **Y** se puede recuperar sin cambios

#### CA-001.3: Modificación de rol
- **Dado** que existe un usuario con rol `usuario`
- **Cuando** un administrador cambia su rol a `admin`
- **Entonces** la base de datos actualiza el rol correctamente
- **Y** los cambios se reflejan en la próxima sesión

---

### RF-002: Middleware de autorización por rol

**Descripción**: El sistema debe validar el rol del usuario antes de permitir acceso a endpoints protegidos.

**Criterios de Aceptación**:

#### CA-002.1: Middleware valida rol de usuario
- **Dado** que un usuario realiza una solicitud HTTP a un endpoint protegido
- **Cuando** el middleware intercepta la solicitud
- **Entonces** extrae el token JWT de la sesión
- **Y** verifica que el rol del usuario sea el requerido para ese endpoint
- **Y** permite o rechaza la solicitud según el rol

#### CA-002.2: Rechazo de acceso no autorizado
- **Dado** que un usuario con rol `usuario` intenta acceder a `/admin/dashboard`
- **Cuando** el middleware valida el rol
- **Entonces** retorna HTTP 403 (Forbidden)
- **Y** incluye un mensaje de error: "Acceso denegado: se requiere rol admin"

#### CA-002.3: Continuación en acceso autorizado
- **Dado** que un usuario con rol `admin` realiza una solicitud a `/admin/logs`
- **Cuando** el middleware valida el rol
- **Entonces** permite que la solicitud continúe hacia el controlador
- **Y** no hay bloqueo

---

### RF-003: Endpoints de administración

**Descripción**: El sistema debe proporcionar endpoints para que los administradores gestionen configuraciones internas.

**Criterios de Aceptación**:

#### CA-003.1: Endpoint para listar logs del sistema
- **Dado** que un administrador realiza una solicitud GET a `/admin/logs`
- **Cuando** el middleware valida que tiene rol `admin`
- **Entonces** retorna HTTP 200
- **Y** responde con una lista de logs del sistema (formato JSON)
- **Y** incluye timestamp, nivel (INFO, ERROR, WARNING) y mensaje
- **Y** solo administradores pueden acceder

#### CA-003.2: Endpoint para revisar errores
- **Dado** que un administrador realiza una solicitud GET a `/admin/errors`
- **Cuando** el endpoint se ejecuta
- **Entonces** retorna HTTP 200
- **Y** responde con una lista de errores recientes
- **Y** incluye: timestamp, tipo de error, mensaje, stacktrace, usuario afectado
- **Y** solo administradores pueden acceder

#### CA-003.3: Endpoint para gestionar usuarios
- **Dado** que un administrador realiza una solicitud GET a `/admin/users`
- **Cuando** el endpoint se ejecuta
- **Entonces** retorna HTTP 200
- **Y** responde con lista de usuarios
- **Y** incluye: id, email, nombre, rol, fecha de creación, última sesión
- **Y** solo administradores pueden acceder

#### CA-003.4: Endpoint para cambiar rol de usuario
- **Dado** que un administrador realiza una solicitud PATCH a `/admin/users/{userId}/role`
- **Cuando** envía `{ "newRole": "admin" }`
- **Entonces** retorna HTTP 200
- **Y** actualiza el rol del usuario en la base de datos
- **Y** retorna el usuario actualizado
- **Y** solo administradores pueden acceder

#### CA-003.5: Endpoint para modificar configuraciones
- **Dado** que un administrador realiza una solicitud POST a `/admin/config`
- **Cuando** envía configuraciones válidas (ej: `{ "maxUsersPerDay": 500 }`)
- **Entonces** retorna HTTP 200
- **Y** persiste la configuración
- **Y** solo administradores pueden acceder

---

### RF-004: Sincronización de rol con sesión

**Descripción**: El rol del usuario debe estar disponible en la sesión después de autenticarse.

**Criterios de Aceptación**:

#### CA-004.1: Token JWT incluye rol
- **Dado** que un usuario se autentica correctamente
- **Cuando** el sistema genera el token JWT
- **Entonces** el payload del token incluye el campo `rol`
- **Y** el valor coincide con el rol en la base de datos
- **Y** se puede decodificar sin validación adicional

#### CA-004.2: Rol disponible en contexto de sesión
- **Dado** que una sesión activa existe para un usuario
- **Cuando** se accede a cualquier controlador
- **Entonces** está disponible el rol del usuario en el contexto
- **Y** puede ser consultado sin queries adicionales a la base de datos

---

### RF-005: Validación de cambios de rol

**Descripción**: El sistema debe validar cambios de rol para evitar inconsistencias.

**Criterios de Aceptación**:

#### CA-005.1: Validación de rol válido
- **Dado** que se intenta asignar un rol a un usuario
- **Cuando** se envía un valor que no es `usuario` o `admin`
- **Entonces** retorna HTTP 400 (Bad Request)
- **Y** incluye mensaje: "Rol inválido. Valores aceptados: usuario, admin"

#### CA-005.2: Un usuario no puede cambiar su propio rol
- **Dado** que un usuario realiza una solicitud PATCH a su propio perfil
- **Cuando** intenta cambiar su campo `rol`
- **Entonces** retorna HTTP 403 (Forbidden)
- **Y** incluye mensaje: "No puedes modificar tu propio rol"

#### CA-005.3: Solo administrador puede cambiar roles de otros
- **Dado** que un usuario con rol `usuario` realiza PATCH a `/users/{otherId}/role`
- **Cuando** intenta cambiar el rol de otro usuario
- **Entonces** retorna HTTP 403 (Forbidden)
- **Y** incluye mensaje: "Se requiere rol admin"

---

## 3. Requisitos No Funcionales (RNF)

### RNF-001: Seguridad

**Descripción**: El control de acceso basado en roles debe ser seguro y resistir intentos de escalación de privilegios.

**Criterios de Aceptación**:

#### CA-RNF-001.1: Validación de rol en cada solicitud
- **Dado** que se realiza una solicitud a un endpoint protegido
- **Cuando** el middleware procesa la solicitud
- **Entonces** siempre valida el rol (sin excepciones)
- **Y** no confía en valores enviados por el cliente
- **Y** valida contra el servidor (base de datos o sesión)

#### CA-RNF-001.2: Protección contra modificación de tokens
- **Dado** que un usuario intenta modificar su token JWT
- **Cuando** cambia el campo `rol` en el payload
- **Entonces** la firma del token es inválida
- **Y** el middleware rechaza la solicitud
- **Y** retorna HTTP 401 (Unauthorized)

#### CA-RNF-001.3: Auditoría de cambios de rol
- **Dado** que un administrador cambia el rol de un usuario
- **Cuando** realiza la operación PATCH en `/admin/users/{userId}/role`
- **Entonces** se registra en logs: quién cambió el rol, cuándo, rol anterior, rol nuevo
- **Y** los logs no se pueden modificar desde la aplicación

---

### RNF-002: Rendimiento

**Descripción**: La validación de roles no debe impactar significativamente el rendimiento de las solicitudes.

**Criterios de Aceptación**:

#### CA-RNF-002.1: Latencia de validación de rol
- **Dado** que se realiza una solicitud a un endpoint protegido
- **Cuando** el middleware valida el rol
- **Entonces** la validación se completa en < 10ms
- **Y** no introduce latencia significativa

#### CA-RNF-002.2: Caché de rol en sesión
- **Dado** que un usuario tiene una sesión activa
- **Cuando** realiza múltiples solicitudes
- **Entonces** el rol se obtiene de la sesión en caché
- **Y** no requiere consultas repetidas a la base de datos

---

### RNF-003: Compatibilidad con CHG-0008

**Descripción**: El sistema de roles debe integrarse correctamente con el sistema de sesiones (CHG-0008).

**Criterios de Aceptación**:

#### CA-RNF-003.1: Sincronización con sesiones
- **Dado** que existe el sistema de sesiones de CHG-0008
- **Cuando** se crea una sesión para un usuario
- **Entonces** la sesión incluye el rol actual del usuario
- **Y** los cambios de rol se reflejan en nuevas sesiones

#### CA-RNF-003.2: Invalidación de sesión al cambiar rol
- **Dado** que un administrador cambia el rol de un usuario
- **Cuando** la operación se completa
- **Entonces** la sesión anterior del usuario se invalida
- **Y** debe autenticarse nuevamente para obtener permisos actualizados

---

### RNF-004: Mantenibilidad

**Descripción**: El código debe seguir la arquitectura del proyecto y ser fácil de extender.

**Criterios de Aceptación**:

#### CA-RNF-004.1: Dominio sin dependencias
- **Dado** que se implementa la lógica de roles
- **Cuando** se separa por capas (dominio, aplicación, infraestructura)
- **Entonces** el dominio no tiene dependencias externas
- **Y** se puede usar independientemente

#### CA-RNF-004.2: Interfaz de repositorio
- **Dado** que se implementa persistencia de roles
- **Cuando** se crea el repositorio
- **Entonces** existe una interfaz `RolRepository`
- **Y** la implementación está desacoplada de la base de datos

---

## 4. Casos de Uso

### CU-01: Autenticación y carga de rol

1. Usuario envía credenciales
2. Sistema autentica usuario en CHG-0008
3. Sistema obtiene el rol del usuario
4. Sistema genera token JWT con rol incluido
5. Usuario recibe token y rol en respuesta

### CU-02: Acceso a endpoint protegido por rol

1. Usuario realiza solicitud a endpoint protegido
2. Middleware extrae token JWT
3. Middleware valida firma del token
4. Middleware verifica rol requerido vs rol del usuario
5. Si es válido: solicitud continúa
6. Si es inválido: retorna HTTP 403

### CU-03: Cambio de rol de usuario por administrador

1. Administrador accede a `/admin/users`
2. Sistema obtiene lista de usuarios (solo admin puede acceder)
3. Administrador selecciona usuario y nuevo rol
4. Administrador realiza PATCH a `/admin/users/{userId}/role`
5. Sistema valida que es admin
6. Sistema valida nuevo rol
7. Sistema persiste cambio
8. Sistema invalida sesiones antiguas del usuario
9. Sistema registra en auditoría

---

## 5. Reglas de Negocio

### RB-01: Roles válidos
Solo existen dos roles en el sistema:
- `usuario`: acceso a funciones normales del juego
- `admin`: acceso a funciones de administración

### RB-02: Rol por defecto
Todos los usuarios nuevos tienen rol `usuario` por defecto.

### RB-03: Principio de menor privilegio
Un usuario debe tener solo los permisos mínimos necesarios.

### RB-04: Auditoría de privilegios
Cualquier cambio de rol debe registrarse en auditoría.

### RB-05: No autoelevación
Un usuario con rol `usuario` no puede cambiar su propio rol.

---

## 6. Restricciones de Seguridad

- **Validación de rol**: siempre en el servidor, nunca confiar en el cliente
- **JWT**: la firma debe ser inmutable desde el cliente
- **Auditoría**: todos los cambios de rol deben registrarse
- **Sesión**: invalidar sesiones antiguas cuando cambia el rol
- **Contraseña**: cambio de rol no requiere re-autenticación, pero sí invalida sesiones

---

## 7. Supuestos y Dependencias

### Supuestos
- El sistema de sesiones (CHG-0008) está implementado y estable
- La base de datos puede almacenar un campo `rol` en usuarios
- Existe un sistema de logs en el backend

### Dependencias
- **CHG-0008**: Sistema de sesiones del usuario (obligatorio)
- Base de datos: soporte para migraciones

---

## 8. Criterios de Completitud

El requisito se considera completo cuando:

1. ✓ Field `rol` existe en entidad Usuario y base de datos
2. ✓ Middleware de autorización valida rol en todas las solicitudes protegidas
3. ✓ Endpoints `/admin/logs`, `/admin/errors`, `/admin/users`, `/admin/users/{id}/role`, `/admin/config` funcionan correctamente
4. ✓ Token JWT incluye rol y es inmutable
5. ✓ Cambios de rol se registran en auditoría
6. ✓ Sesiones se invalidan al cambiar rol
7. ✓ No hay escalación de privilegios
8. ✓ Rendimiento < 10ms en validación de rol
9. ✓ Pruebas de aceptación pasan

---

## 9. Referencias

- `proposal.md`: Definición del problema y alcance
- `CHG-0008`: Sistema de sesiones del usuario
- Dominio: `sdd/specs/domains/usuario/spec.md`
- Dominio: `sdd/specs/domains/admin/spec.md`

---

## 10. Aprobaciones

| Rol | Nombre | Fecha | Firma |
|-----|--------|-------|-------|
| PO | Pendiente | - | - |
| Arquitecto | Pendiente | - | - |
| Seguridad | Pendiente | - | - |

---

**Fin de requirements.md**
