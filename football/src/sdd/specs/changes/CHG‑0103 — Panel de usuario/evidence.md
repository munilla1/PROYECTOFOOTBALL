# CHG-0103 - Evidencia del panel de usuario

## Estado
`validacion frontend aprobada con contrato de Checkout pendiente`

## Fecha de validacion

2026-08-19

## Implementacion validada

- Ruta `/panel` servida por Spring MVC y reenviada a la pantalla estatica del panel.
- Proteccion frontend mediante el store de sesion de CHG-0102.
- Redireccion a `/login` cuando no existe token o el backend rechaza la sesion.
- Servicio Core para consultar `GET /api/usuarios/me` con `Authorization: Bearer <token>`.
- Adaptador Domain para nombre, email, membresia, estado del jugador, nivel, XP y energia.
- Valores neutros para datos opcionales ausentes, sin mostrar `undefined` o `null`.
- Estados de carga, error recuperable y reintento.
- Tarjeta de membresia con Checkout solo para usuarios sin membresia activa.
- Servicio Core de Checkout sin gestionar datos de tarjeta en el frontend.
- Accesos a estadisticas, jornadas, fichajes y progresion.
- Cierre de sesion con limpieza de `sessionStorage` y redireccion a `/login`.
- Regiones anunciables mediante `role="status"` y `aria-live`.
- Navegacion mediante teclado y layout responsive para escritorio y movil.
- Separacion de capas entre `ui`, `core`, `domain` y `styles`.

## Pruebas ejecutadas

### Suite de aceptacion frontend

**Archivo:** `src/test/acceptance-frontend/panel.spec.js`

**Comando:**

```text
npx playwright test src/test/acceptance-frontend/panel.spec.js --project=chromium --timeout=10000 --reporter=line
```

**Resultado:** 10 pruebas ejecutadas, 10 pasadas y 0 fallos.

Las pruebas utilizan respuestas controladas para los endpoints protegidos. El escenario responsive establece un viewport de 390 x 844 px dentro del proyecto Chromium.

### Escenarios cubiertos

| Escenario | Resultado | Requisitos |
|---|---|---|
| Acceso sin sesion y redireccion a login | Pasado | RF-001, RF-009 |
| Carga del perfil, contrato HTTP y datos visibles | Pasado | RF-001, RF-002, RNF-001, RNF-005 |
| Valores neutros para campos opcionales y campos desconocidos | Pasado | RF-002, RNF-002, RNF-005 |
| Estado de carga, error de red y reintento | Pasado | RF-003, RNF-003, RNF-005 |
| Token rechazado, limpieza de sesion y redireccion | Pasado | RF-001, RF-003, RNF-001 |
| Checkout para membresia inactiva y redireccion a la URL recibida | Pasado | RF-008, RNF-001, RNF-005 |
| Error recuperable de Checkout conservando el perfil | Pasado | RF-008, RNF-005 |
| Cuatro accesos y ruta de estadisticas | Pasado | RF-004, RF-005, RF-006, RF-007, RNF-003 |
| Cierre de sesion, limpieza local y bloqueo posterior | Pasado | RF-009, RNF-001 |
| Teclado, regiones accesibles y viewport movil sin overflow horizontal | Pasado | RNF-003, RNF-004 |

### Validaciones tecnicas

- `node --check` sobre `panel.js`, `panel-model.js`, `user-service.js` y `checkout-service.js`: pasado.
- `mvnw.cmd -q -DskipTests package`: pasado.
- Diagnosticos del editor sobre los archivos de CHG-0103: sin errores.
- Las peticiones protegidas verifican el metodo, la ruta y la cabecera `Authorization`.
- Las peticiones de Checkout verifican `POST`, payload vacio y ausencia de datos de tarjeta, CVV o CVC.
- La interfaz no muestra el token de sesion ni campos desconocidos del backend.

## Trazabilidad de requisitos

| Requisito | Evidencia |
|---|---|
| RF-001 | Acceso sin token, carga con token y redireccion ante respuesta `401`. |
| RF-002 | Nombre, email, membresia, estado del jugador y progreso; valores neutros para ausentes. |
| RF-003 | Mensaje de carga, error de red, reintento exitoso y sesion no autorizada. |
| RF-004 | Acceso identificable a estadisticas reales y ruta `/estadisticas`. |
| RF-005 | Acceso identificable con ruta `/jornadas`. |
| RF-006 | Acceso identificable con ruta `/fichajes`. |
| RF-007 | Acceso identificable con ruta `/progresion`. |
| RF-008 | Checkout visible solo sin membresia activa, redireccion, error recuperable y ausencia de datos de tarjeta. |
| RF-009 | Logout, limpieza de `sessionStorage`, redireccion y bloqueo del acceso posterior. |
| RNF-001 | Token enviado solo como cabecera protegida y no expuesto en interfaz o almacenamiento tras logout. |
| RNF-002 | Servicios Core y adaptador Domain comprobados mediante el flujo del panel. |
| RNF-003 | Labels implícitos por nombres accesibles, foco, orden de teclado y regiones `aria-live`. |
| RNF-004 | Viewport movil sin desplazamiento horizontal y controles utilizables. |
| RNF-005 | Rutas, metodos, cabeceras, payloads, respuestas controladas y reintentos verificados. |

## Pendientes y limitaciones

- Las pruebas simulan `GET /api/usuarios/me` y `POST /api/membresias/checkout`; no sustituyen las pruebas de contrato o integracion del backend.
- El endpoint backend real de Checkout no esta disponible en el checkout actual. La implementacion usa `/api/membresias/checkout` como contrato provisional y espera una respuesta `{ url }`.
- Las rutas `/estadisticas`, `/jornadas`, `/fichajes` y `/progresion` se validan como accesos y destinos definidos; sus vistas pertenecen a cambios posteriores.
- No se ejecutaron pruebas del perfil `mobile-chrome` de Playwright; la cobertura movil se valido mediante viewport de 390 x 844 px dentro del escenario de accesibilidad.
- No se implementan el backend de membresias, Stripe, las vistas secundarias ni el panel de administrador.

## Conclusion

El frontend de CHG-0103 cumple los requisitos verificables del panel de usuario y sus 10 escenarios de aceptacion pasan en Chromium. El cierre completo del cambio queda condicionado a confirmar el contrato operativo del endpoint de Checkout y a integrar las vistas secundarias cuando correspondan.
