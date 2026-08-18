# CHG-0007 - Requisitos del sistema de usuarios persistentes

## Estado
`borrador`

## Objetivo
Persistir los datos esenciales de cada usuario y su progreso de juego para que pueda recuperar su partida al iniciar sesión desde cualquier dispositivo autorizado.

## Alcance y dependencias

Este cambio cubre el almacenamiento, recuperación y actualización de los datos del usuario. Depende de CHG-0008 para disponer de una sesión autenticada y no define el mecanismo de sesiones.

Quedan fuera de este cambio la sincronización con Stripe, la integración con API-Football y la exportación manual de partidas.

## Requisitos funcionales

### RF-0001 - Crear el registro persistente del usuario

El sistema debe crear un registro único por usuario con, como mínimo, nombre, email, contraseña almacenada como hash, nivel, XP, energía, estado del jugador, tipo de membresía, fecha de creación y progreso del juego.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario válido que todavía no existe, cuando se registra mediante el flujo de creación de usuario, entonces el sistema crea un único registro persistente con los datos obligatorios y valores iniciales válidos para nivel, XP, energía, estado, membresía y progreso.
- **CA-0002:** Dado un usuario cuyo email ya está registrado, cuando se intenta crear otro usuario con ese email, entonces el sistema rechaza la operación y no crea un segundo registro.
- **CA-0003:** Dado un intento de creación con un campo obligatorio ausente o inválido, cuando se procesa la solicitud, entonces el sistema rechaza la operación indicando un error de validación y no persiste datos incompletos.

### RF-0002 - Proteger la contraseña

El sistema debe almacenar únicamente un hash seguro de la contraseña y nunca la contraseña en texto plano.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario con una contraseña válida, cuando se persiste su registro, entonces el valor almacenado de contraseña es un hash y no coincide con la contraseña original.
- **CA-0002:** Dado un registro persistido, cuando se devuelve información del usuario o de su progreso, entonces la respuesta no contiene la contraseña ni su hash.
- **CA-0003:** Dado un usuario persistido, cuando se valida una contraseña mediante el mecanismo de autenticación, entonces una contraseña correcta es aceptada y una incorrecta es rechazada sin revelar cuál fue el valor almacenado.

### RF-0003 - Recuperar el usuario y su progreso

El sistema debe recuperar el registro y el progreso asociados al usuario autenticado al iniciar sesión.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario persistido con progreso guardado, cuando inicia sesión correctamente, entonces el sistema carga su nivel, XP, energía, estado del jugador, membresía y progreso de juego.
- **CA-0002:** Dado un identificador de usuario que no existe, cuando se solicita su recuperación, entonces el sistema devuelve un resultado de no encontrado y no crea datos automáticamente.
- **CA-0003:** Dado un usuario autenticado, cuando solicita su progreso, entonces solo puede obtener el registro que le pertenece.

### RF-0004 - Actualizar los datos persistentes del usuario

El sistema debe permitir actualizar los datos de progreso y estado del usuario sin perder los valores no modificados.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario autenticado y un cambio válido en su progreso, cuando se guarda el cambio, entonces el sistema actualiza únicamente los datos indicados y conserva los demás valores.
- **CA-0002:** Dado un cambio que produciría un nivel, XP, energía, estado, membresía o progreso inválido según las reglas del dominio, cuando se intenta guardar, entonces el sistema rechaza la operación y conserva el último estado válido.
- **CA-0003:** Dado un usuario autenticado, cuando intenta actualizar el registro de otro usuario, entonces el sistema rechaza la operación por falta de autorización y no modifica ningún registro.

### RF-0005 - Guardado automático

El sistema debe guardar automáticamente el estado del usuario al cerrar sesión y después de acciones importantes que modifiquen su progreso.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario autenticado con cambios pendientes, cuando cierra sesión correctamente, entonces el sistema persiste el último estado válido antes de finalizar la sesión.
- **CA-0002:** Dado un usuario autenticado, cuando completa una acción importante que modifica nivel, XP, energía, estado o progreso, entonces el sistema persiste el resultado de esa acción.
- **CA-0003:** Dado un fallo durante el guardado automático, cuando el sistema no puede persistir el cambio, entonces informa del fallo, conserva o recupera el último estado confirmado y no comunica el cambio como guardado.

### RF-0006 - Consultar y modificar los datos mediante una frontera de persistencia

La aplicación debe disponer de operaciones de persistencia para crear, buscar y actualizar usuarios sin acoplar las reglas del dominio a una tecnología concreta de base de datos.

**Criterios de aceptación**

- **CA-0001:** Dado un caso de uso de creación, recuperación o actualización, cuando ejecuta una operación de persistencia, entonces utiliza una abstracción de repositorio y no depende directamente de detalles de la base de datos.
- **CA-0002:** Dado un error de almacenamiento, cuando el repositorio no puede completar la operación, entonces el caso de uso recibe un error controlado y no devuelve un resultado parcial como exitoso.

## Requisitos no funcionales

### RNF-0001 - Seguridad y privacidad

El acceso a los datos persistentes debe requerir autenticación válida y autorización basada en la identidad del usuario. Los datos personales y las credenciales deben tratarse conforme a las reglas de seguridad del proyecto.

**Criterios de aceptación**

- **CA-0001:** Dado un usuario sin sesión válida, cuando intenta crear, consultar o actualizar datos persistentes protegidos, entonces el sistema rechaza la operación sin exponer información de usuarios.
- **CA-0002:** Dado un usuario autenticado, cuando solicita datos de otro usuario, entonces el sistema aplica la autorización y no revela sus datos personales ni su progreso.

### RNF-0002 - Integridad y atomicidad

Cada guardado debe dejar el registro en un estado consistente. Una operación que falle no debe dejar una actualización parcial.

**Criterios de aceptación**

- **CA-0001:** Dado un guardado que modifica varios campos relacionados, cuando falla antes de completarse, entonces ningún subconjunto parcial se presenta como el estado confirmado del usuario.
- **CA-0002:** Dado un registro persistido, cuando se recupera, entonces sus campos obligatorios mantienen tipos, restricciones y relaciones coherentes.

### RNF-0003 - Persistencia entre sesiones y dispositivos

Los datos confirmados deben sobrevivir al cierre de sesión, al reinicio de la aplicación y al acceso desde otro dispositivo autorizado.

**Criterios de aceptación**

- **CA-0001:** Dado un estado guardado correctamente, cuando el usuario cierra y vuelve a iniciar sesión, entonces recupera el mismo estado confirmado.
- **CA-0002:** Dado un estado guardado correctamente, cuando el usuario inicia sesión desde otro dispositivo autorizado, entonces recupera sus datos sin crear una partida independiente.

### RNF-0004 - Trazabilidad de errores

Los errores de validación, autenticación, autorización, usuario inexistente y persistencia deben diferenciarse mediante respuestas controladas, sin revelar credenciales ni detalles internos de la base de datos.

**Criterios de aceptación**

- **CA-0001:** Dado cualquiera de los errores definidos, cuando se comunica al cliente, entonces recibe una respuesta estable y segura que permite distinguir la categoría del error sin incluir información sensible o detalles internos.

## Modelo mínimo de datos

La entidad `Usuario` debe incluir:

- `id` único y estable.
- `nombre`.
- `email` único y validado.
- `passwordHash`; nunca `password` en texto plano.
- `nivel`.
- `xp`.
- `energia`.
- `estadoJugador`.
- `membresia` con los valores de dominio permitidos, incluyendo `trial`, `normal` y `premium`.
- `fechaCreacion`.
- `progresoJuego`.
- Metadatos de actualización suficientes para controlar el último estado confirmado.

## Restricciones

- La persistencia no debe introducir reglas de negocio en la capa de infraestructura.
- El sistema no debe almacenar datos de tarjetas ni sincronizar membresías con Stripe dentro de CHG-0007.
- El sistema no debe simular ni consumir estadísticas de API-Football dentro de CHG-0007.
- La definición de sesiones, expiración de tokens y recuperación de sesión pertenece a CHG-0008.

## Trazabilidad

| Requisito | Área de la propuesta |
|---|---|
| RF-0001 | Crear tabla/colección `usuarios` y guardar datos esenciales |
| RF-0002 | Contraseña almacenada como hash; seguridad sensible |
| RF-0003 | Cargar automáticamente la partida al iniciar sesión |
| RF-0004 | Mantener y recuperar el progreso del juego |
| RF-0005 | Guardado automático al cerrar sesión o realizar acciones importantes |
| RF-0006 | CRUD y persistencia mediante una frontera de repositorio |
| RNF-0001 | Seguridad, autenticación y datos personales |
| RNF-0002 | Integridad de los datos persistentes |
| RNF-0003 | Mantener el progreso al cambiar de dispositivo |
| RNF-0004 | Validaciones y manejo seguro de errores |
