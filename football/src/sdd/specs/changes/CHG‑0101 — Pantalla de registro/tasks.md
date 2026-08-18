# CHG-0101 - Tareas técnicas de la pantalla de registro

## Estado
`pendiente de aprobación de requirements.md`

## Regla de ejecución
Estas tareas no deben implementarse hasta que `requirements.md` sea aprobado. La implementación debe respetar la separación entre UI, Core, Domain y Styles definida por el Frontend Agent.

## Tareas

### T-0101 - Confirmar contratos y dependencias frontend

- **Capa:** análisis / arquitectura
- **Requisitos:** RF-0103, RF-0104, RF-0105, RNF-0104
- **Descripción:** Confirmar la ruta de registro, la ruta de login de CHG-0102, el endpoint de CHG-0007, el formato de petición y los códigos de error que debe traducir la interfaz.
- **Verificación:** Documentar el contrato `POST` de registro, el comportamiento esperado para éxito y los mapeos de `usuario.email-duplicado`, validación, red y error inesperado. Identificar cualquier contrato pendiente antes de implementar.

### T-0102 - Definir el modelo de dominio del registro

- **Capa:** Domain
- **Requisitos:** RF-0101, RF-0102, RF-0103, RF-0105, RNF-0102, RNF-0104
- **Descripción:** Crear los tipos o view-models para los valores del formulario, errores por campo, estado de envío, resultado exitoso y errores de dominio. No incluir contraseña en modelos de respuesta ni en estado persistente.
- **Verificación:** Comprobar que los tipos distinguen datos de entrada, respuesta segura y categorías de error sin exponer `passwordHash`.

### T-0103 - Implementar el adaptador de validación

- **Capa:** Domain
- **Requisitos:** RF-0102, RF-0105, RF-0106, RNF-0101
- **Descripción:** Implementar validaciones puras para nombre, email y contraseña, incluyendo mensajes asociados a cada campo y estado de formulario válido o inválido.
- **Verificación:** Verificar nombres vacíos o con espacios, emails inválidos, contraseñas inválidas, corrección de errores y validación al perder el foco y al enviar.

### T-0104 - Implementar el servicio HTTP de registro

- **Capa:** Core
- **Requisitos:** RF-0103, RF-0105, RNF-0102, RNF-0104, RNF-0105
- **Descripción:** Crear un servicio tipado que envíe `POST` al endpoint de CHG-0007 con `{ nombre, email, password }`, transforme las respuestas y clasifique errores de dominio, validación, red e inesperados.
- **Verificación:** Comprobar que el servicio realiza una única petición por operación, no añade credenciales a la URL y no devuelve ni almacena la contraseña o el hash en el modelo de vista.

### T-0105 - Implementar el controlador o view-model de registro

- **Capa:** Core / Domain
- **Requisitos:** RF-0102, RF-0103, RF-0104, RF-0105, RF-0106, RNF-0104, RNF-0105
- **Descripción:** Orquestar estado del formulario, validación, envío, bloqueo durante la petición, tratamiento de errores, foco tras errores y navegación tras éxito sin colocar lógica HTTP en el componente visual.
- **Verificación:** Verificar los estados inicial, inválido, válido, enviando, error corregible, error de red y éxito. Confirmar que los envíos repetidos mientras la petición está en curso se ignoran.

### T-0106 - Construir el componente y la página de registro

- **Capa:** UI
- **Requisitos:** RF-0101, RF-0102, RF-0103, RF-0104, RF-0105, RF-0106
- **Descripción:** Crear la página con campos de nombre, email y contraseña, etiquetas, acción principal, mensajes de validación, estado de carga, confirmación y error general. Consumir exclusivamente el view-model y el servicio definido en capas inferiores.
- **Verificación:** Comprobar que la UI representa cada estado sin duplicar reglas de negocio ni llamadas HTTP dentro del componente.

### T-0107 - Añadir accesibilidad al formulario

- **Capa:** UI
- **Requisitos:** RF-0101, RF-0106, RNF-0101
- **Descripción:** Asociar etiquetas y mensajes a sus controles, establecer nombres accesibles, orden de teclado, foco inicial o foco tras error y regiones anunciables para carga, éxito y errores.
- **Verificación:** Recorrer el flujo únicamente con teclado y revisar que errores y cambios de estado sean identificables sin depender solo del color.

### T-0108 - Integrar la ruta de registro y la navegación al login

- **Capa:** Core / UI
- **Requisitos:** RF-0101, RF-0104, RNF-0105
- **Descripción:** Registrar la ruta de la pantalla y conectar el éxito del registro con la ruta de login de CHG-0102, evitando crear sesión o reenviar el formulario al volver atrás.
- **Verificación:** Acceder directamente a la ruta de registro, completar un registro exitoso, comprobar la navegación al login y verificar que no se genera una sesión automáticamente.

### T-0109 - Aplicar estilos adaptables y estados visuales

- **Capa:** Styles / UI
- **Requisitos:** RF-0101, RF-0105, RF-0106, RNF-0103, RNF-0105
- **Descripción:** Definir estilos y variables coherentes con el frontend existente para escritorio y móvil, con estados visibles de foco, error, carga, éxito y controles deshabilitados.
- **Verificación:** Revisar la pantalla en resoluciones móvil y escritorio, confirmando ausencia de solapamiento, desplazamiento horizontal o pérdida de acciones.

### T-0110 - Aplicar protección de credenciales en frontend

- **Capa:** Core / UI
- **Requisitos:** RF-0103, RF-0105, RNF-0102
- **Descripción:** Mantener la contraseña únicamente durante la interacción necesaria, transmitirla por el canal configurado, no incluirla en URL, logs visibles, almacenamiento local, session storage ni cookies propias, y limpiar el estado tras terminar.
- **Verificación:** Inspeccionar peticiones, navegación, almacenamiento del navegador, modelo de respuesta y mensajes visibles para confirmar que no aparecen contraseña ni hash.

### T-0111 - Implementar pruebas de aceptación E2E

- **Capa:** pruebas de aceptación
- **Requisitos:** Todos los RF y RNF aplicables
- **Descripción:** Crear pruebas E2E con Cypress, Playwright o herramienta equivalente para el flujo completo de registro y sus estados de error, simulando el backend de forma controlada cuando sea necesario.
- **Verificación:** Cubrir formulario visible, validación en tiempo real, envío único, carga, registro exitoso y redirección, email duplicado, validación backend, fallo de red, reintento, teclado, respuesta móvil y ausencia de credenciales en la navegación.

### T-0112 - Verificar contrato, regresión y seguridad

- **Capa:** integración / calidad
- **Requisitos:** RF-0103, RF-0104, RF-0105, RNF-0101, RNF-0102, RNF-0103, RNF-0104, RNF-0105
- **Descripción:** Ejecutar las comprobaciones del frontend, validar compatibilidad con el endpoint de CHG-0007 y revisar que la nueva ruta no rompa la navegación existente ni introduzca exposición de credenciales.
- **Verificación:** Ejecutar build, lint, pruebas E2E y comprobaciones responsive/accessibility disponibles. Registrar resultados en `evidence.md`.

## Orden recomendado

1. T-0101 - Confirmar contratos y dependencias.
2. T-0102 - Definir el modelo de dominio.
3. T-0103 - Implementar el adaptador de validación.
4. T-0104 - Implementar el servicio HTTP.
5. T-0105 - Implementar el controlador o view-model.
6. T-0106 - Construir el componente y la página.
7. T-0107 - Añadir accesibilidad.
8. T-0108 - Integrar ruta y navegación.
9. T-0109 - Aplicar estilos adaptables.
10. T-0110 - Proteger credenciales.
11. T-0111 - Implementar pruebas E2E.
12. T-0112 - Verificar contrato, regresión y seguridad.

## Dependencias externas

- **CHG-0007:** endpoint de registro y errores del dominio Usuario.
- **CHG-0102:** ruta de login de destino tras un registro exitoso.
- **Frontend existente:** router, sistema de estilos, cliente HTTP y herramienta de pruebas, si están disponibles.

## Trazabilidad resumida

| Requisito | Tareas |
|---|---|
| RF-0101 | T-0102, T-0106, T-0107, T-0108, T-0111 |
| RF-0102 | T-0102, T-0103, T-0105, T-0106, T-0111 |
| RF-0103 | T-0101, T-0102, T-0104, T-0105, T-0106, T-0110, T-0111 |
| RF-0104 | T-0101, T-0105, T-0106, T-0108, T-0111 |
| RF-0105 | T-0101, T-0102, T-0103, T-0104, T-0105, T-0106, T-0109, T-0110, T-0111 |
| RF-0106 | T-0103, T-0105, T-0106, T-0107, T-0109, T-0111 |
| RNF-0101 | T-0103, T-0107, T-0111, T-0112 |
| RNF-0102 | T-0102, T-0104, T-0110, T-0111, T-0112 |
| RNF-0103 | T-0109, T-0111, T-0112 |
| RNF-0104 | T-0101, T-0102, T-0104, T-0105, T-0106, T-0112 |
| RNF-0105 | T-0104, T-0105, T-0108, T-0109, T-0111, T-0112 |
