# CHG-0006 - Jornadas sincronizadas con partidos reales

**Estado**: `aprobado`  
**Fecha**: 2026-08-24  
**Dependencia**: CHG-0001 (Integración de estadísticas reales desde API-Football)

---

## 1. Requisitos Funcionales

### RF-0001: Sincronización de jornadas desde API-Football

**Descripción**:  
El sistema debe obtener jornadas reales de API-Football (por liga, temporada) y crear entidades de jornada internas que reflejen exactamente las jornadas reales.

**Actores**:
- Sistema (sincronización automática)
- API-Football (fuente de datos)

**Precondiciones**:
- CHG-0001 debe estar completado (acceso a API-Football)
- Base de datos PostgreSQL disponible
- Tabla `jornadas` debe existir

**Flujo principal**:
1. Sistema obtiene lista de jornadas reales (API-Football: `/fixtures?league=X&season=Y`)
2. Para cada jornada real:
   - Verifica si jornada interna ya existe (por `roundNumber`)
   - Si no existe: crea nueva entidad `Jornada` con datos reales
   - Si existe: actualiza estado de la jornada
3. Registra resultado en tabla `sync_logs` con estado `SUCCESS` o `FAILURE`

**Criterios de Aceptación**:

#### CA-0001.1: Estructura de datos de Jornada
```gherkin
Dado que existe una jornada real en API-Football con:
  | Campo       | Valor                    |
  | Round       | 10                       |
  | League      | La Liga (liga_id=775)    |
  | Season      | 2024/2025                |
  | Status      | Not Started              |

Cuando el sistema sincroniza jornadas desde API-Football

Entonces se crea entidad Jornada interna con campos:
  | Campo          | Tipo     | Valor requerido |
  | id             | UUID     | Generado        |
  | roundNumber    | Integer  | 10              |
  | league         | String   | LaLiga          |
  | season         | Integer  | 2024            |
  | status         | Enum     | NOT_STARTED     |
  | matchCount     | Integer  | > 0             |
  | createdAt      | Instant  | Ahora           |
  | synchronizedAt | Instant  | Ahora           |
```

#### CA-0001.2: Estado de jornada sincronizada correctamente
```gherkin
Dado que el sistema crea una Jornada interna a partir de datos reales

Cuando verifica el estado de la jornada

Entonces el estado debe coincidir con el estado real de API-Football:
  | Estado Real      | Estado Interno  |
  | Not Started      | NOT_STARTED     |
  | In Progress      | IN_PROGRESS     |
  | Match Finished   | FINISHED        |
  | Postponed        | POSTPONED       |
```

#### CA-0001.3: Identidad única por jornada
```gherkin
Dado que existen múltiples jornadas (round 1, 2, 3, etc.)

Cuando el sistema sincroniza jornadas

Entonces cada Jornada tiene identidad única por (league + season + roundNumber):
  - La Liga, 2024, Round 10 → ID único
  - La Liga, 2024, Round 11 → ID único (diferente)
  - Premier League, 2024, Round 10 → ID único (diferente)
```

#### CA-0001.4: Sincronización incremental
```gherkin
Dado que la jornada Round 10 ya existe en la BD con estado NOT_STARTED

Cuando API-Football reporta que Round 10 ahora está IN_PROGRESS

Entonces el sistema actualiza el estado sin crear duplicados:
  - Jornada.id permanece igual
  - Jornada.status se actualiza a IN_PROGRESS
  - Jornada.synchronizedAt se actualiza a ahora
  - No se crea una nueva Jornada
```

---

### RF-0002: Bloqueo de partidos fuera de jornada real

**Descripción**:  
El sistema debe impedir que un jugador juegue partidos cuando no existe una jornada real activa o cuando la jornada está postponed/cancelada.

**Actores**:
- Jugador (intenta jugar un partido)
- Sistema (valida disponibilidad de jornada)

**Precondiciones**:
- Tabla `jornadas` debe existir y estar sincronizada
- Tabla `partidos` debe existir
- Cada partido debe tener referencia a `jornada_id`

**Flujo principal**:
1. Jugador intenta jugar un partido
2. Sistema verifica si existe jornada real activa que contenga ese partido
3. Si jornada status = `NOT_STARTED`: bloquea, retorna error
4. Si jornada status = `IN_PROGRESS`: permite
5. Si jornada status = `FINISHED`: bloquea, retorna error
6. Si jornada status = `POSTPONED`: bloquea, retorna error

**Criterios de Aceptación**:

#### CA-0002.1: Bloqueo en jornada NOT_STARTED
```gherkin
Dado que existe una jornada real con status NOT_STARTED
  | roundNumber | 10 |
  | league      | LaLiga |
  | status      | NOT_STARTED |

Y el jugador intenta jugar un partido de esa jornada

Entonces el sistema rechaza la acción con:
  | Campo        | Valor                                      |
  | httpStatus   | 400 Bad Request                            |
  | errorCode    | MATCH_BLOCKED_ROUND_NOT_STARTED            |
  | message      | "La jornada 10 aún no ha comenzado"        |
```

#### CA-0002.2: Permiso en jornada IN_PROGRESS
```gherkin
Dado que existe una jornada real con status IN_PROGRESS
  | roundNumber | 10 |
  | status      | IN_PROGRESS |

Y el jugador intenta jugar un partido de esa jornada

Entonces el sistema permite la acción:
  | Resultado    | Valor           |
  | httpStatus   | 200 OK          |
  | MatchPlayed  | true            |
  | partidoGuardado | true         |
```

#### CA-0002.3: Bloqueo en jornada FINISHED
```gherkin
Dado que existe una jornada real con status FINISHED
  | roundNumber | 10 |
  | status      | FINISHED |

Y el jugador intenta jugar un partido de esa jornada

Entonces el sistema rechaza la acción con:
  | Campo        | Valor                                      |
  | httpStatus   | 400 Bad Request                            |
  | errorCode    | MATCH_BLOCKED_ROUND_FINISHED               |
  | message      | "La jornada 10 ya ha finalizado"           |
```

#### CA-0002.4: Bloqueo en jornada POSTPONED
```gherkin
Dado que existe una jornada real con status POSTPONED
  | roundNumber | 10 |
  | status      | POSTPONED |

Y el jugador intenta jugar un partido de esa jornada

Entonces el sistema rechaza la acción con:
  | Campo        | Valor                                      |
  | httpStatus   | 400 Bad Request                            |
  | errorCode    | MATCH_BLOCKED_ROUND_POSTPONED              |
  | message      | "La jornada 10 ha sido aplazada"           |
```

#### CA-0002.5: Bloqueo si no existe jornada real
```gherkin
Dado que el jugador intenta crear/jugar un partido
  | league      | LaLiga |
  | season      | 2024   |
  | roundNumber | 99     | (Round no sincronizado)

Y no existe Jornada interna con round=99 sincronizada desde API-Football

Entonces el sistema rechaza la acción con:
  | Campo        | Valor                                      |
  | httpStatus   | 400 Bad Request                            |
  | errorCode    | MATCH_BLOCKED_ROUND_NOT_FOUND              |
  | message      | "Round 99 no existe en las jornadas reales"|
```

---

### RF-0003: Actualización automática de estado de jornadas

**Descripción**:  
El sistema debe actualizar automáticamente el estado de jornadas cada cierto tiempo (diariamente) para reflejar cambios en API-Football (ej: NOT_STARTED → IN_PROGRESS).

**Actores**:
- Sistema (job programado)
- API-Football (fuente de datos)

**Precondiciones**:
- CHG-0001 debe estar completado
- Debe existir mecanismo de scheduling (@Scheduled)
- Tabla `jornadas` debe existir

**Flujo principal**:
1. Job programado se ejecuta diariamente (ej: 03:00 UTC)
2. Sistema obtiene lista de jornadas desde API-Football para todas las ligas/temporadas activas
3. Para cada jornada real:
   - Obtiene estado actual de API-Football
   - Compara con estado interno
   - Si cambió: actualiza estado en BD
   - Registra cambio en `sync_logs` con detalles

**Criterios de Aceptación**:

#### CA-0003.1: Job programado ejecuta diariamente
```gherkin
Dado que existe una tarea programada de sincronización de jornadas

Cuando se ejecuta el job programado

Entonces:
  - Se ejecuta una vez cada 24 horas
  - El horario es consistente (ej: 03:00 UTC)
  - No bloquea otras operaciones del sistema
```

#### CA-0003.2: Transición de estado NOT_STARTED → IN_PROGRESS
```gherkin
Dado que existe una Jornada interna con:
  | Field        | Valor       |
  | roundNumber  | 10          |
  | status       | NOT_STARTED |
  | updatedAt    | Hace 12h    |

Y API-Football reporta que Round 10 ahora está IN_PROGRESS

Cuando se ejecuta el job de sincronización

Entonces:
  | Campo              | Valor       |
  | jornada.status     | IN_PROGRESS |
  | jornada.updatedAt  | Ahora       |
  | sync_log.status    | SUCCESS     |
  | sync_log.action    | ROUND_STARTED |
```

#### CA-0003.3: Transición de estado IN_PROGRESS → FINISHED
```gherkin
Dado que existe una Jornada interna con:
  | Field        | Valor       |
  | roundNumber  | 10          |
  | status       | IN_PROGRESS |

Y API-Football reporta que Round 10 está FINISHED (todos los partidos terminados)

Cuando se ejecuta el job de sincronización

Entonces:
  | Campo            | Valor    |
  | jornada.status   | FINISHED |
  | jornada.updatedAt| Ahora    |
```

#### CA-0003.4: Detección de postponed/cancelado
```gherkin
Dado que existe una Jornada interna con:
  | Field        | Valor       |
  | roundNumber  | 10          |
  | status       | NOT_STARTED |

Y API-Football reporta que Round 10 está POSTPONED (ej: por clima)

Cuando se ejecuta el job de sincronización

Entonces:
  | Campo            | Valor    |
  | jornada.status   | POSTPONED|
  | sync_log.reason  | "Clima adverso" (si está disponible) |
```

#### CA-0003.5: Registro de cambios en sync_logs
```gherkin
Dado que el job sincroniza jornadas

Cuando ocurre un cambio de estado

Entonces se registra en sync_logs:
  | Campo           | Valor                              |
  | sync_timestamp  | Ahora                              |
  | league          | LaLiga                             |
  | season          | 2024                               |
  | roundNumber     | 10                                 |
  | oldStatus       | NOT_STARTED                        |
  | newStatus       | IN_PROGRESS                        |
  | status          | SUCCESS                            |
  | errors          | null                               |
```

---

### RF-0004: Asociación de partidos a jornadas reales

**Descripción**:  
Cada partido interno debe estar asociado a una Jornada real. El sistema debe garantizar que no existan partidos "huérfanos" sin jornada real de referencia.

**Actores**:
- Sistema (crea/valida partidos)
- Jugador (visualiza partidos disponibles)

**Precondiciones**:
- RF-0001, RF-0002, RF-0003 completados
- Tabla `partidos` debe tener referencia a `jornadas` (FK)
- Tabla `jornadas` debe estar sincronizada

**Flujo principal**:
1. Al crear/sincronizar un partido, sistema valida que existe jornada real
2. Si jornada no existe o está POSTPONED: rechaza creación
3. Partido se vincula con `jornada_id`
4. Validación se ejecuta en capas: Dominio, Aplicación, Base de datos

**Criterios de Aceptación**:

#### CA-0004.1: Validación de jornada al crear partido
```gherkin
Dado que un usuario intenta crear un partido para:
  | Campo       | Valor      |
  | league      | LaLiga     |
  | season      | 2024       |
  | roundNumber | 10         |

Cuando no existe Jornada interna sincronizada con esos datos

Entonces el sistema rechaza con:
  | Campo      | Valor                           |
  | httpStatus | 400 Bad Request                |
  | errorCode  | JORNADA_NOT_FOUND               |
  | message    | "Jornada 10 LaLiga 2024 no existe" |
```

#### CA-0004.2: Referencia FK a jornada_id
```gherkin
Dado que existe una Jornada interna sincronizada:
  | id          | uuid-1234   |
  | roundNumber | 10          |
  | status      | IN_PROGRESS |

Y se crea un Partido en esa jornada

Entonces el Partido contiene:
  | Campo      | Valor     |
  | jornada_id | uuid-1234 |
  | partido_id | uuid-5678 |
```

#### CA-0004.3: Imposible crear partido sin jornada
```gherkin
Dado que se intenta insertar un Partido en la BD
  | jornada_id | NULL (o invalid) |

Cuando se ejecuta INSERT en tabla partidos

Entonces la BD rechaza con:
  | Tipo              | FOREIGN KEY CONSTRAINT VIOLATION |
  | Mensaje           | "jornada_id must reference existing jornada" |
  | Resultado         | Transacción ROLLBACK             |
```

#### CA-0004.4: Visualización de partidos disponibles por jornada
```gherkin
Dado que el jugador visualiza partidos disponibles

Cuando filtra por liga=LaLiga, temporada=2024

Entonces ve solo partidos de jornadas con status IN_PROGRESS:
  - Oculta partidos de jornadas NOT_STARTED
  - Oculta partidos de jornadas FINISHED
  - Oculta partidos de jornadas POSTPONED
```

---

## 2. Requisitos No Funcionales

### RNF-0001: Rendimiento de sincronización

**Requisito**:  
La sincronización de jornadas debe completarse en menos de 5 segundos para todas las ligas principales (LaLiga, Premier League, Serie A, Bundesliga, Ligue 1).

**Métrica**:
- Tiempo de sincronización ≤ 5 segundos (para ~380 jornadas totales)
- P99 latency ≤ 7 segundos

**Verificación**:
```
CUANDO se ejecuta: SELECT * FROM jornadas WHERE league='LaLiga' AND season=2024
ENTONCES retorna resultados en < 100ms
```

---

### RNF-0002: Disponibilidad de API-Football

**Requisito**:  
Si API-Football está no disponible, el sistema debe:
- No bloquear partidos inmediatamente
- Usar último estado conocido de jornadas (hasta 24 horas)
- Registrar error en `sync_logs` con status `FAILURE`
- Reintentar cada 15 minutos

**Métrica**:
- Reintentos máximo 3 intentos en 45 minutos
- Después de 3 fallos: cambiar a modo "último estado conocido"

---

### RNF-0003: Consistencia transaccional

**Requisito**:  
Actualización de estado de jornada debe ser atómica:
- Si falla cambio de estado: rollback completo
- No se pueden quedar registros parciales en `sync_logs`
- Si falla inserción de nueva jornada: se rechaza todo

**Verificación**:
```sql
-- Antes de sincronizar
SELECT COUNT(*) FROM jornadas WHERE roundNumber=10; -- N registros

-- Después de fallo de sincronización
SELECT COUNT(*) FROM jornadas WHERE roundNumber=10; -- N registros (sin cambios)
```

---

### RNF-0004: Auditoría completa

**Requisito**:  
Cada cambio de estado de jornada debe quedar registrado en `sync_logs`:
- Quién (sistema/API-Football)
- Qué (roundNumber, oldStatus, newStatus)
- Cuándo (timestamp UTC)
- Resultado (SUCCESS/FAILURE)
- Errores (si aplica)

**Retención**:  
Mínimo 90 días de logs

---

### RNF-0005: Compatibilidad con CHG-0001

**Requisito**:  
CHG-0006 debe funcionar correctamente junto con CHG-0001 (estadísticas reales):
- Jornadas sincronizadas por CHG-0006
- Jugadores y estadísticas sincronizadas por CHG-0001
- Partidos jugados registran estadísticas de jugadores reales

**Integración**:
```
CHG-0001 (Estadísticas) + CHG-0006 (Jornadas) 
→ Partidos con datos reales completos
```

---

### RNF-0006: Seguridad de datos

**Requisito**:  
- Solo el sistema puede sincronizar jornadas (sin endpoint público)
- Cambios de estado de jornada se validan antes de aplicar
- Logs de sincronización no exponen datos sensibles de API-Football

---

## 3. Matriz de Trazabilidad

| Requisito | Criterios de Aceptación | Dominio | Tarea |
|-----------|------------------------|---------|-------|
| RF-0001 | CA-0001.1, CA-0001.2, CA-0001.3, CA-0001.4 | Jornadas | T-0001, T-0002 |
| RF-0002 | CA-0002.1, CA-0002.2, CA-0002.3, CA-0002.4, CA-0002.5 | Partidos | T-0003, T-0004 |
| RF-0003 | CA-0003.1, CA-0003.2, CA-0003.3, CA-0003.4, CA-0003.5 | Jornadas | T-0005, T-0006 |
| RF-0004 | CA-0004.1, CA-0004.2, CA-0004.3, CA-0004.4 | Partidos | T-0007, T-0008 |

---

## 4. Definiciones

### Jornada (Round)
Conjunto de partidos reales jugados simultáneamente o en el mismo período. Ejemplo: La Liga 2024/25 Jornada 10.

### Estado de Jornada
- **NOT_STARTED**: Jornada aún no ha comenzado
- **IN_PROGRESS**: Al menos 1 partido en juego, no todos terminados
- **FINISHED**: Todos los partidos han terminado
- **POSTPONED**: Jornada fue aplazada (clima, otros eventos)

### Sincronización
Proceso de obtener datos de API-Football y actualizar estado interno en BD.

### Bloqueo de Partido
Acción del sistema de rechazar un intento de juego por jornada no disponible.

---

## 5. Supuestos

1. API-Football proporciona endpoint `/fixtures?league=X&season=Y` con estado de jornada
2. BD PostgreSQL tiene tabla `jornadas` con estructura definida
3. CHG-0001 está completado (acceso confiable a API-Football)
4. No existen jornadas simuladas en el videojuego (todas son reales)
5. Sistema puede usar @Scheduled para tareas programadas

---

## 6. Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|--------|-----------|
| API-Football retorna datos incompletos o incorrectos | Media | Alto | Usar último estado conocido; validar estructura |
| Jornada desaparece de API-Football (retroactivamente) | Baja | Medio | Registrar en logs; alertar admins |
| Partidos jugados después de jornada FINISHED | Alta | Alto | Bloqueo en RF-0002; validación en capas |
| Inconsistencia entre estadísticas (CHG-0001) y jornadas (CHG-0006) | Media | Medio | Sincronización coordinada; transacciones ACID |

---

## 7. Criterio de Completitud

Este CHG se considera **completado** cuando:

✅ Todas las CA de RF-0001 a RF-0004 pasan en pruebas de aceptación  
✅ RNF-0001 a RNF-0006 se verifican en ambiente de staging  
✅ Sync logs contienen mínimo 5 ciclos de sincronización exitosos  
✅ Pruebas de bloqueo de partidos pasan (CA-0002.1 a CA-0002.5)  
✅ Documentación actualizada con ejemplos de uso  
✅ Equipo de QA da aprobación final  

---

**Próximo paso**: Generar `tasks.md` con tareas técnicas trazables a estos requisitos.
