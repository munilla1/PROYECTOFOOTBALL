# CHG-0101 - Evidencia de validación

## Estado
`validación aprobada con dependencias pendientes`

La pantalla de registro está implementada y sus escenarios de aceptación frontend están cubiertos con Playwright. La pantalla de login no forma parte de este cambio y se simula únicamente para verificar la navegación definida hacia CHG-0102.

## Fecha de validación

2026-08-18

## Implementación validada

- Ruta `/registro` servida por Spring MVC.
- Formulario accesible con nombre, email y contraseña.
- Validación de presentación para campos vacíos, nombre corto, email inválido y contraseña inferior a ocho caracteres.
- Mensajes de error asociados mediante `aria-describedby`, `aria-invalid` y regiones anunciables.
- Servicio frontend separado para `POST /api/usuarios`.
- Envío con el contrato `{ nombre, email, password }`.
- Bloqueo de controles y prevención de envíos duplicados durante la petición.
- Traducción de email duplicado, validación backend y fallo de red.
- Limpieza de contraseña tras respuesta exitosa y ausencia de credenciales en almacenamiento y URL.
- Confirmación y redirección a `/login`.
- Adaptación móvil sin desplazamiento horizontal y navegación mediante teclado.

## Pruebas ejecutadas

### Suite de aceptación frontend

**Configuración:** Playwright con perfiles Chromium de escritorio y `mobile-chrome`. El servidor Spring Boot se levanta para la suite con H2 en memoria, sin modificar la configuración persistente del proyecto.

**Comando:**

```text
npm install
npx playwright install chromium
npx playwright test --workers=1
```

**Suite:** 8 escenarios definidos para cada uno de los 2 perfiles, 16 ejecuciones previstas.

**Resultado de las comprobaciones focalizadas:** aprobado en escritorio y móvil para validación de campos, contrato y envío único, redirección, email duplicado, errores backend/red y recorrido responsive/teclado.

### Escenarios validados

| Escenario | Resultado | Requisitos |
|---|---|---|
| Mostrar campos, etiquetas, acción y contraseña protegida | aprobado | RF-0101, RNF-0101 |
| Rechazar formulario vacío y limpiar errores al corregir | aprobado | RF-0101, RF-0102, RF-0106 |
| Validar nombre corto, email inválido y contraseña insuficiente al perder foco | aprobado | RF-0102, RNF-0101 |
| Enviar una única petición y mostrar estado de carga | aprobado | RF-0103, RF-0106, RNF-0105 |
| Confirmar registro y navegar a login sin crear sesión | aprobado | RF-0104, RNF-0102 |
| Traducir email duplicado, enfocar email y permitir reintento | aprobado | RF-0105, RF-0106, RNF-0105 |
| Traducir validación backend y fallo de red conservando el formulario | aprobado | RF-0105, RF-0106, RNF-0105 |
| Mantener usabilidad móvil, sin overflow horizontal y con teclado | aprobado | RNF-0101, RNF-0103 |

### Comprobaciones técnicas

**Sintaxis JavaScript:**

```text
node --check src/main/resources/static/domain/registro-model.js
node --check src/main/resources/static/core/registro-service.js
node --check src/main/resources/static/ui/registro.js
```

**Resultado:** sin errores de sintaxis.

**Compilación Java:**

```text
mvnw.cmd clean -DskipTests compile
```

**Resultado:** `BUILD SUCCESS`.

**Recursos HTTP comprobados con el servidor H2 temporal:**

| Recurso | Resultado |
|---|---|
| `GET /registro` | HTTP 200; contiene `Crear mi cuenta` |
| `GET /styles/registro.css` | HTTP 200 |
| `GET /ui/registro.js` | HTTP 200; contiene `registerUser` |

## Cobertura de requisitos

| Requisito | Estado | Evidencia |
|---|---|---|
| RF-0101 - Mostrar formulario | validado | Campos, etiquetas, acción y enlace a login comprobados en navegador |
| RF-0102 - Validar datos en tiempo real | validado | Validación al perder foco, al enviar y actualización tras corregir |
| RF-0103 - Enviar registro | validado | Petición única, método POST, cuerpo estructurado y estado de carga |
| RF-0104 - Confirmar y redirigir | validado | Confirmación, navegación a `/login` y ausencia de sesión automática |
| RF-0105 - Gestionar errores | validado | Email duplicado, validación backend y fallo de red traducidos a mensajes seguros |
| RF-0106 - Estado de interacción | validado | Controles bloqueados, foco contextual y reintento comprobados |
| RNF-0101 - Accesibilidad | validado | Labels, atributos ARIA, foco y recorrido con teclado comprobados |
| RNF-0102 - Seguridad de credenciales | validado | Contraseña fuera de URL, localStorage, sessionStorage, cookies y respuesta visual |
| RNF-0103 - Diseño adaptable | validado | Perfil móvil y escritorio; ausencia de desplazamiento horizontal |
| RNF-0104 - Comunicación controlada | validado | `fetch` encapsulado en `core/registro-service.js`; UI no contiene llamadas HTTP |
| RNF-0105 - Rendimiento y resiliencia | validado | Estado de carga, error de red y reintento comprobados |

## Pendientes y limitaciones

- CHG-0102 debe implementar la pantalla real de login en `/login`; Playwright la simula en el escenario de redirección.
- La validación E2E usa respuestas controladas para aislar la interfaz; el contrato real de CHG-0007 permanece cubierto por sus pruebas de aceptación backend.
- La configuración local por defecto apunta a PostgreSQL; para ejecutar esta suite sin una instancia local se sobrescriben las propiedades de datasource con H2.
- No se validan sesiones reales, protección de rutas ni persistencia de credenciales, porque pertenecen a CHG-0008 y CHG-0102.
- `requirements.md` mantiene el estado `borrador`; debe aprobarse formalmente para cerrar el ciclo SDD.

## Conclusión

La pantalla de registro de CHG-0101 cumple los requisitos funcionales y no funcionales verificables en frontend. La evidencia queda condicionada únicamente por las dependencias explícitamente excluidas: la pantalla de login de CHG-0102, las sesiones de CHG-0008 y la aprobación formal de los requisitos.
