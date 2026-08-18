# CHG-0101 - Requisitos de la pantalla de registro

## Estado
`borrador`

## Objetivo
Proporcionar una pantalla de registro accesible y clara que permita crear un usuario mediante el backend de CHG-0007, validar los datos introducidos y dirigir al usuario al inicio de sesión cuando el registro finalice correctamente.

## Alcance y dependencias

Este cambio cubre la interfaz, las validaciones de presentación y la comunicación con el endpoint de registro del backend.

Depende de CHG-0007 para el endpoint y las reglas del dominio Usuario. La pantalla de login, la gestión de sesiones y el panel de usuario quedan fuera de este cambio.

## Requisitos funcionales

### RF-0101 - Mostrar el formulario de registro

La aplicación debe mostrar un formulario con los campos obligatorios `nombre`, `email` y `contraseña`, junto con una acción visible para enviar el registro.

**Criterios de aceptación**

- **CA-0101:** Dado que una persona accede a la ruta de registro, cuando la pantalla termina de cargar, entonces se muestran los tres campos obligatorios, sus etiquetas asociadas y la acción para crear la cuenta.
- **CA-0102:** Dado que el formulario está vacío, cuando la persona intenta enviarlo, entonces el formulario no realiza la llamada al backend y muestra qué campos son obligatorios.
- **CA-0103:** Dado que la persona escribe una contraseña, cuando el campo se muestra, entonces la contraseña permanece oculta visualmente y el control indica de forma accesible qué tipo de dato espera.

### RF-0102 - Validar los datos en tiempo real

La pantalla debe validar el nombre, el email y la contraseña mientras la persona completa el formulario, sin esperar exclusivamente al envío.

**Criterios de aceptación**

- **CA-0101:** Dado un nombre vacío o compuesto únicamente por espacios, cuando el campo pierde el foco o se intenta enviar, entonces se muestra un mensaje de validación asociado al campo.
- **CA-0102:** Dado un email con formato inválido, cuando la persona termina de editarlo, entonces se muestra un mensaje indicando que debe introducir un email válido.
- **CA-0103:** Dado una contraseña vacía o inválida según las reglas configuradas para el registro, cuando la persona la introduce o intenta enviar el formulario, entonces se muestra el requisito incumplido y se bloquea el envío.
- **CA-0104:** Dado que todos los campos cumplen las validaciones de presentación, cuando la persona los completa, entonces el formulario queda habilitado para enviarse.

### RF-0103 - Enviar el registro al backend

La aplicación debe enviar los datos válidos al endpoint de registro de CHG-0007 usando una petición HTTP y un cuerpo estructurado con `nombre`, `email` y `password`.

**Criterios de aceptación**

- **CA-0101:** Dado un formulario válido, cuando la persona pulsa la acción de registro, entonces la aplicación envía una única petición al endpoint configurado de registro con los tres datos introducidos.
- **CA-0102:** Dado que la petición está en curso, cuando la persona pulsa varias veces la acción de registro, entonces la aplicación evita envíos duplicados y muestra un estado de carga.
- **CA-0103:** Dado que el backend responde correctamente, cuando la aplicación recibe la respuesta de registro, entonces no muestra ni persiste la contraseña ni el hash de contraseña en el estado de interfaz.

### RF-0104 - Confirmar el registro y redirigir al login

La aplicación debe informar de que el registro se completó correctamente y redirigir automáticamente a la pantalla de login de CHG-0102.

**Criterios de aceptación**

- **CA-0101:** Dado un registro aceptado por el backend, cuando la aplicación procesa la respuesta exitosa, entonces muestra una confirmación breve y redirige a la ruta de login.
- **CA-0102:** Dado un registro aceptado, cuando la persona llega a la pantalla de login, entonces la aplicación no crea una sesión automáticamente ni expone credenciales.
- **CA-0103:** Dado que la redirección se realiza, cuando la persona utiliza el botón de volver del navegador, entonces no se reenvía automáticamente el formulario de registro.

### RF-0105 - Gestionar errores del dominio Usuario

La pantalla debe traducir los errores conocidos del backend a mensajes comprensibles y asociados al contexto de la operación.

**Criterios de aceptación**

- **CA-0101:** Dado que el backend responde con `usuario.email-duplicado`, cuando la aplicación procesa el error, entonces informa que el email ya está registrado, mantiene los datos no sensibles y no redirige al login.
- **CA-0102:** Dado que el backend responde con un error de validación o formato inválido, cuando la aplicación procesa el error, entonces muestra un mensaje accionable sin presentar detalles técnicos internos.
- **CA-0103:** Dado que el backend responde con un error inesperado o no disponible, cuando la aplicación procesa el error, entonces muestra un mensaje genérico de fallo y permite volver a intentar el registro.
- **CA-0104:** Dado que ocurre un error, cuando la pantalla vuelve a estar disponible para editarse, entonces la contraseña se limpia o permanece protegida y nunca se muestra en mensajes, logs visibles ni URLs.

### RF-0106 - Mantener el estado de interacción del formulario

La pantalla debe preservar una interacción comprensible durante la edición, validación, envío, éxito y error.

**Criterios de aceptación**

- **CA-0101:** Dado que el formulario está enviándose, cuando se muestra el estado de carga, entonces los controles relevantes quedan bloqueados para evitar modificaciones o envíos simultáneos.
- **CA-0102:** Dado que el backend devuelve un error corregible, cuando se muestra el error, entonces el foco puede volver al campo que requiere atención y la persona puede corregir y reenviar.
- **CA-0103:** Dado que la persona corrige un campo inválido, cuando el nuevo valor cumple la regla, entonces desaparece o se actualiza el mensaje de error asociado.

## Requisitos no funcionales

### RNF-0101 - Accesibilidad

La pantalla debe ser operable mediante teclado y compatible con tecnologías de asistencia.

**Criterios de aceptación**

- **CA-0101:** Dado que la persona navega usando únicamente el teclado, cuando recorre el formulario, entonces puede alcanzar los campos, consultar sus errores y enviar el registro en un orden lógico.
- **CA-0102:** Dado que un campo es inválido, cuando se muestra su error, entonces el mensaje está asociado programáticamente al campo y puede ser anunciado por una tecnología de asistencia.
- **CA-0103:** Dado que cambia el estado de envío o aparece un error general, cuando se actualiza la interfaz, entonces el estado se comunica mediante una región accesible sin depender únicamente del color.

### RNF-0102 - Seguridad de credenciales

La interfaz debe tratar la contraseña como dato sensible y no almacenarla de forma persistente en el navegador.

**Criterios de aceptación**

- **CA-0101:** Dado que la persona introduce una contraseña, cuando se envía el formulario, entonces se transmite únicamente por el canal HTTP configurado para la aplicación y no se añade a la URL.
- **CA-0102:** Dado que el registro termina o falla, cuando la aplicación actualiza su estado, entonces no guarda la contraseña en almacenamiento local, session storage, cookies propias ni parámetros de navegación.
- **CA-0103:** Dado que se inspecciona la respuesta del registro, cuando la aplicación procesa los datos, entonces no expone el hash de contraseña en la interfaz ni en el modelo de vista.

### RNF-0103 - Diseño adaptable

La pantalla debe funcionar en las resoluciones soportadas de escritorio y móvil sin solapamientos ni pérdida de acciones.

**Criterios de aceptación**

- **CA-0101:** Dado que la pantalla se visualiza en un dispositivo móvil, cuando se muestra el formulario, entonces todos los campos, mensajes y acciones permanecen visibles y utilizables sin desplazamiento horizontal.
- **CA-0102:** Dado que la pantalla se visualiza en escritorio, cuando se muestra el formulario, entonces la jerarquía visual permite identificar rápidamente los campos, los errores y la acción principal.

### RNF-0104 - Comunicación HTTP controlada

La lógica de comunicación con el backend debe estar encapsulada en un servicio o adaptador del core frontend y no dentro del componente visual.

**Criterios de aceptación**

- **CA-0101:** Dado que la pantalla necesita registrar un usuario, cuando ejecuta la operación, entonces delega la petición HTTP a un servicio tipado del frontend.
- **CA-0102:** Dado que el backend devuelve un error, cuando el servicio lo transforma, entonces entrega al componente un resultado controlado que permite distinguir email duplicado, validación y fallo inesperado.
- **CA-0103:** Dado que se modifica la presentación visual, cuando se mantiene el contrato del servicio, entonces la comunicación con el backend continúa funcionando sin duplicar lógica HTTP en la UI.

### RNF-0105 - Rendimiento y resiliencia

La pantalla debe responder de forma clara durante la comunicación con el backend y recuperarse de fallos de red.

**Criterios de aceptación**

- **CA-0101:** Dado que existe latencia en la petición, cuando la persona espera la respuesta, entonces la interfaz muestra un estado de carga sin aparentar que la acción se ha completado.
- **CA-0102:** Dado que la petición falla por red, cuando se detecta el fallo, entonces la pantalla conserva el formulario corregible y permite reintentar sin recargar toda la aplicación.

## Contrato de integración

- **Operación:** `POST` al endpoint de registro proporcionado por CHG-0007.
- **Petición mínima:** `{ "nombre": "...", "email": "...", "password": "..." }`.
- **Éxito:** respuesta de creación aceptada; la pantalla confirma el registro y navega a login.
- **Errores de dominio:** al menos `usuario.email-duplicado` y errores de validación deben poder mapearse a mensajes de interfaz.
- **Seguridad:** la respuesta no debe requerir que la interfaz almacene contraseñas ni hashes.

## Exclusiones

- Implementación de la pantalla de login.
- Creación, almacenamiento o renovación de sesiones.
- Protección de rutas autenticadas.
- Panel de usuario.
- Integración directa con Stripe o API-Football.

## Trazabilidad

| Requisito | Área de la propuesta |
|---|---|
| RF-0101 | Formulario con nombre, email y contraseña |
| RF-0102 | Validaciones visuales en tiempo real |
| RF-0103 | Llamada al endpoint de registro |
| RF-0104 | Redirección automática al login tras éxito |
| RF-0105 | Manejo de errores del dominio |
| RF-0106 | Feedback visual y estados de interacción |
| RNF-0101 | Accesibilidad y uso claro del formulario |
| RNF-0102 | Seguridad de entrada y credenciales |
| RNF-0103 | Interfaz adaptable |
| RNF-0104 | Arquitectura UI/Core/Domain/Styles |
| RNF-0105 | Resiliencia ante latencia y fallos de red |
