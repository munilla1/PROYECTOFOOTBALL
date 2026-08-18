# CHG-0007 - Tareas técnicas del sistema de usuarios persistentes

## Estado
`pendiente de aprobación de requirements.md`

## Regla de ejecución
Estas tareas no deben implementarse hasta que `requirements.md` sea aprobado. La implementación debe respetar la separación entre dominio, aplicación, infraestructura y presentación.

## Tareas

### T-0001 - Confirmar contratos y dependencias del cambio

- **Capa:** análisis / arquitectura
- **Requisitos:** RF-0001, RF-0003, RF-0005, RNF-0001
- **Descripción:** Confirmar el contrato de identidad autenticada proporcionado por CHG-0008, los límites de CHG-0007 y las reglas existentes de los dominios Usuario, Jugador, Membresías y Sesiones.
- **Verificación:** Documentar los identificadores de usuario, estados iniciales, valores permitidos de membresía y eventos que se consideran acciones importantes. No iniciar la implementación si existe una contradicción sin resolver.

### T-0002 - Definir la entidad de dominio Usuario

- **Capa:** dominio
- **Requisitos:** RF-0001, RF-0002, RF-0004, RNF-0002
- **Descripción:** Crear o ampliar la entidad `Usuario` con `id`, `nombre`, `email`, `passwordHash`, `nivel`, `xp`, `energia`, `estadoJugador`, `membresia`, `fechaCreacion` y `progresoJuego`, incluyendo metadatos del último estado confirmado.
- **Verificación:** Pruebas unitarias que comprueben la creación de una entidad válida, la obligatoriedad de sus campos y el rechazo de combinaciones inválidas.

### T-0003 - Implementar validaciones y reglas de dominio

- **Capa:** dominio
- **Requisitos:** RF-0001, RF-0004, RNF-0002, RNF-0004
- **Descripción:** Implementar validaciones puras para email, valores de nivel, XP, energía, estado del jugador, membresía y progreso. Definir errores de dominio diferenciados para datos inválidos.
- **Verificación:** Pruebas unitarias para valores límite, campos ausentes, membresías no permitidas y actualizaciones parciales que conserven el estado no modificado.

### T-0004 - Definir la interfaz del repositorio de usuarios

- **Capa:** dominio / aplicación
- **Requisitos:** RF-0001, RF-0003, RF-0004, RF-0006
- **Descripción:** Definir una interfaz de repositorio para crear, buscar por identificador, buscar por email y actualizar usuarios. La interfaz no debe exponer tipos ni detalles de una base de datos concreta.
- **Verificación:** Pruebas de contrato con una implementación en memoria o doble de prueba que cubran creación, búsqueda, actualización y usuario inexistente.

### T-0005 - Diseñar el esquema persistente de usuarios

- **Capa:** infraestructura
- **Requisitos:** RF-0001, RF-0002, RNF-0002, RNF-0003
- **Descripción:** Crear la migración o esquema de la tabla/colección `usuarios`, con identificador estable, email único, restricciones para campos obligatorios, fecha de creación y metadatos de actualización.
- **Verificación:** Ejecutar la migración en una base de datos de prueba y comprobar que se crea el esquema, que el email es único y que no se pueden persistir registros incompletos.

### T-0006 - Implementar el adaptador de persistencia

- **Capa:** infraestructura
- **Requisitos:** RF-0001, RF-0003, RF-0004, RF-0006, RNF-0002
- **Descripción:** Implementar el repositorio concreto que traduzca entre la entidad de dominio y el modelo persistente, gestione usuario inexistente y convierta los errores de almacenamiento a errores controlados.
- **Verificación:** Pruebas de integración contra la base de datos de prueba para crear, recuperar, actualizar y rechazar operaciones fallidas sin devolver resultados parciales.

### T-0007 - Integrar el hash de contraseñas

- **Capa:** aplicación / infraestructura
- **Requisitos:** RF-0002, RNF-0001
- **Descripción:** Incorporar el servicio de hash/verificación de contraseñas. El caso de uso debe recibir la contraseña, transformarla antes de persistirla y evitar que la entidad o las respuestas expongan la contraseña en texto plano.
- **Verificación:** Pruebas que comprueben que el valor persistido es un hash, que la verificación acepta una contraseña correcta, rechaza una incorrecta y que ninguna respuesta contiene contraseña ni hash.

### T-0008 - Implementar el caso de uso de creación de usuario

- **Capa:** aplicación
- **Requisitos:** RF-0001, RF-0002, RNF-0001, RNF-0002
- **Descripción:** Orquestar validación, comprobación de email único, generación del hash, inicialización del progreso y persistencia atómica del nuevo usuario.
- **Verificación:** Pruebas unitarias del caso de uso para creación válida, email duplicado, entrada inválida y fallo del repositorio; en los fallos no debe quedar un usuario creado parcialmente.

### T-0009 - Implementar el caso de uso de recuperación de usuario y progreso

- **Capa:** aplicación
- **Requisitos:** RF-0003, RNF-0001, RNF-0003, RNF-0004
- **Descripción:** Crear el caso de uso que, a partir de la identidad autenticada de CHG-0008, recupere el usuario y devuelva solo los datos de perfil y progreso permitidos.
- **Verificación:** Pruebas unitarias y de aceptación para usuario existente, usuario inexistente, sesión ausente y acceso a datos de otro usuario.

### T-0010 - Implementar el caso de uso de actualización de progreso

- **Capa:** aplicación
- **Requisitos:** RF-0004, RF-0006, RNF-0002, RNF-0003
- **Descripción:** Orquestar actualizaciones parciales del estado del usuario, validar el resultado completo y persistirlo como una operación atómica, conservando el último estado confirmado si falla.
- **Verificación:** Pruebas unitarias e integración para actualización válida, conservación de campos, valores inválidos, usuario no autorizado y fallo de almacenamiento.

### T-0011 - Integrar el guardado automático en acciones importantes

- **Capa:** aplicación
- **Requisitos:** RF-0005, RF-0004, RNF-0002
- **Descripción:** Identificar los casos de uso que modifican nivel, XP, energía, estado o progreso y conectar el guardado posterior a una acción válida. El guardado no debe confirmar cambios que no hayan sido persistidos.
- **Verificación:** Pruebas de aceptación que ejecuten cada acción importante definida en T-0001 y comprueben que el estado persistido coincide con el resultado confirmado.

### T-0012 - Integrar el guardado al cerrar sesión

- **Capa:** aplicación / sesiones
- **Requisitos:** RF-0005, RF-0003, RNF-0003
- **Descripción:** Integrar el caso de uso de guardado con el cierre de sesión de CHG-0008, asegurando que los cambios pendientes se persistan antes de finalizar la sesión o que el cierre comunique el fallo de forma controlada.
- **Verificación:** Pruebas de integración para cierre con cambios pendientes, cierre sin cambios y fallo de persistencia durante el cierre.

### T-0013 - Exponer las operaciones mediante la capa de presentación

- **Capa:** presentación
- **Requisitos:** RF-0001, RF-0003, RF-0004, RF-0005, RNF-0001, RNF-0004
- **Descripción:** Crear o adaptar los controladores HTTP necesarios para creación, recuperación y actualización. Mantener la lógica en los casos de uso y mapear errores a respuestas estables sin filtrar credenciales ni detalles internos.
- **Verificación:** Pruebas de aceptación HTTP para entradas válidas e inválidas, autenticación ausente, autorización insuficiente, email duplicado, usuario inexistente y error de persistencia.

### T-0014 - Aplicar autenticación y autorización

- **Capa:** presentación / aplicación
- **Requisitos:** RF-0003, RF-0004, RNF-0001, RNF-0004
- **Descripción:** Proteger los endpoints y casos de uso de datos persistentes con la identidad de CHG-0008, garantizando que un usuario solo pueda consultar o modificar sus propios datos.
- **Verificación:** Pruebas de seguridad para sesión válida, sesión ausente, identidad manipulada y acceso cruzado entre usuarios.

### T-0015 - Completar pruebas de aceptación del cambio

- **Capa:** pruebas de aceptación
- **Requisitos:** Todos los RF y RNF
- **Descripción:** Implementar escenarios de aceptación para el ciclo completo: creación, hash, inicio de sesión y recuperación, actualización, guardado automático, cierre de sesión, persistencia entre reinicios/dispositivos autorizados y errores.
- **Verificación:** Ejecutar la suite de aceptación y registrar en `evidence.md` cada escenario con resultado, evidencia y fecha de ejecución.

### T-0016 - Verificar migración, regresión y seguridad

- **Capa:** integración / calidad
- **Requisitos:** RNF-0001, RNF-0002, RNF-0003, RNF-0004
- **Descripción:** Ejecutar migraciones desde un entorno limpio, pruebas existentes del backend y comprobaciones de seguridad sobre respuestas, logs y errores. Confirmar que no se almacenan contraseñas en texto plano ni datos de tarjetas.
- **Verificación:** Suite completa del proyecto sin regresiones atribuibles a CHG-0007 y revisión de evidencia de que los datos sensibles no aparecen en respuestas ni registros.

## Orden recomendado

1. T-0001 - Confirmar contratos y dependencias.
2. T-0002 - Definir la entidad de dominio.
3. T-0003 - Implementar validaciones y reglas.
4. T-0004 - Definir la interfaz del repositorio.
5. T-0005 - Diseñar el esquema persistente.
6. T-0006 - Implementar el adaptador de persistencia.
7. T-0007 - Integrar el hash de contraseñas.
8. T-0008 - Implementar creación de usuario.
9. T-0009 - Implementar recuperación de usuario y progreso.
10. T-0010 - Implementar actualización de progreso.
11. T-0011 - Integrar guardado en acciones importantes.
12. T-0012 - Integrar guardado al cerrar sesión.
13. T-0013 - Exponer operaciones mediante presentación.
14. T-0014 - Aplicar autenticación y autorización.
15. T-0015 - Completar pruebas de aceptación.
16. T-0016 - Verificar migración, regresión y seguridad.

## Dependencias externas

- **CHG-0008:** identidad autenticada, sesiones y cierre de sesión.
- **Dominio Usuario:** reglas, entidades y errores existentes.
- **Dominio Jugador:** estado y progreso del jugador, si se almacenan como relación o como parte del agregado.
- **Dominio Membresías:** valores válidos de membresía y sus reglas.

## Trazabilidad resumida

| Requisito | Tareas |
|---|---|
| RF-0001 | T-0002, T-0004, T-0005, T-0006, T-0008, T-0013 |
| RF-0002 | T-0002, T-0007, T-0008, T-0016 |
| RF-0003 | T-0001, T-0004, T-0006, T-0009, T-0012, T-0013 |
| RF-0004 | T-0002, T-0003, T-0004, T-0006, T-0010, T-0011, T-0013 |
| RF-0005 | T-0001, T-0011, T-0012, T-0013 |
| RF-0006 | T-0004, T-0006, T-0010 |
| RNF-0001 | T-0001, T-0007, T-0008, T-0009, T-0013, T-0014, T-0016 |
| RNF-0002 | T-0002, T-0003, T-0005, T-0006, T-0008, T-0010, T-0011, T-0016 |
| RNF-0003 | T-0005, T-0006, T-0009, T-0010, T-0012, T-0015, T-0016 |
| RNF-0004 | T-0003, T-0009, T-0013, T-0014, T-0015, T-0016 |
