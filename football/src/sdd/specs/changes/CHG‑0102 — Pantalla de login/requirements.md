# CHG-0102 - Requisitos de la pantalla de login

## Estado
`listo`

Este documento debe ser aprobado antes de iniciar la implementacion.

## Contexto

El sistema de sesiones del backend ya proporciona el mecanismo de autenticacion. La pantalla de login debe permitir que un usuario existente introduzca sus credenciales, reciba el token de sesion y acceda al panel de usuario.

## Requisitos funcionales

### RF-001 - Presentar el formulario de login

La aplicacion DEBE mostrar una pantalla de inicio de sesion con los campos obligatorios `email` y `contrasena`, junto con una accion para enviar el formulario.

**Criterios de aceptacion**

- **Dado** que un visitante accede a la pantalla de login, **cuando** la pantalla termina de cargar, **entonces** se muestran los campos de email y contrasena y la accion de iniciar sesion.
- **Dado** que el usuario introduce una contrasena, **cuando** escribe en el campo correspondiente, **entonces** el valor se presenta como contenido oculto.
- **Dado** que el formulario esta visible, **cuando** el usuario lo consulta, **entonces** cada campo identifica claramente el dato que debe introducir.

### RF-002 - Validar los datos de entrada

La aplicacion DEBE validar que el email tenga un formato valido y que la contrasena no este vacia antes de enviar la solicitud al backend.

**Criterios de aceptacion**

- **Dado** que el email esta vacio o tiene un formato invalido, **cuando** el usuario intenta iniciar sesion, **entonces** se muestra un error de validacion y no se realiza ninguna llamada de login.
- **Dado** que la contrasena esta vacia, **cuando** el usuario intenta iniciar sesion, **entonces** se muestra un error de validacion y no se realiza ninguna llamada de login.
- **Dado** que el email y la contrasena son validos, **cuando** el usuario envia el formulario, **entonces** la solicitud de login puede enviarse al endpoint del sistema de sesiones.

### RF-003 - Autenticar al usuario

La aplicacion DEBE enviar las credenciales validas al endpoint de login del sistema de sesiones mediante una solicitud HTTP y procesar la respuesta de autenticacion.

**Criterios de aceptacion**

- **Dado** que los datos superan la validacion local, **cuando** el usuario envia el formulario, **entonces** se realiza una solicitud al endpoint de login con email y contrasena.
- **Dado** que el backend responde con autenticacion exitosa y un token de sesion, **cuando** la respuesta es procesada, **entonces** el usuario queda identificado como autenticado.
- **Dado** que hay una solicitud de login en curso, **cuando** el usuario observa el formulario, **entonces** la aplicacion evita envios duplicados hasta recibir la respuesta.

### RF-004 - Almacenar el token de sesion

La aplicacion DEBE almacenar el token recibido usando el mecanismo de almacenamiento definido por el sistema de sesiones y DEBE reutilizarlo para las rutas o solicitudes protegidas.

**Criterios de aceptacion**

- **Dado** que el backend devuelve un token valido, **cuando** el login termina correctamente, **entonces** el token se almacena sin exponerlo en la interfaz.
- **Dado** que existe un token de sesion almacenado, **cuando** la aplicacion realiza una solicitud protegida, **entonces** la solicitud utiliza el token conforme al contrato del backend.
- **Dado** que la respuesta de login no contiene un token valido, **cuando** se procesa la respuesta, **entonces** la aplicacion no establece una sesion autenticada ni redirige al usuario.

### RF-005 - Redirigir al panel de usuario

La aplicacion DEBE redirigir al panel de usuario despues de completar correctamente el login y almacenar el token.

**Criterios de aceptacion**

- **Dado** que el backend confirma las credenciales y devuelve un token valido, **cuando** el token queda almacenado, **entonces** el usuario es redirigido al panel de usuario.
- **Dado** que el login falla, **cuando** la aplicacion procesa la respuesta, **entonces** el usuario permanece en la pantalla de login.

### RF-006 - Gestionar errores de autenticacion

La aplicacion DEBE mostrar mensajes comprensibles para credenciales invalidas y usuario inexistente, sin revelar informacion sensible adicional.

**Criterios de aceptacion**

- **Dado** que el backend rechaza las credenciales, **cuando** responde con el error de credenciales invalidas, **entonces** se muestra un mensaje de autenticacion fallida y no se crea la sesion.
- **Dado** que el backend indica que el usuario no existe, **cuando** responde con ese error de dominio, **entonces** se muestra un mensaje de autenticacion fallida y no se crea la sesion.
- **Dado** que el backend responde con un error no previsto o no disponible, **cuando** la aplicacion procesa la respuesta, **entonces** se muestra un mensaje generico y el formulario sigue disponible para reintentar.
- **Dado** que se muestra un error de autenticacion, **cuando** el usuario introduce nuevos datos o reintenta, **entonces** el estado de error anterior no impide un nuevo intento.

## Requisitos no funcionales

### RNF-001 - Seguridad de credenciales y token

La aplicacion DEBE transmitir las credenciales usando el canal seguro configurado por el sistema y NO DEBE registrar contrasenas ni tokens en logs, mensajes de error o elementos visibles de la interfaz.

**Criterios de aceptacion**

- **Dado** que el usuario envia sus credenciales, **cuando** la aplicacion realiza la solicitud, **entonces** las credenciales se transmiten mediante el mecanismo seguro definido para la comunicacion con el backend.
- **Dado** que se produce un error de login, **cuando** se muestra el resultado, **entonces** el mensaje no contiene la contrasena ni el token.
- **Dado** que el login finaliza correctamente, **cuando** la aplicacion actualiza el estado de sesion, **entonces** no se escribe la contrasena ni el token en logs de depuracion.

### RNF-002 - Proteccion de rutas

La aplicacion DEBE impedir el acceso al panel de usuario cuando no existe una sesion autenticada valida y DEBE enviar al usuario a la pantalla de login.

**Criterios de aceptacion**

- **Dado** que no existe un token de sesion valido, **cuando** el usuario intenta acceder al panel de usuario, **entonces** se le redirige a la pantalla de login.
- **Dado** que el token almacenado ha expirado o es rechazado por el backend, **cuando** el usuario intenta acceder a una funcionalidad protegida, **entonces** la aplicacion invalida la sesion local y redirige al login.

### RNF-003 - Accesibilidad y usabilidad

La pantalla DEBE ser operable mediante teclado, asociar etiquetas con sus campos y comunicar los errores de validacion de forma comprensible.

**Criterios de aceptacion**

- **Dado** que el usuario utiliza solo el teclado, **cuando** navega por la pantalla, **entonces** puede alcanzar los campos y enviar el formulario mediante controles con foco visible.
- **Dado** que un campo no supera la validacion, **cuando** se muestra el error, **entonces** el error se asocia al campo correspondiente y puede ser comprendido por tecnologias de asistencia.
- **Dado** que el login esta en curso, **cuando** se desactiva temporalmente la accion de envio, **entonces** el estado de carga se comunica al usuario.

### RNF-004 - Compatibilidad con el contrato del backend

La integracion DEBE consumir exclusivamente el endpoint de login proporcionado por el sistema de sesiones y respetar su formato de solicitud, respuesta y errores de dominio.

**Criterios de aceptacion**

- **Dado** el contrato vigente del sistema de sesiones, **cuando** la pantalla envia una solicitud, **entonces** utiliza el metodo, ruta y estructura de datos definidos por dicho contrato.
- **Dado** que el backend devuelve un error de dominio conocido, **cuando** la aplicacion lo adapta a la interfaz, **entonces** conserva el significado del error sin inventar estados de autenticacion.

## Fuera de alcance

- Registro de usuarios, correspondiente a CHG-0101.
- Desarrollo del panel de usuario, correspondiente a CHG-0103.
- Desarrollo del panel de administrador, correspondiente a CHG-0104.
- Cambios en la implementacion del backend de sesiones.
- Recuperacion o cambio de contrasena.
- Gestion de datos de pago o integracion con Stripe.

## Dependencias y supuestos

- CHG-0008 esta implementado y expone un endpoint de login operativo.
- El contrato del backend define el formato del token, su duracion, el mecanismo de almacenamiento esperado y los errores de autenticacion.
- El panel de usuario y su ruta estaran disponibles cuando se integre CHG-0103.

## Trazabilidad

| Requisito | Objetivo de la propuesta |
|---|---|
| RF-001, RF-002 | Introducir y validar credenciales |
| RF-003 | Llamar al endpoint de login |
| RF-004, RNF-001 | Almacenar el token de forma segura |
| RF-005, RNF-002 | Acceder de forma protegida al panel de usuario |
| RF-006 | Manejar errores de autenticacion |
| RNF-003, RNF-004 | Calidad de la experiencia e integracion |
