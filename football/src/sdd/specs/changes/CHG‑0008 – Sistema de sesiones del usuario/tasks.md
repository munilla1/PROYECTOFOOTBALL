# CHG-0008 - Tareas técnicas del sistema de sesiones del usuario

## Estado
`pendiente de aprobación de requirements.md`

## Regla de ejecución

Estas tareas no deben implementarse hasta que `requirements.md` sea aprobado. La implementación debe respetar la separación entre dominio, aplicación, infraestructura y presentación definida por el Backend Agent. Antes de desarrollar la autenticación debe quedar documentada la estrategia de transporte, firma, expiración, revocación y persistencia de tokens.

## Tareas

### T-0801 - Confirmar el contrato de sesión y decisiones de seguridad

- **Capa:** análisis / arquitectura
- **Requisitos:** RF-0008-01, RF-0008-03, RF-0008-04, RF-0008-05, RF-0008-06, RNF-0008-02, RNF-0008-04, RNF-0008-05
- **Descripción:** Definir el contrato HTTP de login, logout y rutas protegidas, junto con la estrategia de token, transporte, firma, claims, expiración, inactividad, revocación y almacenamiento de sesiones.
- **Verificación:** Documentar método, rutas, cuerpos, respuestas, códigos de error, política de múltiples sesiones, atributos de cookie si aplica y configuración de tiempos. No continuar si el mecanismo elegido permite exponer tokens o impide la revocación requerida.

### T-0802 - Definir entidades y objetos de valor del dominio Sesiones

- **Capa:** dominio
- **Requisitos:** RF-0008-01, RF-0008-03, RF-0008-04, RF-0008-05, RNF-0008-04
- **Descripción:** Crear la entidad o agregado `SesionUsuario` y los objetos de valor necesarios para identificador de sesión, identificador de usuario, estado, fechas, expiración, última actividad y referencia segura del token.
- **Verificación:** Comprobar mediante reglas puras que una sesión válida requiere usuario, fechas coherentes, estado permitido y referencia no vacía; que una sesión cerrada o expirada no puede volver a estado activo sin una nueva creación.

### T-0803 - Implementar reglas de dominio de sesión y errores

- **Capa:** dominio
- **Requisitos:** RF-0008-02, RF-0008-03, RF-0008-04, RF-0008-05, RNF-0008-05, RNF-0008-06
- **Descripción:** Implementar las reglas puras para sesión activa, cierre idempotente, expiración por fecha o inactividad, rechazo de sesión invalidada y clasificación de errores `sesion.token-invalido`, `sesion.expirada`, `sesion.credenciales-invalidas` y `sesion.no-autenticado`.
- **Verificación:** Verificar estados límite, doble logout, token expirado, sesión invalidada, actividad dentro de ventana y concurrencia sin reactivar sesiones cerradas.

### T-0804 - Definir interfaces de repositorios de sesiones

- **Capa:** dominio / aplicación
- **Requisitos:** RF-0008-01, RF-0008-03, RF-0008-04, RF-0008-05, RNF-0008-03, RNF-0008-04
- **Descripción:** Definir interfaces para crear sesiones, buscar por identificador o referencia de token, actualizar actividad y estado, invalidar sesiones y consultar sesiones activas del usuario sin exponer tecnología de persistencia.
- **Verificación:** Comprobar que los casos de uso dependen únicamente de abstracciones y que las operaciones distinguen sesión inexistente, activa, expirada y cerrada sin devolver estados parciales.

### T-0805 - Diseñar el modelo persistente y restricciones de sesiones

- **Capa:** infraestructura
- **Requisitos:** RF-0008-01, RF-0008-04, RF-0008-05, RNF-0008-04, RNF-0008-06
- **Descripción:** Crear el modelo JPA o migración equivalente para sesiones, con identificador estable, usuario asociado, referencia segura del token, fechas, estado, última actividad y restricciones de integridad e índices necesarios.
- **Verificación:** Ejecutar el esquema en una base de datos de prueba y comprobar asociación válida con usuario, unicidad de referencias, estados permitidos, fechas obligatorias y ausencia de registros incompletos o duplicados.

### T-0806 - Implementar el adaptador de persistencia de sesiones

- **Capa:** infraestructura
- **Requisitos:** RF-0008-03, RF-0008-04, RF-0008-05, RNF-0008-03, RNF-0008-04, RNF-0008-06
- **Descripción:** Implementar el repositorio concreto que traduzca entre el dominio y el modelo persistente, gestione búsquedas, actualizaciones atómicas e invalidaciones, y convierta fallos de almacenamiento en errores controlados.
- **Verificación:** Comprobar creación, consulta, actualización de actividad, cierre, expiración, persistencia tras reinicio y comportamiento ante conflicto o fallo de almacenamiento sin comunicar éxito parcial.

### T-0807 - Implementar el servicio de verificación de credenciales

- **Capa:** aplicación / infraestructura
- **Requisitos:** RF-0008-01, RF-0008-02, RNF-0008-01, RNF-0008-05
- **Descripción:** Integrar el repositorio de usuarios de CHG-0007 y el `PasswordHasher` existente para buscar por email y comparar la contraseña recibida únicamente contra el hash persistido.
- **Verificación:** Comprobar credencial correcta, contraseña incorrecta, email inexistente, datos inválidos y respuesta uniforme sin revelar si el usuario existe ni exponer contraseña o hash en logs y respuestas.

### T-0808 - Implementar el adaptador de firma y validación de tokens

- **Capa:** infraestructura
- **Requisitos:** RF-0008-03, RF-0008-05, RNF-0008-02, RNF-0008-03, RNF-0008-05
- **Descripción:** Implementar el adaptador JWT o equivalente aprobado para generar y validar tokens firmados, con claims mínimas de identidad, emisión y expiración, sin colocar reglas HTTP ni de negocio en el adaptador.
- **Verificación:** Comprobar unicidad y firma, token alterado, algoritmo o clave inválidos, expiración, claims ausentes, ausencia de secretos en respuestas y que la validación devuelva solo una identidad controlada.

### T-0809 - Implementar el caso de uso de inicio de sesión

- **Capa:** aplicación
- **Requisitos:** RF-0008-01, RF-0008-02, RF-0008-03, RNF-0008-01, RNF-0008-02, RNF-0008-04, RNF-0008-06
- **Descripción:** Orquestar validación de entrada, comprobación de credenciales, creación de sesión, generación de token, persistencia atómica y construcción de una respuesta segura.
- **Verificación:** Comprobar login válido, contraseña incorrecta, usuario inexistente, entrada inválida, múltiples sesiones, fallo de repositorio o firma y ausencia de sesión o token parcial cuando la operación falla.

### T-0810 - Implementar el caso de uso de cierre de sesión

- **Capa:** aplicación
- **Requisitos:** RF-0008-04, RF-0008-03, RNF-0008-04, RNF-0008-06
- **Descripción:** Orquestar la resolución de la sesión autenticada, su invalidación y la limpieza del mecanismo de transporte definido, manteniendo el cierre idempotente.
- **Verificación:** Comprobar logout válido, sesión ya cerrada, token inválido, conservación de otras sesiones del usuario y rechazo posterior del token cerrado en rutas protegidas.

### T-0811 - Implementar el caso de uso de validación y expiración

- **Capa:** aplicación
- **Requisitos:** RF-0008-03, RF-0008-05, RF-0008-06, RNF-0008-04, RNF-0008-05, RNF-0008-06
- **Descripción:** Validar token y sesión persistida, aplicar expiración por fecha o inactividad, actualizar última actividad según la política aprobada y devolver únicamente la identidad autenticada.
- **Verificación:** Comprobar sesión activa, token manipulado, sesión cerrada, expiración temporal, inactividad, concurrencia y que una sesión expirada no pueda reactivarse automáticamente.

### T-0812 - Implementar el middleware de autenticación

- **Capa:** presentación / infraestructura
- **Requisitos:** RF-0008-03, RF-0008-06, RNF-0008-03, RNF-0008-05, RNF-0008-06
- **Descripción:** Crear un filtro, interceptor o mecanismo equivalente que extraiga el token del transporte aprobado, delegue su validación al caso de uso y coloque la identidad autenticada en el contexto de la petición.
- **Verificación:** Comprobar ruta pública sin token, ruta protegida sin token, token mal formado, token expirado, token revocado y token válido; confirmar que el controlador no interpreta directamente JWT ni ejecuta el caso de uso con identidad no validada.

### T-0813 - Implementar los controladores HTTP de sesiones

- **Capa:** presentación
- **Requisitos:** RF-0008-01, RF-0008-02, RF-0008-04, RF-0008-06, RNF-0008-03, RNF-0008-05
- **Descripción:** Exponer `POST /api/sesiones/login` y `POST /api/sesiones/logout`, mapear entrada y salida, aplicar códigos HTTP y traducir errores de dominio a respuestas estables sin filtrar detalles internos.
- **Verificación:** Comprobar contratos de éxito, `401` para credenciales inválidas, `400` para datos inválidos, logout idempotente, ausencia de credenciales en respuestas y compatibilidad con la identidad entregada por el middleware.

### T-0814 - Configurar transporte seguro y ciclo de vida

- **Capa:** infraestructura / presentación
- **Requisitos:** RF-0008-04, RF-0008-05, RF-0008-06, RNF-0008-02, RNF-0008-04
- **Descripción:** Configurar el transporte de sesión elegido, las claves o secretos desde configuración segura, expiración, inactividad, invalidación y atributos de cookie `HttpOnly`, `Secure` y `SameSite` cuando corresponda.
- **Verificación:** Revisar configuración por entorno, comprobar que los secretos no están en el repositorio, que producción exige HTTPS, que logout elimina o invalida el transporte y que la expiración se aplica de forma uniforme.

### T-0815 - Aplicar control de observabilidad y privacidad

- **Capa:** aplicación / infraestructura / presentación
- **Requisitos:** RF-0008-02, RNF-0008-01, RNF-0008-02, RNF-0008-05
- **Descripción:** Revisar logs, excepciones, respuestas y métricas para impedir la exposición de contraseñas, hashes, tokens completos, SQL, stack traces o configuración interna.
- **Verificación:** Ejecutar flujos de éxito y error inspeccionando respuestas y logs; confirmar redacción o exclusión de secretos y mensajes genéricos para errores de infraestructura.

### T-0816 - Implementar pruebas unitarias y de integración

- **Capa:** pruebas unitarias / integración
- **Requisitos:** Todos los RF y RNF aplicables
- **Descripción:** Crear pruebas para reglas de dominio, casos de uso, repositorio, firma de tokens, configuración de expiración, mapeo de errores y middleware con base de datos de prueba.
- **Verificación:** Cubrir credenciales válidas e inválidas, tokens alterados o expirados, logout, revocación, múltiples sesiones, rutas públicas/protegidas, fallos de persistencia, concurrencia y ausencia de secretos.

### T-0817 - Implementar pruebas de aceptación del sistema de sesiones

- **Capa:** pruebas de aceptación
- **Requisitos:** Todos los RF y RNF aplicables
- **Descripción:** Implementar escenarios de aceptación HTTP del ciclo completo de sesiones, desde login hasta logout, incluyendo validación de rutas protegidas y expiración.
- **Verificación:** Ejecutar escenarios de iniciar sesión correctamente, credenciales inválidas, usuario inexistente, acceso sin sesión, token válido, token manipulado, token expirado, logout, revocación y persistencia según el mecanismo elegido; registrar cada resultado en `evidence.md`.

### T-0818 - Verificar regresión, seguridad y contrato frontend

- **Capa:** integración / calidad
- **Requisitos:** RF-0008-01, RF-0008-03, RF-0008-04, RF-0008-05, RF-0008-06, RNF-0008-01, RNF-0008-02, RNF-0008-03, RNF-0008-04, RNF-0008-05, RNF-0008-06
- **Descripción:** Ejecutar la suite existente de CHG-0007, comprobar que los endpoints protegidos siguen recibiendo `Principal` o identidad equivalente, validar el contrato consumible por CHG-0102 y revisar configuración de secretos y cookies.
- **Verificación:** Confirmar compilación, pruebas completas sin regresiones, contrato HTTP documentado, respuestas seguras, rutas protegidas operativas y evidencia actualizada con pendientes explícitos.

## Orden recomendado

1. T-0801 - Confirmar el contrato de sesión y decisiones de seguridad.
2. T-0802 - Definir entidades y objetos de valor del dominio Sesiones.
3. T-0803 - Implementar reglas de dominio y errores.
4. T-0804 - Definir interfaces de repositorios.
5. T-0805 - Diseñar el modelo persistente y restricciones.
6. T-0806 - Implementar el adaptador de persistencia.
7. T-0807 - Implementar el servicio de verificación de credenciales.
8. T-0808 - Implementar el adaptador de firma y validación de tokens.
9. T-0809 - Implementar el caso de uso de inicio de sesión.
10. T-0810 - Implementar el caso de uso de cierre de sesión.
11. T-0811 - Implementar el caso de uso de validación y expiración.
12. T-0812 - Implementar el middleware de autenticación.
13. T-0813 - Implementar los controladores HTTP de sesiones.
14. T-0814 - Configurar transporte seguro y ciclo de vida.
15. T-0815 - Aplicar control de observabilidad y privacidad.
16. T-0816 - Implementar pruebas unitarias y de integración.
17. T-0817 - Implementar pruebas de aceptación.
18. T-0818 - Verificar regresión, seguridad y contrato frontend.

## Dependencias externas

- **CHG-0007:** entidad `Usuario`, repositorio de usuarios, email persistido y `PasswordHasher`/hash BCrypt.
- **CHG-0102:** consumo del endpoint de login y del mecanismo de sesión.
- **CHG-0103:** rutas protegidas y destino posterior al login.
- **Configuración de despliegue:** claves, secretos, HTTPS, tiempos de expiración y atributos de cookie.

## Trazabilidad resumida

| Requisito | Tareas |
|---|---|
| RF-0008-01 | T-0801, T-0802, T-0804, T-0805, T-0806, T-0807, T-0808, T-0809, T-0813, T-0816, T-0817, T-0818 |
| RF-0008-02 | T-0801, T-0803, T-0807, T-0809, T-0813, T-0815, T-0816, T-0817, T-0818 |
| RF-0008-03 | T-0801, T-0802, T-0803, T-0804, T-0806, T-0808, T-0809, T-0811, T-0812, T-0816, T-0817, T-0818 |
| RF-0008-04 | T-0801, T-0802, T-0803, T-0804, T-0805, T-0806, T-0810, T-0812, T-0813, T-0814, T-0816, T-0817, T-0818 |
| RF-0008-05 | T-0801, T-0802, T-0803, T-0804, T-0805, T-0806, T-0808, T-0811, T-0812, T-0814, T-0816, T-0817, T-0818 |
| RF-0008-06 | T-0801, T-0803, T-0804, T-0811, T-0812, T-0813, T-0814, T-0816, T-0817, T-0818 |
| RNF-0008-01 | T-0807, T-0809, T-0813, T-0815, T-0816, T-0817, T-0818 |
| RNF-0008-02 | T-0801, T-0808, T-0809, T-0811, T-0814, T-0815, T-0816, T-0817, T-0818 |
| RNF-0008-03 | T-0802, T-0803, T-0804, T-0806, T-0808, T-0809, T-0810, T-0811, T-0812, T-0813, T-0816, T-0818 |
| RNF-0008-04 | T-0801, T-0802, T-0804, T-0805, T-0806, T-0809, T-0810, T-0811, T-0814, T-0816, T-0817, T-0818 |
| RNF-0008-05 | T-0801, T-0803, T-0807, T-0808, T-0811, T-0812, T-0813, T-0815, T-0816, T-0817, T-0818 |
| RNF-0008-06 | T-0801, T-0803, T-0805, T-0806, T-0809, T-0810, T-0811, T-0812, T-0814, T-0816, T-0817, T-0818 |
