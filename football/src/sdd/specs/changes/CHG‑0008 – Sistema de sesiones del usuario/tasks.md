# CHG-0008 - Tareas del sistema de sesiones del usuario

## Estado
`pendiente de implementacion`

Estas tareas describen la implementacion prevista y no autorizan cambios de codigo hasta que el plan sea revisado conforme al flujo SDD.

## Dependencias

- CHG-0007: usuarios persistentes con email y contrasena protegida.
- Contrato aprobado de login, logout, validacion de token, expiracion y errores.
- Mecanismo seguro para comparar hashes de contrasena.
- Mecanismo de tokens firmados o equivalente verificable.
- Base de datos y configuracion de persistencia disponibles para sesiones.
- Rutas protegidas que consumiran la identidad autenticada.

## Convenciones de arquitectura

- `domain/`: entidades, estados, reglas y errores puros del dominio de sesiones.
- `application/`: casos de uso, puertos y orquestacion de autenticacion y ciclo de vida.
- `infrastructure/`: adaptadores de persistencia, hash, tokens, reloj y configuracion externa.
- `presentation/`: controladores HTTP, DTOs y traduccion de errores a respuestas.
- `src/test/java/`: pruebas unitarias, de integracion y de aceptacion backend.

## Tareas

### T-001 - Confirmar el contrato de sesiones

- **Requisitos:** RF-001, RF-002, RF-004, RF-005, RF-006, RNF-002, RNF-004.
- **Capa:** Presentacion / Aplicacion / Infraestructura.
- **Trabajo:** Documentar metodo, rutas, JSON de entrada y salida, transporte del token, duracion, expiracion, logout y codigos de error.
- **Verificacion:** registrar ejemplos de solicitudes y respuestas para login correcto, credenciales invalidas, usuario inexistente, token ausente, token invalido, token expirado y logout.
- **Bloqueo:** no conectar controladores ni clientes hasta cerrar los campos obligatorios y los estados HTTP del contrato.

### T-002 - Definir la entidad y los estados de sesion

- **Requisitos:** RF-002, RF-003, RF-005, RF-006, RF-007, RNF-003.
- **Capa:** Domain.
- **Trabajo:** Crear la entidad de sesion con identidad de sesion, usuario, referencia segura del token, fecha de inicio, fecha de expiracion y estado activa/expirada/invalidada.
- **Verificacion:** pruebas unitarias cubren invariantes, fechas coherentes, transiciones validas de estado, expiracion y cierre manual.
- **Restriccion:** la entidad no debe depender de Spring, JPA, JWT ni otros adaptadores externos.

### T-003 - Definir errores y reglas del dominio

- **Requisitos:** RF-001, RF-004, RF-005, RF-006, RNF-001, RNF-004.
- **Capa:** Domain.
- **Trabajo:** Modelar `sesion.credenciales-invalidas`, `usuario.no-existe`, `sesion.no-autenticado`, `sesion.token-invalido` y `sesion.expirada`, junto con las reglas de autenticacion, expiracion y revocacion.
- **Verificacion:** pruebas unitarias comprueban que cada regla produce el error de dominio correcto sin incluir credenciales, hashes ni tokens en sus mensajes.

### T-004 - Definir puertos de aplicacion

- **Requisitos:** RF-001 a RF-007, RNF-003, RNF-005.
- **Capa:** Application.
- **Trabajo:** Definir interfaces para buscar usuarios, comparar contrasenas, crear y consultar sesiones, emitir/verificar tokens y obtener el tiempo actual.
- **Verificacion:** comprobar que los casos de uso dependen de interfaces y que no importan implementaciones JPA, JWT o clases concretas de infraestructura.

### T-005 - Implementar el caso de uso de inicio de sesion

- **Requisitos:** RF-001, RF-002, RF-003, RF-007, RNF-001, RNF-005.
- **Capa:** Application.
- **Trabajo:** Orquestar la búsqueda por email, validación del hash, generación del token, creación de la sesión y devolución del resultado autenticado.
- **Verificacion:** pruebas unitarias cubren login valido, email inexistente, contrasena incorrecta, campos ausentes, token no generado y dos sesiones independientes para el mismo usuario.
- **Seguridad:** no registrar ni devolver la contrasena, su hash o el token en errores de aplicación.

### T-006 - Implementar el caso de uso de validacion de sesion

- **Requisitos:** RF-004, RF-005, RNF-002, RNF-005.
- **Capa:** Application.
- **Trabajo:** Validar la firma o integridad del token, localizar la sesión, comprobar estado y expiracion, y devolver la identidad autenticada.
- **Verificacion:** pruebas unitarias cubren token valido, ausente, mal formado, manipulado, expirado, invalidado y asociado a una sesion inexistente.

### T-007 - Implementar el caso de uso de cierre de sesion

- **Requisitos:** RF-006, RF-007, RNF-005.
- **Capa:** Application.
- **Trabajo:** Invalidar la sesión asociada al token autenticado sin afectar otras sesiones activas del mismo usuario.
- **Verificacion:** pruebas unitarias cubren logout valido, logout repetido, logout sin autenticacion y preservacion de una segunda sesión activa.

### T-008 - Crear el adaptador de comparacion de contrasenas

- **Requisitos:** RF-001, RNF-001, RNF-003.
- **Capa:** Infrastructure.
- **Trabajo:** Conectar el puerto de comparación de contrasenas con el mecanismo de hash aprobado por CHG-0007, sin almacenar contrasenas en texto plano.
- **Verificacion:** pruebas de integracion comprueban que una contrasena valida coincide, una incorrecta no coincide y el adaptador no expone el hash en logs o respuestas.

### T-009 - Crear el adaptador de tokens

- **Requisitos:** RF-002, RF-004, RF-005, RF-007, RNF-002, RNF-003.
- **Capa:** Infrastructure.
- **Trabajo:** Implementar la emisión, firma, verificación y extracción segura de identidad y expiracion mediante JWT o mecanismo equivalente aprobado.
- **Verificacion:** pruebas de integracion cubren tokens únicos, firma válida, token manipulado, expiracion, ausencia de credenciales sensibles y configuración de duración.
- **Seguridad:** la clave o secreto debe proceder de configuración segura y no quedar hardcodeado ni aparecer en logs.

### T-010 - Crear la persistencia de sesiones

- **Requisitos:** RF-003, RF-005, RF-006, RF-007, RNF-003, RNF-005.
- **Capa:** Infrastructure.
- **Trabajo:** Crear entidad JPA, repositorio y adaptador para guardar, consultar, expirar e invalidar sesiones sin acoplar el dominio a JPA.
- **Verificacion:** pruebas de integracion cubren alta, consulta por referencia de token o identificador, cambio de estado, expiracion, sesiones múltiples y consistencia transaccional.
- **Seguridad:** persistir solo la referencia necesaria del token o un identificador seguro según el contrato; no guardar credenciales.

### T-011 - Implementar el controlador HTTP de login

- **Requisitos:** RF-001, RF-002, RF-003, RNF-001, RNF-004.
- **Capa:** Presentation.
- **Trabajo:** Exponer `POST /api/sesiones/login`, recibir email y password en JSON, delegar al caso de uso y devolver `{ "token": "..." }` en caso de éxito.
- **Verificacion:** pruebas MVC comprueban método, ruta, JSON, estado de éxito, token no vacío y ausencia de lógica de autenticación en el controlador.
- **Restriccion:** el controlador no debe acceder directamente a repositorios, comparar hashes ni generar tokens.

### T-012 - Implementar el controlador HTTP de logout y validacion

- **Requisitos:** RF-004, RF-005, RF-006, RF-007, RNF-004, RNF-005.
- **Capa:** Presentation.
- **Trabajo:** Exponer los endpoints definidos para cerrar sesión y, si corresponde al contrato, validar o resolver la identidad en solicitudes protegidas mediante el contexto de seguridad.
- **Verificacion:** pruebas MVC comprueban logout autenticado, ausencia de autenticación, token expirado, token invalidado y preservación de sesiones ajenas.

### T-013 - Implementar el middleware de autenticacion

- **Requisitos:** RF-004, RF-005, RNF-002, RNF-003, RNF-004.
- **Capa:** Presentation / Infrastructure.
- **Trabajo:** Extraer el token de la cabecera definida, delegar su verificación al caso de uso, establecer la identidad autenticada y bloquear solicitudes no válidas antes del controlador protegido.
- **Verificacion:** pruebas de integración cubren token válido, ausente, mal formado, manipulado, expirado y sesión invalidada; las respuestas utilizan los códigos de dominio definidos.
- **Restriccion:** no duplicar reglas de expiración o validación dentro del filtro.

### T-014 - Configurar manejo seguro de errores HTTP

- **Requisitos:** RF-001, RF-004, RF-005, RF-006, RNF-001, RNF-004, RNF-005.
- **Capa:** Presentation.
- **Trabajo:** Traducir errores de dominio y fallos técnicos a estados HTTP y cuerpos JSON estables, sin filtrar detalles internos ni diferenciar información sensible indebidamente.
- **Verificacion:** pruebas MVC cubren credenciales inválidas, usuario inexistente, token inválido, sesión expirada, no autenticado, validación de entrada y almacenamiento no disponible.

### T-015 - Configurar expiracion, reloj y secretos

- **Requisitos:** RF-005, RNF-001, RNF-002, RNF-005.
- **Capa:** Infrastructure.
- **Trabajo:** Externalizar duración de tokens, reloj, claves o secretos y parámetros de almacenamiento; definir valores seguros por entorno.
- **Verificacion:** pruebas con reloj controlado comprueban expiración determinista; validación de configuración impide arrancar con secretos ausentes o inseguros en entornos protegidos.

### T-016 - Añadir pruebas de unidad, integracion y aceptacion

- **Requisitos:** RF-001 a RF-007, RNF-001 a RNF-005.
- **Capa:** Pruebas.
- **Trabajo:** Crear pruebas del dominio, casos de uso, adaptadores, controladores, middleware y flujo completo de login/logout con persistencia de sesiones.
- **Verificacion:** cubrir todos los criterios de aceptación, sesiones múltiples, concurrencia, expiración, errores de infraestructura y ausencia de datos sensibles en respuestas y logs verificables.

### T-017 - Ejecutar validacion y preparar evidencia

- **Requisitos:** Todos.
- **Capa:** Verificación SDD.
- **Trabajo:** Ejecutar pruebas unitarias, integración y aceptación, compilación, análisis estático y comprobaciones de seguridad; documentar resultados y contratos en `evidence.md`.
- **Verificacion:** no quedan errores introducidos por CHG-0008, cada requisito tiene evidencia ejecutada y las limitaciones o dependencias pendientes quedan registradas.

## Orden recomendado de ejecucion

1. T-001.
2. T-002 y T-003.
3. T-004.
4. T-005, T-006 y T-007.
5. T-008, T-009 y T-010.
6. T-011, T-012 y T-013.
7. T-014 y T-015.
8. T-016.
9. T-017.

## Criterio de completitud

CHG-0008 se considerara listo cuando el flujo de login emita sesiones persistentes y tokens seguros, las rutas protegidas validen identidad y expiracion, logout invalide solo la sesion solicitada, los errores mantengan el contrato definido, las credenciales y tokens no se expongan, y toda la cobertura quede registrada en `evidence.md`.

## Fuera de alcance tecnico

- Autenticacion social.
- Recuperacion o cambio de contrasena.
- Autenticacion multifactor.
- Gestion de roles y permisos de CHG-0009.
- Registro de usuarios de CHG-0007.
- Pantallas frontend de CHG-0101 y CHG-0102.
- Integracion con Stripe o pagos.
