# CHG-0103 - Requisitos del panel de usuario

## Estado
`listo`

Este documento debe ser aprobado antes de iniciar la implementacion.

## Contexto

El usuario autenticado necesita una pantalla principal desde la que pueda consultar su informacion basica, conocer el estado de su membresia y acceder a las funcionalidades principales del juego.

## Requisitos funcionales

### RF-001 - Proteger el acceso al panel

La aplicacion DEBE permitir el acceso al panel unicamente cuando exista una sesion autenticada valida.

**Criterios de aceptacion**

- **Dado** que el usuario tiene una sesion valida, **cuando** accede a la ruta del panel, **entonces** la aplicacion muestra el panel de usuario.
- **Dado** que el usuario no tiene una sesion autenticada, **cuando** intenta acceder a la ruta del panel, **entonces** la aplicacion lo redirige a `/login`.
- **Dado** que el token almacenado ha expirado o es rechazado por el backend, **cuando** el usuario accede al panel o realiza una solicitud protegida, **entonces** la aplicacion invalida la sesion local y lo redirige a `/login`.

### RF-002 - Consultar la informacion del usuario

La aplicacion DEBE consultar y mostrar la informacion basica del usuario autenticado: nombre, email, membresia y estado del jugador cuando este disponible.

**Criterios de aceptacion**

- **Dado** que el usuario tiene una sesion valida y el backend responde con sus datos, **cuando** el panel termina de cargar, **entonces** muestra el nombre y el email del usuario.
- **Dado** que la respuesta incluye una membresia, **cuando** el panel muestra la informacion del usuario, **entonces** muestra el tipo y estado de la membresia de forma comprensible.
- **Dado** que la respuesta incluye el estado del jugador, **cuando** el panel muestra la informacion del usuario, **entonces** muestra ese estado sin modificar su significado de dominio.
- **Dado** que un dato opcional no esta disponible, **cuando** el panel se renderiza, **entonces** muestra un estado neutro y no presenta valores tecnicos como `undefined`, `null` o campos vacios sin contexto.

### RF-003 - Gestionar la carga y los errores del panel

La aplicacion DEBE comunicar el estado de carga y mostrar un mensaje recuperable cuando no pueda obtener la informacion del usuario.

**Criterios de aceptacion**

- **Dado** que la consulta del usuario esta en curso, **cuando** el panel esta esperando la respuesta, **entonces** muestra un estado de carga y no presenta datos como si fueran definitivos.
- **Dado** que el backend no esta disponible, **cuando** la consulta falla por red o por un error inesperado, **entonces** muestra un mensaje comprensible y ofrece una accion para reintentar.
- **Dado** que el backend responde con una sesion no autorizada, **cuando** el error es procesado, **entonces** la aplicacion limpia la sesion y redirige al login.
- **Dado** que el usuario pulsa reintentar, **cuando** la operacion vuelve a comenzar, **entonces** el panel abandona el estado de error y vuelve a mostrar el estado de carga.

### RF-004 - Navegar a estadisticas reales

El panel DEBE ofrecer un acceso a la funcionalidad de estadisticas reales sin consultar API-Football directamente desde el frontend.

**Criterios de aceptacion**

- **Dado** que el usuario esta autenticado, **cuando** consulta las acciones disponibles, **entonces** ve un control identificable para acceder a estadisticas reales.
- **Dado** que el usuario pulsa el acceso a estadisticas reales, **cuando** la navegacion se completa, **entonces** llega a la ruta definida para esa funcionalidad.
- **Dado** que se carga la funcionalidad de estadisticas, **cuando** necesita datos externos, **entonces** el frontend consume un endpoint propio del backend y no API-Football directamente.

### RF-005 - Navegar a jornadas

El panel DEBE ofrecer un acceso a la funcionalidad de jornadas.

**Criterios de aceptacion**

- **Dado** que el usuario esta autenticado, **cuando** consulta las acciones disponibles, **entonces** ve un control identificable para acceder a jornadas.
- **Dado** que el usuario pulsa el acceso a jornadas, **cuando** la navegacion se completa, **entonces** llega a la ruta definida para esa funcionalidad.

### RF-006 - Navegar al mercado de fichajes

El panel DEBE ofrecer un acceso a la funcionalidad de fichajes.

**Criterios de aceptacion**

- **Dado** que el usuario esta autenticado, **cuando** consulta las acciones disponibles, **entonces** ve un control identificable para acceder a fichajes.
- **Dado** que el usuario pulsa el acceso a fichajes, **cuando** la navegacion se completa, **entonces** llega a la ruta definida para esa funcionalidad.

### RF-007 - Navegar a progresion

El panel DEBE ofrecer un acceso a la funcionalidad de progresion del jugador.

**Criterios de aceptacion**

- **Dado** que el usuario esta autenticado, **cuando** consulta las acciones disponibles, **entonces** ve un control identificable para acceder a progresion.
- **Dado** que el usuario pulsa el acceso a progresion, **cuando** la navegacion se completa, **entonces** llega a la ruta definida para esa funcionalidad.

### RF-008 - Ofrecer Checkout para usuarios sin membresia

La aplicacion DEBE mostrar un acceso a Stripe Checkout cuando el usuario no tenga una membresia activa, sin gestionar datos de tarjeta directamente en el frontend.

**Criterios de aceptacion**

- **Dado** que el usuario no tiene una membresia activa, **cuando** el panel muestra su estado de membresia, **entonces** ofrece una accion para iniciar Stripe Checkout.
- **Dado** que el usuario tiene una membresia activa, **cuando** el panel muestra su estado de membresia, **entonces** no presenta una accion de compra como si no tuviera membresia.
- **Dado** que el usuario inicia el pago, **cuando** la aplicacion solicita Checkout, **entonces** delega la creacion de la sesion de pago al backend y redirige a la URL de Checkout recibida.
- **Dado** que se inicia un flujo de pago, **cuando** el usuario interactua con el frontend, **entonces** la aplicacion no solicita, almacena ni procesa numeros de tarjeta, CVV u otros datos sensibles de pago.
- **Dado** que la creacion de Checkout falla, **cuando** el backend devuelve un error, **entonces** el panel muestra un mensaje recuperable y conserva el resto de la informacion del usuario.

### RF-009 - Cerrar la sesion

La aplicacion DEBE ofrecer una accion para cerrar la sesion del usuario.

**Criterios de aceptacion**

- **Dado** que el usuario esta autenticado, **cuando** pulsa cerrar sesion, **entonces** la aplicacion elimina el estado local de sesion y redirige a `/login`.
- **Dado** que el usuario ha cerrado sesion, **cuando** intenta volver al panel usando el historial del navegador, **entonces** la proteccion de ruta impide el acceso sin autenticacion.

## Requisitos no funcionales

### RNF-001 - Seguridad de autenticacion

El panel DEBE utilizar el mecanismo de autenticacion definido por CHG-0102 y el sistema de sesiones, sin exponer tokens ni credenciales en la interfaz, URLs o logs visibles.

**Criterios de aceptacion**

- **Dado** que el panel solicita informacion protegida, **cuando** realiza la peticion, **entonces** utiliza el token conforme al contrato del backend.
- **Dado** que el panel muestra informacion del usuario, **cuando** se renderizan los datos, **entonces** no muestra el token de sesion ni credenciales.
- **Dado** que ocurre un error, **cuando** se muestra el mensaje al usuario, **entonces** no contiene tokens, credenciales ni detalles internos del backend.

### RNF-002 - Separacion arquitectonica

La implementacion DEBE respetar la separacion entre UI, Core, Domain y Styles.

**Criterios de aceptacion**

- **Dado** que la UI necesita datos del usuario, **cuando** solicita o transforma informacion, **entonces** delega la comunicacion HTTP a un servicio del Core y la adaptacion a modelos del Domain.
- **Dado** que cambia la presentacion visual, **cuando** se modifican componentes o estilos, **entonces** no se duplica la logica de autenticacion ni de comunicacion HTTP en la UI.
- **Dado** que se crean modelos de presentacion, **cuando** se adaptan respuestas del backend, **entonces** no se mezclan entidades internas o datos sensibles con el view-model visible.

### RNF-003 - Accesibilidad

El panel DEBE ser operable mediante teclado y comprensible para tecnologias de asistencia.

**Criterios de aceptacion**

- **Dado** que el usuario navega solo con teclado, **cuando** recorre el panel, **entonces** puede alcanzar los accesos, reintentar, iniciar Checkout y cerrar sesion en un orden logico.
- **Dado** que el panel esta cargando o muestra un error, **cuando** cambia el estado de la interfaz, **entonces** el cambio se comunica mediante una region accesible y no depende solo del color.
- **Dado** que un control representa una navegacion o accion, **cuando** se muestra, **entonces** tiene un nombre accesible y un foco visible.

### RNF-004 - Responsive y usabilidad

El panel DEBE funcionar en resoluciones de escritorio y movil sin solapamientos ni desplazamiento horizontal innecesario.

**Criterios de aceptacion**

- **Dado** que el usuario accede desde un dispositivo movil, **cuando** el panel termina de cargar, **entonces** puede consultar la informacion y activar todas las acciones sin desplazamiento horizontal.
- **Dado** que el usuario accede desde escritorio, **cuando** el panel se renderiza, **entonces** la informacion y las acciones mantienen una jerarquia visual clara.
- **Dado** que una accion esta deshabilitada durante una peticion, **cuando** el usuario observa el control, **entonces** el estado deshabilitado y la razon se comunican claramente.

### RNF-005 - Contrato y resiliencia de integracion

La aplicacion DEBE consumir endpoints propios del backend, respetar sus contratos y recuperarse de errores recuperables.

**Criterios de aceptacion**

- **Dado** el contrato vigente de usuarios, membresias y sesiones, **cuando** el panel realiza una solicitud, **entonces** utiliza la ruta, metodo, cabeceras y estructura de datos definidos por el backend.
- **Dado** que el backend devuelve un campo nuevo no utilizado por el panel, **cuando** se adapta la respuesta, **entonces** la pantalla sigue funcionando sin mostrar datos inesperados.
- **Dado** que una solicitud recuperable falla, **cuando** el usuario reintenta, **entonces** la aplicacion puede repetir la operacion sin duplicar acciones ni crear estados inconsistentes.

## Fuera de alcance

- Panel de administrador, correspondiente a CHG-0104.
- Implementacion de las vistas de estadisticas, jornadas, fichajes y progresion.
- Implementacion del backend de usuarios o membresias.
- Implementacion del sistema de sesiones y login, correspondiente a CHG-0008 y CHG-0102.
- Gestion directa de datos de tarjeta o integracion de Stripe Elements para datos de pago.
- Consulta directa a API-Football desde el frontend.

## Dependencias y supuestos

- CHG-0102 proporciona una sesion autenticada y el mecanismo frontend para adjuntar el token.
- El backend expone un endpoint protegido para consultar el usuario actual y endpoints propios para membresia y Checkout.
- El contrato del backend define la representacion de nombre, email, membresia y estado del jugador.
- Las rutas de estadisticas, jornadas, fichajes y progresion estaran disponibles cuando se implementen sus cambios correspondientes.
- Stripe Checkout se inicia mediante una sesion creada por el backend y una URL de redireccion segura.

## Trazabilidad

| Requisito | Objetivo de la propuesta |
|---|---|
| RF-001 | Panel accesible solo a usuarios autenticados |
| RF-002 | Mostrar informacion basica del usuario |
| RF-003 | Comunicar carga, errores y reintento |
| RF-004, RF-005, RF-006, RF-007 | Navegar a las funcionalidades principales |
| RF-008 | Ofrecer Checkout cuando no existe membresia activa |
| RF-009 | Cerrar la sesion y proteger el acceso posterior |
| RNF-001 | Seguridad del token y datos de autenticacion |
| RNF-002 | Separacion UI/Core/Domain/Styles |
| RNF-003 | Accesibilidad del panel y sus acciones |
| RNF-004 | Adaptacion responsive y usabilidad |
| RNF-005 | Contratos backend y recuperacion ante errores |
