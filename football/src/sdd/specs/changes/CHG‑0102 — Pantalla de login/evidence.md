# CHG-0102 - Evidencia de la pantalla de login

## Estado
`validacion frontend aprobada con dependencia de contrato pendiente`

## Fecha de validacion

2026-08-19

## Implementacion validada

- Ruta publica `/login` servida por Spring MVC y reenviada a la pantalla estatica de login.
- Formulario accesible con email y contrasena protegida.
- Validacion local de email vacio, email invalido y contrasena vacia.
- Servicio frontend separado para `POST /api/sesiones/login`.
- Envio con el contrato frontend `{ email, password }`.
- Prevencion de envios duplicados y estado de carga con controles bloqueados.
- Traduccion de credenciales invalidas, usuario inexistente, respuesta invalida, fallo de red y error inesperado.
- Almacenamiento del token valido en `sessionStorage` bajo la clave `football.session.token`.
- Uso del token como `Authorization: Bearer <token>` en solicitudes protegidas.
- Limpieza de la sesion ante respuestas `401` o `403`.
- Redireccion al panel `/panel` despues de un login exitoso.
- Redireccion a `/login` cuando no existe sesion o el token es rechazado.
- Mensajes de estado accesibles mediante `role="status"` y `aria-live`.
- Adaptacion responsive para escritorio y movil.
- No se registran ni muestran contrasenas o tokens en la interfaz, URLs o mensajes de error.

## Pruebas ejecutadas

### Suite de aceptacion frontend

**Archivo:** `src/test/acceptance-frontend/login.spec.js`

**Comando:**

```text
npx playwright test src/test/acceptance-frontend/login.spec.js --workers=1
```

**Perfiles:** Chromium y mobile-chrome.

**Resultado:** 14 ejecuciones completadas, sin tests fallidos.

### Escenarios cubiertos

| Escenario | Resultado | Requisitos |
|---|---|---|
| Formulario, labels, contraseña protegida y enlace de registro | Pasado | RF-001, RNF-003 |
| Validacion local sin llamada al backend | Pasado | RF-002, RNF-004 |
| Solicitud unica, carga, payload, token y redireccion | Pasado | RF-003, RF-004, RF-005, RNF-001 |
| Credenciales invalidas y usuario inexistente | Pasado | RF-005, RF-006 |
| Respuesta invalida, fallo de red y reintento | Pasado | RF-006 |
| Token rechazado, limpieza y vuelta a login | Pasado | RF-004, RNF-002 |
| Teclado, foco, estado accesible y movil | Pasado | RNF-003 |

### Validaciones tecnicas

- `node --check` sobre los cuatro modulos JavaScript de login: pasado.
- `mvnw.cmd -q -DskipTests package`: pasado.
- Diagnosticos del editor sobre los archivos modificados: sin errores.

## Trazabilidad de requisitos

| Requisito | Evidencia |
|---|---|
| RF-001 | Formulario visible, labels y accion de inicio de sesion. |
| RF-002 | Errores de email y contrasena sin solicitud HTTP. |
| RF-003 | Solicitud POST unica con email y password, y estado de carga. |
| RF-004 | Token valido almacenado y enviado en solicitudes protegidas. |
| RF-005 | Redireccion a `/panel` tras autenticacion correcta. |
| RF-006 | Credenciales invalidas, usuario inexistente, respuesta invalida y red. |
| RNF-001 | No exposicion del token o contrasena en interfaz, URL o mensajes. |
| RNF-002 | Limpieza de sesion y redireccion ante token ausente o rechazado. |
| RNF-003 | Labels, foco visible, `aria-describedby`, `aria-invalid` y `aria-live`. |
| RNF-004 | Servicio separado y payload controlado para el endpoint de login. |

## Pendientes y limitaciones

- Las pruebas de aceptacion simulan el endpoint de sesiones con respuestas controladas; no sustituyen las pruebas de contrato del backend de CHG-0008.
- El checkout actual no contiene las clases backend del dominio de sesiones, por lo que la ruta `/api/sesiones/login`, la forma `{ token }` y los codigos de error deben confirmarse contra el contrato real de CHG-0008.
- La ruta `/panel` se valida como destino de navegacion; la implementacion del panel pertenece a CHG-0103.
- No se implementan registro, recuperacion de contrasena, panel de administrador ni cobros Stripe.

## Conclusion

La pantalla de login cumple los requisitos verificables del frontend de CHG-0102 y sus escenarios de aceptacion pasan en los perfiles configurados. El cierre completo del cambio queda condicionado a confirmar el contrato operativo de CHG-0008 y a integrar el panel de usuario de CHG-0103.
