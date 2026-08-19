# CHG-0103 - Tareas del panel de usuario

## Estado
`listo`

Estas tareas describen la implementacion prevista y no autorizan cambios de codigo hasta que `requirements.md` sea aprobado.

## Dependencias

- CHG-0102: pantalla de login, store de sesion, token y proteccion de rutas.
- Contrato vigente del endpoint protegido del usuario actual.
- Contrato de membresias y del endpoint backend que crea una sesion de Stripe Checkout.
- Rutas publicas o protegidas de estadisticas, jornadas, fichajes y progresion.
- CHG-0103 no implementa las vistas secundarias; solo registra sus accesos.

## Convenciones de arquitectura

- `ui/`: pagina, componentes del panel, tarjetas de informacion y acciones visuales.
- `core/`: router, servicios HTTP protegidos, cierre de sesion y servicio de Checkout.
- `domain/`: modelos, adaptadores y view-models del usuario y la membresia.
- `styles/`: variables, layout responsive y estados visuales.
- `src/test/acceptance-frontend/`: pruebas E2E con Playwright.

## Tareas

### T-001 - Confirmar contratos y rutas de integracion

- **Requisitos:** RF-001, RF-002, RF-004, RF-005, RF-006, RF-007, RF-008, RF-009, RNF-001, RNF-005.
- **Capa:** Core / Domain.
- **Trabajo:** Confirmar la ruta del panel, el endpoint del usuario actual, el formato de la respuesta, los codigos de sesion invalida, el endpoint de Checkout, la forma de la URL recibida y las rutas de estadisticas, jornadas, fichajes y progresion.
- **Verificacion:** documentar metodo, ruta, cabeceras, payload, respuesta y errores de cada operacion; registrar contratos pendientes antes de implementar.
- **Bloqueo:** no conectar servicios con backend hasta confirmar estos contratos.

### T-002 - Definir los modelos de dominio y view-models

- **Requisitos:** RF-002, RF-003, RF-008, RNF-001, RNF-002, RNF-005.
- **Capa:** Domain.
- **Trabajo:** Crear modelos para usuario autenticado, membresia, estado del jugador, estado de carga/error, acciones del panel y resultado seguro de Checkout. Excluir token, credenciales, datos internos y datos de tarjeta de los view-models visibles.
- **Verificacion:** comprobar que los adaptadores aceptan datos opcionales sin producir `undefined` o `null` visibles y que no exponen campos sensibles.

### T-003 - Implementar el adaptador de respuesta del usuario

- **Requisitos:** RF-002, RF-003, RNF-002, RNF-005.
- **Capa:** Domain.
- **Trabajo:** Transformar la respuesta del endpoint de usuario actual a un view-model estable con nombre, email, membresia, estado del jugador y valores neutros para campos opcionales ausentes.
- **Verificacion:** cubrir respuesta completa, membresia ausente, estado del jugador ausente y campos adicionales desconocidos sin romper el renderizado.

### T-004 - Implementar el servicio protegido del usuario actual

- **Requisitos:** RF-001, RF-002, RF-003, RNF-001, RNF-005.
- **Capa:** Core.
- **Trabajo:** Crear un servicio tipado que consulte el usuario autenticado mediante el cliente protegido de CHG-0102, adjunte el token conforme al contrato y clasifique errores de red, respuesta invalida y sesion no autorizada.
- **Verificacion:** confirmar que utiliza la ruta y metodo definidos, no registra tokens ni datos sensibles y delega la invalidacion de sesion ante `401` o `403` al mecanismo comun.

### T-005 - Implementar el servicio de membresia y Stripe Checkout

- **Requisitos:** RF-008, RNF-001, RNF-005.
- **Capa:** Core.
- **Trabajo:** Crear un servicio que solicite al backend una sesion de Stripe Checkout, consuma la URL de redireccion recibida y traduzca errores recuperables. No integrar campos de tarjeta ni almacenar datos de pago.
- **Verificacion:** comprobar que el servicio envia solo el payload definido por el backend, redirige exclusivamente a la URL recibida y no incluye informacion de tarjeta en peticiones, almacenamiento o mensajes.

### T-006 - Implementar el estado y view-model del panel

- **Requisitos:** RF-001, RF-002, RF-003, RF-008, RF-009, RNF-001, RNF-002, RNF-005.
- **Capa:** Domain / Core.
- **Trabajo:** Orquestar carga inicial, datos del usuario, error recuperable, reintento, estado de Checkout, cierre de sesion y transiciones ante sesion expirada, sin colocar llamadas HTTP en la UI.
- **Verificacion:** validar los estados inicial, cargando, cargado, error, reintentando, Checkout en curso, Checkout fallido y sesion invalidada; impedir operaciones duplicadas mientras una peticion esta en curso.

### T-007 - Registrar la ruta protegida del panel

- **Requisitos:** RF-001, RF-009, RNF-001.
- **Capa:** Core.
- **Trabajo:** Registrar `/panel` como ruta protegida, reutilizar el guard de CHG-0102 y asegurar que el acceso sin token o con token rechazado redirige a `/login`.
- **Verificacion:** comprobar acceso con sesion valida, acceso sin sesion, token expirado y retorno mediante historial despues de cerrar sesion.

### T-008 - Crear la estructura de la pagina del panel

- **Requisitos:** RF-001, RF-002, RF-003, RNF-003, RNF-004.
- **Capa:** UI.
- **Trabajo:** Construir la pagina con encabezado, resumen de usuario, membresia, estado del jugador, estado de carga, error con reintento y accion de cierre de sesion. La UI debe consumir el view-model y no contener logica HTTP o de dominio.
- **Verificacion:** comprobar que cada estado del view-model tiene una representacion coherente y que no se muestran valores tecnicos o sensibles.

### T-009 - Crear componentes de navegacion del juego

- **Requisitos:** RF-004, RF-005, RF-006, RF-007, RNF-003, RNF-004.
- **Capa:** UI / Domain.
- **Trabajo:** Crear controles con nombre accesible para estadisticas reales, jornadas, fichajes y progresion, usando las rutas definidas por sus respectivos cambios y sin implementar sus vistas.
- **Verificacion:** comprobar que los cuatro accesos son visibles para el usuario autenticado, navegan a sus rutas configuradas y no consultan API-Football desde el frontend.

### T-010 - Implementar la tarjeta de membresia y Checkout

- **Requisitos:** RF-008, RNF-001, RNF-003, RNF-004, RNF-005.
- **Capa:** UI / Core.
- **Trabajo:** Mostrar el estado de membresia y ofrecer el control de Checkout solo cuando no exista membresia activa; bloquearlo durante la solicitud y presentar errores recuperables.
- **Verificacion:** comprobar membresia activa, membresia ausente, Checkout en curso, Checkout exitoso y fallo de Checkout sin perder los datos del panel.

### T-011 - Implementar el cierre de sesion

- **Requisitos:** RF-009, RNF-001.
- **Capa:** Core / UI.
- **Trabajo:** Conectar la accion de cierre de sesion con la limpieza del store de CHG-0102 y la navegacion a `/login`. La operacion no debe dejar disponible el panel mediante el historial.
- **Verificacion:** comprobar limpieza de `sessionStorage`, redireccion, imposibilidad de acceder de nuevo a `/panel` y ausencia del token en nuevas solicitudes.

### T-012 - Aplicar accesibilidad y estados anunciables

- **Requisitos:** RF-003, RF-008, RF-009, RNF-003.
- **Capa:** UI / Styles.
- **Trabajo:** Asociar etiquetas y nombres accesibles, establecer foco visible, orden de teclado, regiones anunciables para carga/error y estados disabled con motivo comprensible.
- **Verificacion:** recorrer el panel solo con teclado y comprobar que carga, errores, reintento, Checkout y cierre de sesion son identificables sin depender unicamente del color.

### T-013 - Aplicar estilos responsive del panel

- **Requisitos:** RF-002, RF-004, RF-005, RF-006, RF-007, RF-008, RNF-004.
- **Capa:** Styles.
- **Trabajo:** Definir layout, variables y estados visuales coherentes con el frontend existente para escritorio y movil, evitando solapamientos y desplazamiento horizontal innecesario.
- **Verificacion:** revisar viewport desktop y mobile; confirmar que la informacion, accesos y acciones permanecen visibles, utilizables y con dimensiones estables.

### T-014 - Añadir pruebas de aceptacion frontend

- **Requisitos:** RF-001 a RF-009, RNF-001 a RNF-005.
- **Capa:** Pruebas de aceptacion.
- **Trabajo:** Crear pruebas Playwright con respuestas controladas para acceso autorizado/no autorizado, carga de usuario, membresia activa/inactiva, Checkout, navegacion, errores, reintento, cierre de sesion, teclado y movil.
- **Verificacion:** cada criterio de aceptacion tiene al menos un escenario automatizado y las pruebas comprueban ausencia de tokens, credenciales y datos de tarjeta en la interfaz, URLs y almacenamiento no permitido.

### T-015 - Ejecutar validacion y preparar evidencia

- **Requisitos:** Todos.
- **Capa:** Verificacion SDD.
- **Trabajo:** Ejecutar pruebas de aceptacion, compilacion, lint y comprobaciones responsive/accessibility disponibles; registrar resultados, contratos confirmados y limitaciones en `evidence.md`.
- **Verificacion:** no quedan errores introducidos por CHG-0103, los escenarios pasan en desktop y movil y cada requisito queda trazado a evidencia ejecutada.

## Orden recomendado de ejecucion

1. T-001.
2. T-002 y T-003.
3. T-004 y T-005.
4. T-006 y T-007.
5. T-008.
6. T-009 y T-010.
7. T-011.
8. T-012 y T-013.
9. T-014.
10. T-015.

## Criterio de completitud

CHG-0103 se considerara listo cuando el panel este protegido, muestre correctamente los datos del usuario y la membresia, proporcione los cuatro accesos definidos, ofrezca Checkout solo cuando corresponda, permita cerrar sesion, supere las comprobaciones de accesibilidad y responsive, y tenga evidencia de aceptacion registrada.

## Fuera de alcance tecnico

- Implementacion del panel de administrador de CHG-0104.
- Desarrollo de las vistas de estadisticas, jornadas, fichajes y progresion.
- Cambios en el backend de usuarios, membresias o sesiones.
- Gestion directa de tarjetas, CVV u otros datos de pago.
- Consulta directa a API-Football desde el frontend.
