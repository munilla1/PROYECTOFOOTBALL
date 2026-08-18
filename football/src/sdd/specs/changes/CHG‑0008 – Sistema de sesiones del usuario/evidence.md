# CHG‑0008 — Evidencia de validación

## Estado  
`validado`

El ciclo de sesiones queda completamente implementado y validado. Se han corregido los dos fallos detectados en la validación parcial anterior:  
- La expiración por inactividad ahora se persiste correctamente como `EXPIRADA`.  
- Los datos de login inválidos devuelven `400 sesion.datos-invalidos` en lugar de `401`.

No ha sido necesario modificar el filtro de autenticación ni ningún controlador.

---

## Fecha de validación  
2026‑08‑18

---

## Implementación validada

### Correcciones aplicadas
- **Persistencia de expiración por inactividad**  
  En `SesionService.autenticar`, la sesión se marca como `EXPIRADA` y se persiste antes de lanzar `SesionException`, evitando el rollback transaccional y cumpliendo RNF‑0008‑04.

- **Datos de login inválidos → 400**  
  En `ApiExceptionHandler`, el código `sesion.datos-invalidos` se mapea correctamente a `400 Bad Request`, mientras que el resto de errores de sesión mantienen `401 Unauthorized`, cumpliendo RF‑0008‑02 y RNF‑0008‑05.

### Comportamiento final validado
- Login seguro sin exponer credenciales.  
- Rechazo correcto de credenciales inválidas.  
- Validación de datos vacíos con `400`.  
- Protección de rutas autenticadas.  
- Logout idempotente.  
- Expiración por inactividad persistida y no reactivada.  
- Exposición correcta de identidad autenticada.  
- Sesiones múltiples por usuario.  
- Tokens firmados y verificados correctamente.

---

## Pruebas ejecutadas

### SesionAcceptanceTest  
**Resultado:** `6/6 correctos`

Escenarios validados:
- Login correcto  
- Rechazo de credenciales inválidas  
- Rechazo de datos vacíos con `400`  
- Protección de rutas  
- Logout idempotente  
- Expiración por inactividad persistida como `EXPIRADA`

### UsuarioAcceptanceTest  
**Resultado:** `7/7 correctos`

Escenarios validados:
- Recuperación de progreso  
- Actualización parcial  
- Rechazo de progreso inválido  
- Acceso prohibido a progreso de otro usuario  
- Usuario no encontrado  
- Sesión requerida  
- Integridad de datos

---

## Cobertura de requisitos

| Requisito | Estado | Evidencia |
|----------|--------|-----------|
| **RF‑0008‑01** — Login con credenciales válidas | validado | Sesión creada, token emitido, cookie segura |
| **RF‑0008‑02** — Rechazar credenciales no válidas | validado | `401 sesion.credenciales-invalidas` |
| **RF‑0008‑02** — Validación de datos vacíos | validado | `400 sesion.datos-invalidos` |
| **RF‑0008‑03** — Validación de tokens | validado | Token ausente, inválido y válido cubiertos |
| **RF‑0008‑04** — Logout | validado | Idempotente, revoca solo la sesión cerrada |
| **RF‑0008‑05** — Expirar sesiones | validado | Estado persistido como `EXPIRADA` |
| **RF‑0008‑06** — Proteger rutas | validado | Acceso permitido solo con sesión activa |
| **RNF‑0008‑01** — Seguridad de credenciales | validado | No se exponen hashes ni contraseñas |
| **RNF‑0008‑02** — Seguridad de tokens | validado | Cookie `HttpOnly`, `Secure`, `SameSite=Lax` |
| **RNF‑0008‑04** — Persistencia e integridad | validado | Expiración persistida correctamente |
| **RNF‑0008‑05** — Trazabilidad y errores | validado | Códigos de error correctos y consistentes |

---

## Conclusión

CHG‑0008 queda **completamente validado**.  
La implementación cumple todos los requisitos funcionales y no funcionales definidos para el ciclo de sesiones, incluyendo login, autenticación, expiración, logout, protección de rutas y trazabilidad de errores.

El cambio puede marcarse como **aprobado** y la carpeta correspondiente puede moverse a `validado/`.


