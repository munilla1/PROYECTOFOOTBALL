# CHG-0008 - Requisitos del sistema de sesiones del usuario

## Estado
`listo`

Este documento debe ser aprobado antes de iniciar la implementacion.

## Contexto

El sistema necesita identificar a los usuarios registrados y proteger el acceso a sus datos y funcionalidades. Actualmente no existe un flujo de inicio de sesion, cierre de sesion, emision de tokens ni validacion de identidad para las rutas protegidas.

## Requisitos funcionales

### RF-001 - Iniciar sesion con credenciales

La aplicacion DEBE permitir que un usuario registrado inicie sesion mediante su email y contrasena.

**Criterios de aceptacion**

- **Dado** que existe un usuario registrado con un email y una contrasena validos, **cuando** envia sus credenciales al endpoint de login, **entonces** el sistema valida la identidad y crea una sesion activa.
- **Dado** que la solicitud de login contiene email o contrasena ausentes, **cuando** el sistema la procesa, **entonces** rechaza la solicitud sin crear una sesion.
- **Dado** que la solicitud de login contiene un email con formato invalido, **cuando** el sistema la procesa, **entonces** rechaza la solicitud con un error de validacion.
- **Dado** que el email y la contrasena no coinciden con un usuario registrado, **cuando** el sistema procesa el login, **entonces** devuelve el error de dominio `sesion.credenciales-invalidas` sin revelar informacion adicional.
- **Dado** que el email no corresponde a ningun usuario registrado, **cuando** el sistema procesa el login, **entonces** devuelve el error de dominio `usuario.no-existe` conforme al contrato publicado.

### RF-002 - Emitir un token de sesion

El sistema DEBE generar un token de sesion unico y firmado, o un mecanismo equivalente seguro, cuando el login sea valido.

**Criterios de aceptacion**

- **Dado** que las credenciales son validas, **cuando** el sistema crea la sesion, **entonces** devuelve una respuesta con la propiedad `token` no vacia.
- **Dado** que el sistema emite un token, **cuando** se inspecciona su contenido o metadatos, **entonces** permite identificar al usuario y su expiracion sin incluir la contrasena ni su hash.
- **Dado** que dos sesiones validas se crean para el mismo usuario, **cuando** se comparan sus tokens, **entonces** cada token es único.
- **Dado** que el sistema intenta crear una sesion sin credenciales validas, **cuando** procesa la operacion, **entonces** no emite ningun token.

### RF-003 - Persistir y registrar la sesion

El sistema DEBE registrar cada sesion activa con su usuario, token o identificador equivalente, fecha de inicio, fecha de expiracion y estado.

**Criterios de aceptacion**

- **Dado** que un login finaliza correctamente, **cuando** se consulta el registro de sesiones, **entonces** existe una sesion activa asociada al usuario autenticado.
- **Dado** que una sesion se registra, **cuando** se consultan sus datos, **entonces** contiene fecha de inicio y fecha de expiracion coherentes.
- **Dado** que una sesion deja de ser valida, **cuando** se consulta su estado, **entonces** aparece como expirada o invalidada y no puede autorizar nuevas solicitudes.
- **Dado** que un usuario tiene una sesion activa, **cuando** se consultan sus datos de usuario, **entonces** la sesion no modifica ni expone su contrasena almacenada.

### RF-004 - Validar tokens en solicitudes protegidas

El sistema DEBE validar el token de sesion antes de permitir el acceso a rutas protegidas.

**Criterios de aceptacion**

- **Dado** que una solicitud protegida contiene un token valido y no expirado, **cuando** el middleware la procesa, **entonces** permite continuar y proporciona la identidad del usuario al controlador.
- **Dado** que una solicitud protegida no contiene token, **cuando** el middleware la procesa, **entonces** rechaza el acceso con el error `sesion.no-autenticado`.
- **Dado** que una solicitud protegida contiene un token mal formado, **cuando** el middleware la procesa, **entonces** rechaza el acceso con el error `sesion.token-invalido`.
- **Dado** que una solicitud protegida contiene un token de otra sesion o usuario, **cuando** el middleware la procesa, **entonces** rechaza el acceso y no entrega datos protegidos.

### RF-005 - Expirar sesiones

El sistema DEBE invalidar automaticamente las sesiones cuando superen su tiempo de validez o cuando se produzca la condicion de inactividad definida por el contrato.

**Criterios de aceptacion**

- **Dado** que una sesion supera su fecha de expiracion, **cuando** se utiliza su token en una ruta protegida, **entonces** el sistema rechaza la solicitud con `sesion.expirada`.
- **Dado** que una sesion es valida antes de su fecha de expiracion, **cuando** se utiliza su token en una ruta protegida, **entonces** el sistema permite la solicitud si el resto de autorizaciones es correcto.
- **Dado** que una sesion expira, **cuando** se consulta su estado, **entonces** no permanece como activa.
- **Dado** que una sesion expirada intenta acceder a datos de usuario, **cuando** el middleware procesa la solicitud, **entonces** no devuelve informacion protegida.

### RF-006 - Cerrar sesion manualmente

El sistema DEBE permitir que un usuario cierre una sesion activa e invalide su token.

**Criterios de aceptacion**

- **Dado** que existe una sesion activa y autenticada, **cuando** el usuario solicita cerrar sesion, **entonces** el sistema invalida esa sesion.
- **Dado** que una sesion fue cerrada, **cuando** se reutiliza su token, **entonces** el sistema rechaza la solicitud protegida.
- **Dado** que se solicita cerrar una sesion sin autenticacion valida, **cuando** el sistema procesa la solicitud, **entonces** responde con `sesion.no-autenticado` sin modificar otras sesiones.
- **Dado** que un usuario tiene varias sesiones activas, **cuando** cierra una de ellas, **entonces** las demas permanecen activas conforme a la regla de sesiones multiples.

### RF-007 - Permitir sesiones multiples

El sistema DEBE permitir que un usuario mantenga varias sesiones activas simultaneamente, siempre que cada token sea valido.

**Criterios de aceptacion**

- **Dado** que un usuario ya tiene una sesion activa, **cuando** inicia sesion desde otro dispositivo, **entonces** el sistema crea una segunda sesion valida.
- **Dado** que existen varias sesiones activas del mismo usuario, **cuando** cada token se utiliza dentro de su vigencia, **entonces** las solicitudes son autenticadas como el mismo usuario.
- **Dado** que una de las sesiones multiples se cierra, **cuando** se utiliza otra sesion aun activa, **entonces** la segunda sigue funcionando.

## Requisitos no funcionales

### RNF-001 - Seguridad de credenciales

El sistema DEBE proteger las credenciales durante su transporte, validacion y persistencia.

**Criterios de aceptacion**

- **Dado** que el usuario envia credenciales, **cuando** se transmiten al backend, **entonces** se utiliza el canal seguro configurado para la aplicacion.
- **Dado** que el sistema valida una contrasena, **cuando** la compara con el valor persistido, **entonces** utiliza un verificador de hashes y no compara ni almacena contrasenas en texto plano.
- **Dado** que se produce un error de autenticacion, **cuando** se registra o devuelve el resultado, **entonces** no incluye la contrasena, su hash ni el token.

### RNF-002 - Seguridad de tokens

Los tokens DEBEN ser impredecibles, firmados o verificables y estar limitados por una fecha de expiracion.

**Criterios de aceptacion**

- **Dado** que el sistema emite un token, **cuando** se comprueba su validez, **entonces** puede verificarse su integridad y origen.
- **Dado** que una persona modifica un token, **cuando** lo utiliza en una solicitud protegida, **entonces** el sistema lo rechaza.
- **Dado** que un token ha expirado o sido invalidado, **cuando** se utiliza, **entonces** no permite acceder a recursos protegidos.
- **Dado** que el sistema escribe logs de autenticacion, **cuando** se revisan, **entonces** no contienen tokens completos ni credenciales.

### RNF-003 - Separacion arquitectonica

La implementacion DEBE respetar la separacion entre dominio, aplicacion, infraestructura y presentacion.

**Criterios de aceptacion**

- **Dado** que el controlador recibe una solicitud de login, **cuando** inicia el caso de uso, **entonces** delega la validacion y creacion de sesion a la capa de aplicacion.
- **Dado** que se genera o verifica un token, **cuando** se ejecuta la operacion, **entonces** la integracion con JWT o mecanismo equivalente pertenece a infraestructura y no al dominio.
- **Dado** que se persiste una sesion, **cuando** la aplicacion accede a los datos, **entonces** utiliza una interfaz de repositorio y no acopla el dominio a JPA.
- **Dado** que cambia la representacion HTTP de un error, **cuando** se modifica el controlador o manejador, **entonces** no se altera la regla de autenticacion del dominio.

### RNF-004 - Contrato HTTP estable

El endpoint de login, validacion y cierre de sesion DEBE publicar contratos HTTP claros y consistentes.

**Criterios de aceptacion**

- **Dado** que un cliente envia credenciales validas, **cuando** llama al endpoint de login, **entonces** utiliza el metodo, ruta y JSON definidos por el contrato vigente.
- **Dado** que el login es correcto, **cuando** el backend responde, **entonces** devuelve JSON con `token` y un estado HTTP de exito.
- **Dado** que las credenciales son invalidas o el usuario no existe, **cuando** el backend responde, **entonces** devuelve un estado HTTP de error y uno de los codigos de dominio definidos.
- **Dado** que una solicitud protegida no esta autenticada, **cuando** el backend responde, **entonces** utiliza el codigo `sesion.no-autenticado` sin filtrar datos internos.

### RNF-005 - Resiliencia y consistencia

El sistema DEBE mantener un estado de sesiones coherente ante errores, solicitudes repetidas y operaciones concurrentes.

**Criterios de aceptacion**

- **Dado** que una solicitud de login falla antes de crear la sesion, **cuando** finaliza el procesamiento, **entonces** no queda una sesion activa parcial.
- **Dado** que se repite una solicitud de cierre de sesion, **cuando** el sistema la procesa, **entonces** no reactiva la sesion ni altera sesiones ajenas.
- **Dado** que se producen dos inicios de sesion validos de forma concurrente, **cuando** ambos finalizan, **entonces** cada resultado mantiene una sesion independiente y consistente.
- **Dado** que el almacenamiento de sesiones no esta disponible, **cuando** se solicita iniciar sesion, **entonces** el sistema devuelve un error controlado y no expone detalles de infraestructura.

## Fuera de alcance

- Autenticacion social mediante Google, Apple u otros proveedores.
- Recuperacion o cambio de contrasena.
- Autenticacion multifactor.
- Gestion de permisos y roles, correspondiente a CHG-0009.
- Registro de usuarios persistentes, correspondiente a CHG-0007.
- Pantallas frontend de registro y login, correspondientes a CHG-0101 y CHG-0102.
- Integracion con Stripe o gestion de pagos.

## Dependencias y supuestos

- CHG-0007 proporciona usuarios persistentes con email y contrasena protegida.
- El backend dispone de un mecanismo para comparar contrasenas de forma segura.
- El backend dispone o definira un mecanismo de tokens firmados o equivalente.
- El contrato de sesiones define la duracion, formato, transporte y revocacion de los tokens.
- Las rutas protegidas identifican al usuario autenticado mediante el contexto de seguridad o equivalente.

## Trazabilidad

| Requisito | Objetivo de la propuesta |
|---|---|
| RF-001 | Inicio de sesion con email y contrasena. |
| RF-002 | Emision de tokens de sesion seguros. |
| RF-003 | Persistencia y ciclo de vida de sesiones. |
| RF-004 | Middleware para proteger rutas y validar identidad. |
| RF-005 | Expiracion automatica de sesiones. |
| RF-006 | Cierre de sesion e invalidacion del token. |
| RF-007 | Soporte de varias sesiones por usuario. |
| RNF-001 | Proteccion de credenciales. |
| RNF-002 | Integridad, expiracion y no exposicion de tokens. |
| RNF-003 | Separacion entre dominio, aplicacion, infraestructura y presentacion. |
| RNF-004 | Contratos HTTP y errores de dominio consistentes. |
| RNF-005 | Consistencia ante errores y concurrencia. |
