# CHG-0008 - Requisitos del sistema de sesiones del usuario

## Estado
`borrador`

## Objetivo
Implementar un sistema backend de sesiones que permita autenticar usuarios registrados, crear y cerrar sesiones, validar tokens, aplicar expiración y proteger las rutas que requieran una identidad autenticada.

## Alcance y dependencias

Este cambio cubre la autenticación mediante email y contraseña, el ciclo de vida de las sesiones, la emisión y validación de tokens, la expiración por tiempo o inactividad y el middleware de autenticación para rutas protegidas.

Depende de CHG-0007 para consultar usuarios persistidos y verificar contraseñas almacenadas como hash. Los roles, membresías, recuperación de contraseña y autenticación social no modifican su dominio en este cambio.

## Requisitos funcionales

### RF-0008-01 - Iniciar sesión con credenciales válidas

El sistema debe permitir que un usuario registrado inicie sesión mediante email y contraseña válidos.

**Criterios de aceptación**

- **CA-0008-01:** Dado un usuario registrado con una contraseña válida, cuando envía su email y contraseña al endpoint de login, entonces el sistema autentica la identidad y crea una sesión activa.
- **CA-0008-02:** Dado un usuario registrado, cuando inicia sesión correctamente, entonces la sesión queda asociada al identificador persistente del usuario y contiene fecha de inicio y expiración.
- **CA-0008-03:** Dado un usuario registrado con varias sesiones permitidas, cuando inicia sesión desde otro dispositivo autorizado, entonces el sistema crea otra sesión válida sin invalidar indebidamente las sesiones permitidas existentes.
- **CA-0008-04:** Dado que la autenticación termina correctamente, cuando el sistema responde al cliente, entonces no devuelve la contraseña ni el `passwordHash` del usuario.

### RF-0008-02 - Rechazar credenciales no válidas

El sistema debe rechazar credenciales incorrectas sin revelar información que permita distinguir de forma insegura si un email está registrado.

**Criterios de aceptación**

- **CA-0008-01:** Dado un email registrado y una contraseña incorrecta, cuando se solicita el inicio de sesión, entonces el sistema rechaza la operación con el error `sesion.credenciales-invalidas`.
- **CA-0008-02:** Dado un email no registrado y cualquier contraseña, cuando se solicita el inicio de sesión, entonces el sistema rechaza la operación con una respuesta controlada y no confirma la existencia del usuario.
- **CA-0008-03:** Dado un email o contraseña ausente, vacío o con formato inválido, cuando se procesa la petición, entonces el sistema rechaza la operación como datos inválidos sin consultar ni crear una sesión.
- **CA-0008-04:** Dado un intento de login rechazado, cuando el sistema responde, entonces no emite, persiste ni devuelve un token de sesión parcial o inválido.

### RF-0008-03 - Validar tokens de sesión

El sistema debe validar que un token pertenece a una sesión válida, no ha sido manipulado y continúa vigente.

**Criterios de aceptación**

- **CA-0008-01:** Dado un token firmado, válido y asociado a una sesión activa, cuando se utiliza para acceder a una ruta protegida, entonces el middleware acepta la identidad y permite continuar la operación.
- **CA-0008-02:** Dado un token ausente, mal formado o con firma inválida, cuando se utiliza para acceder a una ruta protegida, entonces el sistema rechaza la operación con `sesion.token-invalido` o `sesion.no-autenticado` según corresponda.
- **CA-0008-03:** Dado un token válido cuya sesión fue invalidada, cuando se utiliza para acceder a una ruta protegida, entonces el sistema rechaza la operación y no considera autenticado al usuario.
- **CA-0008-04:** Dado un token válido, cuando el middleware resuelve la identidad, entonces solo expone al caso de uso el identificador autenticado y los datos mínimos permitidos, nunca credenciales.

### RF-0008-04 - Cerrar sesión

El sistema debe permitir cerrar una sesión activa e invalidar sus credenciales de acceso.

**Criterios de aceptación**

- **CA-0008-01:** Dado un usuario con una sesión activa, cuando solicita cerrar sesión con su token válido, entonces el sistema invalida esa sesión y devuelve una respuesta controlada de éxito.
- **CA-0008-02:** Dado un token de una sesión ya cerrada, cuando se intenta cerrar sesión nuevamente, entonces el sistema mantiene un resultado idempotente y no reactiva la sesión.
- **CA-0008-03:** Dado un usuario con varias sesiones activas, cuando cierra una sesión, entonces las demás sesiones permanecen activas salvo que una política explícita indique lo contrario.
- **CA-0008-04:** Dado un cierre de sesión, cuando termina la operación, entonces el token cerrado no permite acceder posteriormente a rutas protegidas.

### RF-0008-05 - Expirar sesiones

El sistema debe expirar automáticamente las sesiones cuando superan su tiempo de vigencia o incumplen la política de inactividad configurada.

**Criterios de aceptación**

- **CA-0008-01:** Dada una sesión cuya fecha de expiración ya pasó, cuando se utiliza su token, entonces el sistema rechaza la operación con `sesion.expirada`.
- **CA-0008-02:** Dada una sesión que supera el tiempo máximo de inactividad, cuando se utiliza su token, entonces el sistema la marca como expirada y rechaza el acceso.
- **CA-0008-03:** Dada una sesión activa dentro de su ventana de vigencia e inactividad, cuando se utiliza su token, entonces el sistema permite el acceso y aplica la política de renovación definida.
- **CA-0008-04:** Dado un token expirado, cuando se consulta o modifica el estado de la sesión, entonces el sistema no lo reactiva automáticamente ni lo acepta como válido.

### RF-0008-06 - Proteger rutas autenticadas

El sistema debe proporcionar un middleware o mecanismo equivalente para exigir una sesión válida en las operaciones que requieran identidad.

**Criterios de aceptación**

- **CA-0008-01:** Dado un endpoint marcado como protegido, cuando se solicita sin sesión, entonces el sistema rechaza la operación con `sesion.no-autenticado`.
- **CA-0008-02:** Dado un endpoint protegido, cuando se solicita con un token inválido o expirado, entonces el sistema rechaza la operación con el error de sesión correspondiente y no ejecuta el caso de uso.
- **CA-0008-03:** Dado un endpoint protegido y un token válido, cuando se procesa la petición, entonces el caso de uso recibe la identidad autenticada sin que el controlador tenga que interpretar directamente el token.
- **CA-0008-04:** Dado un endpoint público, cuando se solicita sin token, entonces el middleware no bloquea la operación por ausencia de sesión.

## Requisitos no funcionales

### RNF-0008-01 - Seguridad de credenciales

El sistema debe tratar la contraseña como secreto y utilizar exclusivamente el hash persistido por CHG-0007 para verificarla.

**Criterios de aceptación**

- **CA-0008-01:** Dada una contraseña recibida en una petición de login, cuando se valida, entonces se compara mediante el servicio de hash y nunca se almacena como texto plano.
- **CA-0008-02:** Dado un login correcto o fallido, cuando se registran eventos técnicos, entonces los logs no contienen la contraseña, el `passwordHash` ni el token completo.
- **CA-0008-03:** Dado un error de autenticación, cuando se comunica al cliente, entonces la respuesta no incluye credenciales, hashes ni detalles internos de persistencia.

### RNF-0008-02 - Seguridad de tokens

Los tokens deben ser únicos, firmados, no predecibles y transportados mediante el mecanismo seguro definido por el contrato de la aplicación.

**Criterios de aceptación**

- **CA-0008-01:** Dado que se crea una sesión, cuando se genera su token, entonces el token es único, firmado y contiene únicamente las claims mínimas necesarias, como el identificador de usuario, emisión y expiración.
- **CA-0008-02:** Dado un token alterado, cuando se valida su firma, entonces el sistema lo rechaza sin resolver la identidad como autenticada.
- **CA-0008-03:** Dado un token emitido, cuando se entrega al cliente, entonces se transporta por HTTPS en producción y no se incluye en URLs, parámetros de consulta ni mensajes de error.
- **CA-0008-04:** Dado que la arquitectura utiliza cookies para la sesión, cuando se establece la cookie, entonces aplica los atributos `HttpOnly`, `Secure` y `SameSite` apropiados para el despliegue.
- **CA-0008-05:** Dado que la arquitectura devuelve el token al cliente para su transporte posterior, cuando se documenta el contrato, entonces quedan definidos su audiencia, expiración, revocación y mecanismo de almacenamiento seguro antes de consumirlo desde frontend.

### RNF-0008-03 - Separación de capas

La lógica de sesiones debe respetar la arquitectura de dominio, aplicación, infraestructura y presentación.

**Criterios de aceptación**

- **CA-0008-01:** Dado que se valida una credencial o una sesión, cuando se ejecuta la regla de negocio, entonces el dominio no depende de Spring, JPA, JWT ni detalles HTTP.
- **CA-0008-02:** Dado un caso de uso de login, logout o validación, cuando necesita persistir o firmar datos, entonces utiliza interfaces y adaptadores de infraestructura.
- **CA-0008-03:** Dado un controlador HTTP de sesiones, cuando recibe una petición, entonces delega la lógica al caso de uso y se limita a traducir entrada, salida y errores.

### RNF-0008-04 - Persistencia e integridad de sesiones

Las sesiones activas deben persistirse o gestionarse mediante un mecanismo que permita validar su vigencia e invalidarlas de forma controlada.

**Criterios de aceptación**

- **CA-0008-01:** Dada una sesión creada correctamente, cuando se consulta el estado de sesiones, entonces existe una asociación consistente entre sesión, usuario, token o identificador de token, fechas y estado.
- **CA-0008-02:** Dado un cierre o expiración de sesión, cuando se actualiza su estado, entonces la sesión deja de aceptarse sin crear registros duplicados o estados contradictorios.
- **CA-0008-03:** Dado un reinicio del servicio, cuando se valida una sesión cuyo estado debe sobrevivir según la política configurada, entonces el sistema conserva una decisión de validez coherente con el mecanismo de persistencia elegido.

### RNF-0008-05 - Trazabilidad y control de errores

El sistema debe exponer errores de sesión estables y seguros, diferenciando las categorías necesarias sin filtrar detalles internos.

**Criterios de aceptación**

- **CA-0008-01:** Dado un token mal formado o manipulado, cuando se comunica el rechazo, entonces el cliente recibe `sesion.token-invalido` sin detalles criptográficos internos.
- **CA-0008-02:** Dado un token cuya vigencia terminó, cuando se comunica el rechazo, entonces el cliente recibe `sesion.expirada`.
- **CA-0008-03:** Dado un intento con credenciales no válidas, cuando se comunica el rechazo, entonces el cliente recibe `sesion.credenciales-invalidas` sin confirmar si el usuario existe.
- **CA-0008-04:** Dado un acceso sin identidad autenticada, cuando se comunica el rechazo, entonces el cliente recibe `sesion.no-autenticado`.
- **CA-0008-05:** Dado un error inesperado de almacenamiento o infraestructura, cuando se comunica al cliente, entonces recibe un error genérico sin stack trace, SQL, secreto o configuración interna.

### RNF-0008-06 - Rendimiento y resiliencia

El sistema debe responder de forma predecible bajo solicitudes repetidas, fallos transitorios y expiración concurrente.

**Criterios de aceptación**

- **CA-0008-01:** Dado un mismo intento de login repetido, cuando se reciben varias peticiones, entonces cada respuesta mantiene un resultado controlado y no se crean sesiones corruptas o tokens duplicados para una misma operación lógica.
- **CA-0008-02:** Dado un fallo temporal de persistencia al crear o cerrar una sesión, cuando la operación no puede completarse, entonces el sistema no comunica éxito ni deja una sesión parcialmente activa.
- **CA-0008-03:** Dado que varias peticiones utilizan una sesión durante su expiración, cuando se evalúa su vigencia concurrentemente, entonces ninguna petición obtiene acceso después de que la sesión haya sido invalidada.

## Contrato de integración

- **Login:** `POST /api/sesiones/login`.
- **Petición mínima:** `{ "email": "...", "password": "..." }`.
- **Éxito:** `200 OK` con una sesión establecida según la estrategia acordada. La respuesta no contiene la contraseña ni el `passwordHash`.
- **Credenciales inválidas:** `401 Unauthorized` con `code: "sesion.credenciales-invalidas"`.
- **Datos inválidos:** `400 Bad Request` con un código de validación controlado.
- **Logout:** `POST /api/sesiones/logout`, autenticado con la sesión activa.
- **Éxito de logout:** `204 No Content` o respuesta equivalente idempotente.
- **Token:** debe ser único, firmado y contener expiración. El mecanismo de transporte puede ser cookie segura o token explícito según la decisión de arquitectura documentada antes de implementar.
- **Sesión:** el backend debe poder invalidar una sesión cerrada o expirada; no basta con verificar únicamente la firma si la política requiere revocación inmediata.
- **Errores definidos:** `sesion.token-invalido`, `sesion.expirada`, `sesion.credenciales-invalidas` y `sesion.no-autenticado`.
- **Rutas protegidas:** el middleware debe dejar disponible la identidad autenticada para los casos de uso posteriores.

## Exclusiones

- Autenticación social.
- Recuperación, cambio o restablecimiento de contraseña.
- Gestión de roles y autorización específica de administradores.
- Integración directa con Stripe o API-Football.
- Implementación de la pantalla frontend de login de CHG-0102.
- Implementación del panel de usuario de CHG-0103.

## Trazabilidad

| Requisito | Área de la propuesta |
|---|---|
| RF-0008-01 | Inicio de sesión con credenciales propias |
| RF-0008-02 | Rechazo seguro de credenciales incorrectas |
| RF-0008-03 | Validación de tokens de sesión |
| RF-0008-04 | Cierre de sesión manual e invalidación |
| RF-0008-05 | Expiración por tiempo o inactividad |
| RF-0008-06 | Middleware para rutas protegidas |
| RNF-0008-01 | Protección de contraseñas y datos sensibles |
| RNF-0008-02 | Tokens únicos, firmados y transportados de forma segura |
| RNF-0008-03 | Separación dominio, aplicación, infraestructura y presentación |
| RNF-0008-04 | Persistencia e integridad del estado de sesión |
| RNF-0008-05 | Errores estables y sin detalles internos |
| RNF-0008-06 | Resiliencia, concurrencia y ausencia de estados parciales |
