# CHG-0007 - Evidencia de validación

## Estado
`validación parcial`

La persistencia de usuarios y progreso está implementada y validada. El cambio completo permanece parcialmente pendiente de las integraciones con CHG-0008 y con las acciones de gameplay, que no existen todavía en el backend.

## Fecha de validación

2026-08-18

## Implementación validada

- Entidad de dominio `Usuario` sin acoplamiento a JPA.
- Progreso de jugador con validaciones de nivel, XP, energía y estado.
- Membresía inicial `trial` con expiración a los 7 días.
- Tabla `usuarios` mediante JPA/Hibernate.
- Email único y persistencia en H2.
- Hash de contraseñas con BCrypt.
- Repositorio de dominio y adaptador Spring Data.
- Registro, recuperación y actualización parcial del progreso.
- Respuestas HTTP sin contraseña ni `passwordHash`.
- Autorización del acceso al progreso mediante `Principal`.
- Manejo diferenciado de email duplicado, sesión ausente, acceso no autorizado, usuario inexistente y datos inválidos.

## Pruebas ejecutadas

### Suite de aceptación

**Comando:**

```text
mvnw.cmd -q -Dtest=UsuarioAcceptanceTest test
```

**Resultado:**

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

### Escenarios validados

| Escenario | Resultado | Requisitos |
|---|---|---|
| Registrar usuario con progreso y membresía inicial | aprobado | RF-0001, RF-0002 |
| Persistir la contraseña como hash BCrypt | aprobado | RF-0002 |
| Rechazar email duplicado sin crear otro usuario | aprobado | RF-0001, RNF-0004 |
| Recuperar el progreso persistido del usuario autenticado | aprobado | RF-0003, RNF-0003 |
| Actualizar parcialmente conservando campos no modificados | aprobado | RF-0004, RF-0006 |
| Rechazar energía inválida y conservar el estado confirmado | aprobado | RF-0004, RNF-0002 |
| Bloquear sesión ausente y acceso al progreso de otro usuario | aprobado | RF-0003, RNF-0001 |
| Devolver usuario inexistente sin crear registros | aprobado | RF-0003, RNF-0004 |

Nota: el escenario de registro y hash se valida en una misma prueba de aceptación, por lo que la tabla contiene ocho comprobaciones funcionales dentro de siete métodos de prueba.

### Compilación

**Comando:**

```text
mvnw.cmd clean -DskipTests compile
```

**Resultado:** `BUILD SUCCESS`

## Cobertura de requisitos

| Requisito | Estado | Evidencia |
|---|---|---|
| RF-0001 - Crear registro persistente | validado | Registro, valores iniciales, email único y tabla `usuarios` |
| RF-0002 - Proteger contraseña | validado | Hash BCrypt y ausencia de credenciales en respuestas |
| RF-0003 - Recuperar usuario y progreso | validado parcialmente | Recuperación mediante `Principal`; login completo depende de CHG-0008 |
| RF-0004 - Actualizar datos persistentes | validado | Actualización parcial y validación de energía |
| RF-0005 - Guardado automático | pendiente | Requiere acciones de gameplay y cierre de sesión de CHG-0008 |
| RF-0006 - Frontera de persistencia | validado | Interfaz `UsuarioRepository` y adaptador JPA |
| RNF-0001 - Seguridad y privacidad | validado parcialmente | Autorización y exclusión de credenciales; tokens dependen de CHG-0008 |
| RNF-0002 - Integridad y atomicidad | validado parcialmente | Validación y conservación del último estado; faltan escenarios de fallo de almacenamiento |
| RNF-0003 - Persistencia entre sesiones/dispositivos | validado parcialmente | Persistencia en base de datos; falta integración con sesiones reales |
| RNF-0004 - Trazabilidad de errores | validado | Respuestas estables para errores cubiertos |

## Pendientes y limitaciones

- Integrar el guardado antes del cierre de sesión cuando CHG-0008 esté disponible.
- Conectar el guardado automático con acciones importantes de gameplay.
- Validar tokens reales, expiración de sesiones y carga durante un login real.
- Añadir escenarios de fallo de almacenamiento y atomicidad con una base de datos de integración.
- Aprobar formalmente `requirements.md` y actualizar el estado del cambio según el proceso SDD.

## Conclusión

La base de persistencia de usuarios de CHG-0007 funciona y sus siete pruebas de aceptación pasan correctamente. No se debe marcar el cambio como completamente cerrado hasta resolver las dependencias y pendientes indicados.
