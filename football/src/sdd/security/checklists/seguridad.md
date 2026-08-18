## Lista de verificación de revisión de seguridad

Utilice esta lista de verificación siempre que un cambio pueda afectar la autenticación, la autorización, los secretos, los datos de usuario, la disponibilidad o el cumplimiento normativo.
Este archivo es ** normativo ** : define lo que DEBE comprobarse antes de fusionar los cambios.
Es leido por humanos y por GitHub Copilot como contexto SDD.

---

## 0. Global - Proceso de desarrollo (SIEMPRE REQUERIDO)
Se aplica a ** todos los proyectos ** , independientemente del cliente o la normativa.

### Autenticación y autorización
 - [ ] No se introduce ningun nuevo mecanismo de inicio de sesion, token o credenciales sin revisión.
 - [ ] Los puntos finales protegidos declaran explicitamente el metodo de autenticación y los ambitos/roles requeridos.
 - [ ] La autorización a nivel de objeto se aplica en ** cada ** lectura y escritura.
 - [ ] Un usuario no puede acceder ni modificar los datos de otro usuario a menos que se le permita explicitamente.
 - [ ] Las acciones administrativas o privilegiadas están aisladas y requieren roles elevados.

### Secretos y configuración
 - [ ] No se han confirmado secretos, tokens, claves privadas, certificados ni archivos .env' reales
 - [ ] Los secretos se cargan unicamente desde variables de entorno o un gestor de secretos.
 - [ ] No aparecen credenciales, claves ni URL internas en los registros ni en los mensajes de error.
 - [ ] Las credenciales predeterminadas no están presentes en ninguna parte del código fuente.

### Entorno y ciclo de vida del desarrollo de software
 - [ ] No se utilizan datos de producción en entornos de desarrollo o prueba.
 - [ ] La separación de entornos (desarrollo/pruebas/producción) se conserva en la configuración y en CI/CD.
 - [ ] Los puntos finales de depuración, prueba o solo para administradores no se exponen en producción.
 - [ ] Se revisaron las dependencias (incluido el código generado por IA) en busca de vulnerabilidades conocidas.

### Seguridad de entrada/salida
 - [ ] Las entradas se validan (tipo, rango, formato); los campos inesperados se rechazan.
 - [ ] Las salidas no exponen campos internos, identificadores ni metadatos confidenciales.
 - [ ] Los mensajes de error no filtran rastros de pila ni detalles de implementación interna.

### Registro y evidencia
 - [ ] Se registran las acciones relevantes para la seguridad (autenticación, acceso , cambios de privilegios).
 - [ ] Los registros contienen marcas de tiempo e identidad del actor.
 - [ ] Los registros no contienen secretos ni cargas utiles sensibles.

### Codificación asistida por IA
 - [ ] El código generado por IA fue revisado en cuanto a seguridad, corrección y licencia.
 - [ ] No se incluyeron datos confidenciales ni logica propietaria en las indicaciones
 - [ ] El código generado no elude los controles de seguridad existentes.

## 1. ISO/IEC 27001 - Entregable
Aplique esta política si el ** producto ** debe cumplir con las expectativas de la norma ISO/IEC 27001.
 - [ ] Los controles de seguridad se aplican en el producto, no solo en la infraestructura.
 - [ ] El control de acceso sigue el principio de mínimo privilegio y la separación de roles .
 - [ ] Los datos confidenciales se cifran en reposo y en tránsito .
 - [ ] Los eventos relevantes para la seguridad se registran y son auditables.
 - [ ] No existen credenciales codificadas ni valores predeterminados inseguros en el producto entregable.
 - [ ] Existe documentación de seguridad (roles, flujos de datos, supuestos de seguridad).

---

## 2. ENS Alto - Entregable

Aplicar si el ** producto ** debe cumplir con ENS Nivel Alto.

 - [ ] Se aplica una autenticación fuerte a todos los usuarios que acceden a datos de alto nivel.
 - [ ] La autenticación multifactor (MFA) es obligatoria para los administradores y los roles privilegiados.
 - [ ] Cada usuario tiene un identificador único e intransferible .
 - [ ] Se aplica RBAC; los privilegios no se combinan por defecto.
 - [ ] Todas las comunicaciones requieren TLS 1.2+ (se prefiere TLS 1.3 ).
 - [ ] Los datos confidenciales se cifran en reposo (bases de datos, copias de seguridad, registros).
 - [ ] Los accesos y los eventos de seguridad se registran con marcas de tiempo.
 - [ ] El acceso administrativo remoto está restringido y protegido.

## 3. ISO/IEC 42001 - Entregable (Sistemas de IA)
Aplicar si el ** producto incluye componentes de IA **
 - [ ] Los componentes de IA se identifican y documentan explícitamente.
 - [ ] Las entradas, salidas y decisiones de la IA son rastreables.
 - [ ] Existen medidas de mitigación de riesgos para el mal uso o los modos de fallo de la IA.
 - [ ] Existe supervisión humana para las decisiones de IA de alto impacto.
 - [ ] Los registros de IA están disponibles para auditoría y monitoreo.
 - [ ] Los usuarios son informados cuando interactúan con la salida generada por IA.

---

## 4. RGPD - Entregable

Aplicar si el ** producto procesa datos personales **

 - [ ] La recopilación de datos personales se limita a lo estrictamente necesario.
 - [ ] No se exponen datos personales en los registros, URL o almacenamiento del lado del cliente.
 - [ ] Los usuarios pueden acceder, corregir, exportar y eliminar sus datos personales.
 - [ ] El consentimiento (si es necesario) es explícito y revocable.
 - [ ] Los datos personales estan cifrados en reposo y en transito.
 - [ ] Se implementan y se hacen cumplir las reglas de retención y eliminación de datos.
 - [ ] El acceso a los datos personales se registra y es rastreable.

---

## 5. Ley de IA de la UE: entregable

Aplicar si el ** producto incluye sistemas de IA sujetos a la Ley de IA **.

 - [ ] Se identifica la categoria de riesgo de IA (riesgo minimo / limitado / alto). 
 - [ ] Los sistemas de IA de alto riesgo implementan gestión y monitoreo de riesgos.
 - [ ] Los usuarios son informados cuando el contenido o las decisiones son generados por IA.
 - [ ] La intervención humana es posible cuando sea necesaria.
 - [ ] Las operaciones de IA se registran para la trazabilidad y el seguimiento posterior a la comercialización.

---

## 6. NIS2 - Entregable

Solicite si el ** producto presta servicios esenciales o importantes **.
 
 - [ ] La configuración segura se aplica por defecto.
 - [ ] No hay contraseñas predeterminadas ni servicios innecesarios habilitados.
 - [ ] Los registros de seguridad ayudan a detectar y reportar incidentes.
 - [ ] Los mecanismos de copia de seguridad y recuperación están documentados y son  comprobables.
 - [ ] Las dependencias externas están documentadas y se minimizan.
 - [ ] El producto admite la aplicación oportuna de parches de seguridad.

---

## 7. DORA - Entregable

Solicite si el ** producto admite entidades financieras ** .

 - [ ] No existe un único punto de fallo para las funciones críticas.
 - [ ] Se garantiza la integridad de los datos financieros (operaciones atómicas , validación).
 - [ ] Existen registros de auditoría para las operaciones financieras.
 - [ ] Los datos financieros confidenciales están cifrados y no se registran en texto plano.
 - [ ] Los procedimientos de copia de seguridad, restauración y conmutación por error están respaldados y documentados.
 - [ ] Las dependencias de terceros están documentadas para la gestión de riesgos de las TIC.

---

## 8. HIPAA - Entregable

Aplicar si el ** producto procesa información de salud protegida (PHI) ** .


 - [ ] Se exigen identificadores de usuario únicos; ninguna cuenta compartida accede a información de salud protegida (PHI).
 - [ ] La MFA se utiliza para roles privilegiados o de acceso a información de salud protegida (PHI).
 - [ ] La información de salud protegida (PHI) está cifrada en reposo y en tránsito.
 - [ ] Todo acceso a la información de salud protegida (PHI) se registra y se conserva.
 - [ ] Los registros son a prueba de manipulaciones y tienen control de acceso.
 - [ ] La información de salud protegida (PHI) no se utiliza en entornos que no sean de producción a menos que esté anonimizada.
 - [ ] Se admite la exportación y eliminación segura de información de salud protegida (PHI).

---

## 9. PCI DSS - Entregable

Aplicar si el ** producto maneja datos de tarjetas de pago **.


 - [ ] Los datos CVV, PIN y de la banda magnética nunca se almacenan .
 - [ ] PAN está cifrado en reposo y enmascarado cuando se muestra.
 - [ ] Los datos de pago se transmiten únicamente a través de TLS 1.2+.
 - [ ] El acceso a los datos de la tarjeta está estrictamente limitado y registrado.
 - [ ] El acceso de administrador a los datos de pago requiere una autenticación fuerte.
 - [ ] Se eliminan las credenciales predeterminadas y las configuraciones inseguras.
 - [ ] Preferimos la tokenización o proveedores de pago externos con certificación PCI.

---

## Regla de uso

Si ** alguno **
de los elementos de la lista de verificación anterior se ve afectado por un cambio:

 - [ ] El cambio ** no debe fusionarse ** hasta que se aprueben todas las secciones relevantes.
 - [ ] Las secciones no aplicables deben ser reconocidas explícitamente.
 - [ ] Este archivo es la puerta de seguridad autorizada para el repositorio.