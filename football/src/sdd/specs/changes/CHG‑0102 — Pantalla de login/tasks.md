# CHG-0102 - Tareas de la pantalla de login

## Estado
`listo`

Estas tareas describen la implementacion prevista y no autorizan cambios de codigo hasta que `requirements.md` sea aprobado.

## Dependencias

- CHG-0008: sistema de sesiones del usuario.
- Contrato vigente del endpoint de login: metodo HTTP, ruta, payload, respuesta, token, expiracion y errores de dominio.
- CHG-0103: panel de usuario y ruta de destino, necesario para validar la redireccion final.
- Aprobacion de `requirements.md` antes de comenzar la implementacion.

## Convenciones de arquitectura

- `ui/`: pantalla, componentes y estados visuales del formulario.
- `core/`: cliente HTTP, servicio de autenticacion, store de sesion, almacenamiento del token y proteccion de rutas.
- `domain/`: tipos, errores y adaptadores entre el contrato del backend y los view-models de la interfaz.
- `styles/`: estilos globales y especificos de la pantalla.
- `src/test/acceptance-frontend/`: escenarios E2E con Playwright.

## Tareas

### T-001 - Confirmar el contrato de login del backend

- **Requisitos:** RF-003, RF-004, RF-006, RNF-001, RNF-004.
- **Capa:** Core / Domain.
- **Trabajo:** Documentar en el servicio tipado la ruta, metodo, payload de credenciales, estructura de respuesta, ubicacion del token, expiracion y codigos o tipos de error publicados por CHG-0008.
- **Verificacion:** una prueba del servicio comprueba que una solicitud valida respeta el contrato y que las respuestas de exito y error se transforman al modelo de dominio esperado.
- **Bloqueo:** no iniciar la integracion hasta disponer del contrato vigente.

### T-002 - Definir los modelos y errores del dominio de autenticacion

- **Requisitos:** RF-003, RF-004, RF-006, RNF-004.
- **Capa:** Domain.
- **Trabajo:** Crear los tipos para credenciales, respuesta de login, sesion y errores de autenticacion; separar credenciales invalidas, usuario inexistente, respuesta invalida y fallo tecnico.
- **Verificacion:** pruebas unitarias cubren la conversion de cada respuesta conocida y garantizan que un token ausente o invalido no produce una sesion autenticada.

### T-003 - Implementar el servicio HTTP de autenticacion

- **Requisitos:** RF-003, RF-006, RNF-001, RNF-004.
- **Capa:** Core.
- **Trabajo:** Implementar un servicio tipado que envie email y contrasena al endpoint definido por CHG-0008, procese respuestas exitosas y traduzca errores de dominio sin registrar credenciales ni tokens.
- **Verificacion:** pruebas unitarias o de integracion con el cliente HTTP comprueban payload, metodo, ruta, respuesta exitosa, errores conocidos, errores inesperados y ausencia de datos sensibles en logs.

### T-004 - Implementar el almacenamiento y estado global de sesion

- **Requisitos:** RF-003, RF-004, RF-005, RNF-001, RNF-002.
- **Capa:** Core.
- **Trabajo:** Implementar el store de sesion y el adaptador de almacenamiento conforme al mecanismo aprobado por el sistema; exponer estado autenticado, carga, error, token valido, limpieza e invalidacion por expiracion o rechazo del backend.
- **Verificacion:** pruebas unitarias comprueban que el token valido crea la sesion, que el token invalido no la crea, que el cierre o rechazo limpia el estado y que no se registran contrasenas ni tokens.

### T-005 - Conectar el token con las solicitudes protegidas

- **Requisitos:** RF-004, RNF-001, RNF-002.
- **Capa:** Core.
- **Trabajo:** Integrar el store con el cliente HTTP o middleware existente para adjuntar el token segun el contrato de CHG-0008 y procesar respuestas de sesion expirada o no autorizada.
- **Verificacion:** una prueba del cliente protegido confirma que el token se envia con la solicitud y que una respuesta de sesion invalida limpia la sesion y dispara la redireccion al login.

### T-006 - Implementar la proteccion de rutas

- **Requisitos:** RNF-002, RF-005.
- **Capa:** Core.
- **Trabajo:** Añadir el guard o middleware de rutas que permita el panel de usuario solo con una sesion valida y redirija al login cuando no haya sesion, el token haya expirado o el backend lo rechace.
- **Verificacion:** pruebas de router cubren acceso sin token, acceso con token valido y acceso con token expirado o invalidado.

### T-007 - Crear el view-model del formulario de login

- **Requisitos:** RF-001, RF-002, RF-003, RF-005, RF-006, RNF-003.
- **Capa:** Domain.
- **Trabajo:** Implementar el view-model que gestione valores del formulario, validacion local, estado de envio, errores de campo, error de autenticacion, reintento y redireccion tras login exitoso, sin contener marcado ni estilos.
- **Verificacion:** pruebas unitarias cubren email vacio o invalido, contrasena vacia, envio valido, prevencion de envios duplicados, errores del dominio, error tecnico, limpieza del error al reintentar y exito con redireccion.

### T-008 - Construir la pantalla y componentes de UI

- **Requisitos:** RF-001, RF-002, RF-003, RF-006, RNF-003.
- **Capa:** UI.
- **Trabajo:** Crear la pagina de login y sus componentes reutilizables para email, contrasena, boton de envio, estado de carga, errores de campo y error general; conectar la vista exclusivamente con el view-model.
- **Verificacion:** prueba de componente o E2E confirma que la pantalla muestra ambos campos, oculta la contrasena, presenta el boton, muestra estados de carga y permite reintentar tras un error.

### T-009 - Aplicar estilos y estados visuales accesibles

- **Requisitos:** RF-001, RF-006, RNF-003.
- **Capa:** Styles / UI.
- **Trabajo:** Añadir estilos coherentes con el sistema visual existente para foco visible, campos invalidos, mensajes de error, carga, deshabilitado y diseño responsive, sin cambiar la logica de autenticacion.
- **Verificacion:** comprobacion visual en viewport desktop y movil, y prueba de accesibilidad valida etiquetas, asociaciones de errores, foco y comunicacion del estado de carga.

### T-010 - Integrar la ruta de login y la redireccion al panel

- **Requisitos:** RF-001, RF-005, RNF-002.
- **Capa:** Core / UI.
- **Trabajo:** Registrar la ruta publica de login, definir la ruta protegida del panel de usuario y conectar el resultado exitoso del login con la navegacion al panel.
- **Verificacion:** prueba de router o E2E confirma que el login es accesible sin sesion, que el panel requiere autenticacion y que un login correcto navega al destino definido por CHG-0103.

### T-011 - Añadir pruebas de aceptacion frontend

- **Requisitos:** RF-001 a RF-006, RNF-001 a RNF-004.
- **Capa:** Pruebas de aceptacion.
- **Trabajo:** Crear o ampliar la suite Playwright para cubrir formulario, validacion local sin llamada, autenticacion exitosa, almacenamiento de token, redireccion, credenciales invalidas, usuario inexistente, error tecnico, reintento, acceso protegido y accesibilidad basica.
- **Verificacion:** todos los escenarios automatizados pasan usando respuestas controladas del endpoint de sesiones; las pruebas inspeccionan que no se expongan credenciales o tokens en la interfaz ni en los mensajes mostrados.

### T-012 - Ejecutar validacion completa y preparar evidencia

- **Requisitos:** Todos.
- **Capa:** Verificacion SDD.
- **Trabajo:** Ejecutar pruebas unitarias, de integracion y de aceptacion frontend; revisar errores de compilacion y lint; registrar resultados y desviaciones en `evidence.md`.
- **Verificacion:** la suite relevante pasa, no quedan errores introducidos por CHG-0102 y cada requisito tiene al menos un escenario de aceptacion ejecutado.

## Orden recomendado de ejecucion

1. T-001 y T-002.
2. T-003 y T-004.
3. T-005 y T-006.
4. T-007.
5. T-008 y T-009.
6. T-010.
7. T-011.
8. T-012.

## Criterio de completitud

CHG-0102 se considerara listo cuando todas las tareas verificables esten completadas, los escenarios de aceptacion pasen, el token se gestione conforme al contrato de CHG-0008, no se expongan datos sensibles y los resultados queden registrados en `evidence.md`.

## Fuera de alcance tecnico

- Cambios en el backend o en el contrato de CHG-0008.
- Pantalla y flujo de registro de CHG-0101.
- Implementacion del panel de usuario de CHG-0103.
- Panel de administrador de CHG-0104.
- Recuperacion o cambio de contrasena.
- Integracion con Stripe o manejo de datos de tarjeta.
