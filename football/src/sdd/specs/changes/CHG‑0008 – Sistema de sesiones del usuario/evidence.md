# CHG-0008 - Evidencia de validacion

## Estado
`validacion parcial`

El flujo principal de sesiones esta implementado y validado mediante la suite de aceptacion backend. La validacion global del proyecto y algunos escenarios no ejercitados de forma dedicada permanecen pendientes.

## Fecha de validacion

2026-08-19

## Implementacion validada

- Inicio de sesion mediante `POST /api/sesiones/login`.
- Validacion de campos obligatorios y formato de email.
- Diferenciacion entre usuario inexistente y credenciales invalidas.
- Hash de contrasenas mediante BCrypt.
- Emision de tokens firmados HMAC-SHA256 con identificacion de usuario y expiracion.
- Persistencia de sesiones con hash SHA-256 del token.
- Cookie `football_session` HttpOnly, Secure y SameSite=Lax.
- Middleware para proteger rutas `/api` y establecer la identidad autenticada.
- Rechazo de tokens ausentes, manipulados, expirados o invalidados.
- Expiracion por inactividad y actualizacion del estado de la sesion.
- Logout autenticado con invalidacion de una unica sesion.
- Soporte de varias sesiones activas para el mismo usuario.
- Respuestas HTTP JSON con codigos de dominio sin exponer contrasenas, hashes ni tokens en errores.
- Separacion entre dominio, aplicacion, infraestructura y presentacion.

## Pruebas ejecutadas

### Suite de aceptacion backend

**Comando:**

```text
mvnw.cmd -q -Dtest=SesionAcceptanceTest test
```

**Resultado:**

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

### Escenarios validados

| Escenario | Resultado | Requisitos |
|---|---|---|
| Iniciar sesion con credenciales validas y crear varias sesiones | aprobado | RF-001, RF-002, RF-003, RF-007 |
| No exponer contrasena ni `passwordHash` en el login | aprobado | RF-002, RF-003, RNF-001 |
| Rechazar credenciales invalidas y usuario inexistente sin crear sesiones | aprobado | RF-001, RF-003, RNF-004, RNF-005 |
| Rechazar email invalido y campos ausentes | aprobado | RF-001, RNF-004 |
| Rechazar datos de login vacios | aprobado | RF-001, RNF-004, RNF-005 |
| Proteger rutas y exponer solo la identidad autenticada | aprobado | RF-004, RNF-002, RNF-004 |
| Cerrar una sesion, rechazar su reutilizacion y conservar otra sesion activa | aprobado | RF-006, RF-007, RNF-002, RNF-005 |
| Rechazar logout sin sesion autenticada | aprobado | RF-006, RNF-004 |
| Expirar una sesion por inactividad y no reactivarla | aprobado | RF-005, RNF-002, RNF-005 |

Nota: la tabla recoge nueve comprobaciones funcionales distribuidas en ocho metodos de prueba; el primer escenario tambien verifica la cookie segura y la unicidad de tokens.

### Compilacion

**Comando:**

```text
mvnw.cmd -DskipTests compile
```

**Resultado:** `BUILD SUCCESS`

### Diagnosticos

El analisis de errores Java no reporta errores en los archivos modificados de CHG-0008.

## Cobertura de requisitos

| Requisito | Estado | Evidencia |
|---|---|---|
| RF-001 - Iniciar sesion con credenciales | validado | Login correcto, campos obligatorios, formato de email, credenciales invalidas y usuario inexistente |
| RF-002 - Emitir token de sesion | validado | Token no vacio, expiracion, identidad, unicidad y ausencia de credenciales sensibles |
| RF-003 - Persistir y registrar la sesion | validado | Dos sesiones persistidas, estados y fechas gestionados por la capa de sesiones |
| RF-004 - Validar tokens en solicitudes protegidas | validado | Token ausente, token manipulado y token valido en `/api/usuarios/me` |
| RF-005 - Expirar sesiones | validado | Expiracion por inactividad, error `sesion.expirada` y estado `EXPIRADA` |
| RF-006 - Cerrar sesion manualmente | validado | Logout autenticado, invalidacion del token y rechazo de logout sin autenticacion |
| RF-007 - Permitir sesiones multiples | validado | Dos tokens independientes y segunda sesion activa tras cerrar la primera |
| RNF-001 - Seguridad de credenciales | validado parcialmente | BCrypt y ausencia de credenciales en respuestas; el canal seguro depende del entorno |
| RNF-002 - Seguridad de tokens | validado | Firma HMAC-SHA256, expiracion, hash persistido y rechazo de tokens manipulados o invalidados |
| RNF-003 - Separacion arquitectonica | validado | Capas de dominio, aplicacion, infraestructura y presentacion separadas |
| RNF-004 - Contrato HTTP estable | validado | Rutas, JSON, cookies, estados HTTP y codigos de dominio verificados |
| RNF-005 - Resiliencia y consistencia | validado parcialmente | Logout repetido y sesiones multiples verificadas; faltan pruebas dedicadas de concurrencia y almacenamiento no disponible |

## Pendientes y limitaciones

- Ejecutar la suite completa de pruebas del proyecto para comprobar regresiones globales.
- Añadir o ejecutar pruebas dedicadas de concurrencia de logins.
- Añadir o ejecutar pruebas dedicadas para indisponibilidad del almacenamiento de sesiones.
- Revisar el secreto alternativo de desarrollo (`app.session.secret`) antes de un despliegue productivo y exigir configuracion segura por entorno.
- Confirmar en el entorno de despliegue el uso obligatorio de HTTPS para el transporte de credenciales.
- Actualizar `tasks.md` y el estado formal del cambio cuando finalice la validacion global.

## Conclusion

La implementacion de sesiones de CHG-0008 funciona para el flujo validado de login, autenticacion de rutas protegidas, expiracion, logout y sesiones multiples: sus ocho pruebas de aceptacion pasan correctamente. El cambio no debe marcarse como completamente cerrado hasta ejecutar la validacion global y resolver las limitaciones indicadas.
