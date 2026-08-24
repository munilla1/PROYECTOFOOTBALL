# CHG-0001 - Requisitos: Integración de estadísticas reales desde API-Football

## Estado
`borrador`

## Trazabilidad
**Cambio**: CHG-0001  
**Proposal**: [proposal.md](proposal.md)  
**Próximo**: tasks.md

---

## 1. Requisitos Funcionales

### RF-0001: Consumo de endpoints de API-Football

**Descripción**  
El sistema debe consumir datos de jugadores y partidos desde los endpoints públicos de API-Football de forma confiable y autorizada.

**Criterios de Aceptación**

#### CA-0001.1: Autenticación con API-Football
- **Descripción**: El sistema se autentica usando API key en requests a API-Football.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que existe una API key válida configurada en `application.properties`
  - **Cuando** el sistema intenta conectar a `https://api-football-v3.p.rapidapi.com/`
  - **Entonces** la request incluye headers `X-RapidAPI-Key` y `X-RapidAPI-Host`
  - **Y** la respuesta es exitosa (HTTP 200)

#### CA-0001.2: Obtener datos de jugadores
- **Descripción**: El sistema obtiene datos de jugadores (id, nombre, equipo, posición, estadísticas) desde endpoint `/players`.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que existe conexión a API-Football
  - **Cuando** se ejecuta una request a `/players` con parámetros `league` y `season`
  - **Entonces** se recibe una lista JSON con jugadores que incluye: `player_id`, `player_name`, `team_id`, `position`, `statistics` (goles, asistencias, pases, etc.)
  - **Y** el sistema mapea correctamente cada campo

#### CA-0001.3: Obtener datos de partidos
- **Descripción**: El sistema obtiene datos de partidos (id, fecha, equipos, jornada, estado) desde endpoint `/fixtures`.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que existe conexión a API-Football
  - **Cuando** se ejecuta una request a `/fixtures` con parámetros `league` y `season`
  - **Entonces** se recibe una lista JSON con partidos que incluye: `fixture_id`, `fixture_date`, `teams`, `round`, `status`, `goals`
  - **Y** el sistema identifica correctamente los equipos local y visitante

#### CA-0001.4: Manejo de rate limits
- **Descripción**: El sistema respeta los rate limits de API-Football e implementa reintentos exponenciales.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que API-Football devuelve HTTP 429 (Too Many Requests)
  - **Cuando** el sistema procesa la respuesta
  - **Entonces** aguarda según el header `Retry-After`
  - **Y** reintenta la request después del delay
  - **Y** reintentos máximo 3 veces antes de fallar

#### CA-0001.5: Manejo de errores de conectividad
- **Descripción**: El sistema maneja timeouts y conexiones rechazadas con reintentos.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que la conectividad a API-Football falla temporalmente
  - **Cuando** se ejecuta una request y obtiene timeout o conexión rechazada
  - **Entonces** reintenta con backoff exponencial (1s, 2s, 4s)
  - **Y** registra la falla en logs
  - **Y** tras 3 reintentos fallidos, aborta y notifica al administrador

---

### RF-0002: Mapeo de estadísticas reales a entidades internas

**Descripción**  
El sistema convierte estadísticas de API-Football a estructuras internas alineadas con los dominios Jugador, Estadísticas y Progresión.

**Criterios de Aceptación**

#### CA-0002.1: Mapear datos de jugador
- **Descripción**: Cada jugador de API-Football se convierte a entidad interna `Player` con campos relevantes.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que se recibe un JSON de jugador desde API-Football:
    ```json
    {
      "player": {
        "id": 1234,
        "name": "Cristiano Ronaldo",
        "firstname": "Cristiano",
        "lastname": "Ronaldo",
        "age": 39,
        "birth": "1985-02-05",
        "nationality": "Portugal",
        "height": "187 cm",
        "weight": "84 kg"
      },
      "statistics": [
        {
          "team": {"id": 1, "name": "Manchester United"},
          "league": {"id": 39, "season": 2023},
          "games": {"appearances": 30, "minutes": 2400},
          "goals": {"total": 18, "assists": 3},
          "passes": {"total": 800, "accuracy": 87},
          "tackles": {"total": 15},
          "dribbles": {"attempts": 50, "success": 35},
          "fouls": {"committed": 20, "drawn": 25}
        }
      ]
    }
    ```
  - **Cuando** se mapea a entidad interna
  - **Entonces** se crea o actualiza `Player` con:
    - `externalId` = 1234
    - `name` = "Cristiano Ronaldo"
    - `position` = detectado desde equipo/historial
    - `team` = referencia a entidad `Team`
    - `age` = 39
    - `nationality` = "Portugal"
    - `realStats` = objeto anidado con estadísticas

#### CA-0002.2: Mapear estadísticas de rendimiento
- **Descripción**: Estadísticas reales se normalizan a escala 0-100 para uso interno.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que un jugador tiene:
    - `goals.total` = 18
    - `passes.accuracy` = 87
    - `dribbles.success` = 70%
    - `tackles.total` = 15
  - **Cuando** se normalizan para el dominio
  - **Entonces** se calcula un score de rendimiento:
    - Goles → escala (máximo histórico)
    - Pases precisos → 87 (es porcentaje)
    - Regates exitosos → 70 (es porcentaje)
    - Tackles → escala (máximo por posición)
  - **Y** se almacena en `realStats.performanceScore` (0-100)

#### CA-0002.3: Validar integridad de datos mapeados
- **Descripción**: Cada dato mapeado cumple validaciones básicas antes de persistencia.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que se mapean datos de un jugador
  - **Cuando** se valida la entidad interna
  - **Entonces** se verifica:
    - `name` no está vacío
    - `age` está entre 16 y 50
    - `externalId` es único
    - `team` referencia una entidad existente
    - Estadísticas no contienen valores negativos
    - Porcentajes están entre 0-100
  - **Y** si alguna validación falla, se registra error y se rechaza el documento

---

### RF-0003: Sincronización automática tras cada jornada real

**Descripción**  
El sistema ejecuta sincronización automática cuando concluye una jornada real de fútbol para obtener y procesar nuevas estadísticas.

**Criterios de Aceptación**

#### CA-0003.1: Detectar conclusión de jornada
- **Descripción**: El sistema identifica cuándo han concluido todos los partidos de una jornada.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que la jornada actual es "Round 1" de LaLiga 2023-24
  - **Cuando** se ejecuta job de sincronización
  - **Entonces** consulta `/fixtures` con `league=135&season=2023&round=1`
  - **Y** verifica que todos los partidos tengan `status` = "Match Finished"
  - **Y** solo entonces procede a sincronizar estadísticas

#### CA-0003.2: Ejecutar sincronización programada
- **Descripción**: La sincronización se ejecuta en horarios predefinidos (ej. diariamente a las 03:00 UTC).
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que existe un cron job configurado en `application.properties`: `cron.sync.estadisticas=0 0 3 * * *`
  - **Cuando** se alcanza el horario programado
  - **Entonces** el sistema dispara `SyncEstadisticasJob`
  - **Y** registra en logs: `[SYNC_INICIO] Sincronización de estadísticas iniciada`
  - **Y** ejecuta sincronización completa

#### CA-0003.3: Actualizar jugadores tras jornada
- **Descripción**: Se obtienen nuevas estadísticas de jugadores y se actualizan documentos.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que Cristiano Ronaldo había anotado 18 goles antes de la jornada
  - **Cuando** se sincroniza tras jornada 10
  - **Entonces** se consulta `/players` para obtener estadísticas actualizadas
  - **Y** si ahora tiene 19 goles, `Player.realStats.goals` se actualiza a 19
  - **Y** se registra timestamp de última actualización

#### CA-0003.4: Manejar sincronización fallida
- **Descripción**: Si la sincronización falla, se registra y se reintenta más tarde.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que durante sincronización hay error en API-Football
  - **Cuando** el job captura la excepción
  - **Entonces** registra: `[SYNC_ERROR] Sincronización fallida: timeout en /players`
  - **Y** programa reintento automático en 1 hora
  - **Y** notifica al administrador si falla 3 veces consecutivas

#### CA-0003.5: Evitar duplicados en sincronización
- **Descripción**: Si se ejecuta sincronización dos veces para la misma jornada, no se crean duplicados.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que ya se sincronizó Round 1 hace 2 horas
  - **Cuando** se ejecuta nuevamente sincronización para Round 1
  - **Entonces** el sistema detecta que `lastSyncRound` = 1
  - **Y** evita re-procesar los mismos datos
  - **O** si realmente hay cambios, actualiza documentos existentes (no crea nuevos)

---

### RF-0004: Persistencia de estadísticas reales en Firestore

**Descripción**  
Todas las estadísticas reales se persisten en Firestore de forma organizada, indexada y con trazabilidad.

**Criterios de Aceptación**

#### CA-0004.1: Crear colección `players` con estadísticas reales
- **Descripción**: Cada jugador se persiste en Firestore con estructura normalizada.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que se sincroniza Cristiano Ronaldo desde API-Football
  - **Cuando** se persiste en Firestore
  - **Entonces** se crea/actualiza documento en `players/{externalId}` con:
    ```json
    {
      "externalId": 1234,
      "name": "Cristiano Ronaldo",
      "team": "Manchester United",
      "position": "ST",
      "age": 39,
      "nationality": "Portugal",
      "realStats": {
        "season": 2023,
        "league": "LaLiga",
        "appearances": 30,
        "goals": 18,
        "assists": 3,
        "passesAccuracy": 87,
        "dribblesSuccess": 70,
        "tackles": 15,
        "performanceScore": 82,
        "lastUpdated": "2023-11-15T03:00:00Z"
      },
      "metadata": {
        "synced": true,
        "syncedAt": "2023-11-15T03:00:00Z",
        "syncVersion": 1
      }
    }
    ```
  - **Y** cada campo es indexable

#### CA-0004.2: Crear colección `matches` con estadísticas de partidos
- **Descripción**: Cada partido se persiste con datos finales tras conclusión.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que concluyó Match: Manchester United 3-2 Arsenal
  - **Cuando** se sincroniza
  - **Entonces** se crea/actualiza documento en `matches/{fixtureId}` con:
    ```json
    {
      "fixtureId": 567890,
      "round": 10,
      "league": "Premier League",
      "season": 2023,
      "date": "2023-11-15T15:00:00Z",
      "homeTeam": "Manchester United",
      "awayTeam": "Arsenal",
      "homeTeamId": 1,
      "awayTeamId": 2,
      "finalScore": {"home": 3, "away": 2},
      "status": "Match Finished",
      "playerStats": [
        {
          "playerId": 1234,
          "playerName": "Cristiano Ronaldo",
          "team": "Manchester United",
          "goals": 1,
          "assists": 1,
          "minutesPlayed": 90,
          "rating": 8.5
        }
      ],
      "metadata": {
        "synced": true,
        "syncedAt": "2023-11-15T17:30:00Z"
      }
    }
    ```

#### CA-0004.3: Mantener historial de sincronizaciones
- **Descripción**: Cada sincronización queda registrada para auditoría.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que se ejecutó sincronización
  - **Cuando** se persisten datos
  - **Entonces** se crea documento en `syncs/{timestamp}` con:
    ```json
    {
      "timestamp": "2023-11-15T03:00:00Z",
      "status": "SUCCESS",
      "roundSynced": 10,
      "playersUpdated": 457,
      "matchesUpdated": 15,
      "errors": [],
      "duration": "2400ms"
    }
    ```
  - **Y** se puede consultar historial para auditoría

#### CA-0004.4: Garantizar consistencia de datos
- **Descripción**: Transacciones Firestore aseguran consistencia.
- **Verificable**: Sí
- **Escenario**:
  - **Dado** que se actualiza un jugador y su entrada en sincronizaciones
  - **Cuando** se persisten simultáneamente
  - **Entonces** se usa transacción Firestore
  - **Y** si una operación falla, ambas se revierten (atomicidad)
  - **Y** no hay estado inconsistente

---

## 2. Requisitos No Funcionales

### RNF-0001: Seguridad

#### RNF-0001.1: Protección de API key
- **Descripción**: API key de API-Football nunca se expone en logs, respuestas HTTP ni código.
- **Implementación**: Usar variables de entorno, no hardcodear.
- **Verificable**: Sí
  - Los logs no contienen `X-RapidAPI-Key`
  - Código no tiene API key como string literal

#### RNF-0001.2: Validación de origen de datos
- **Descripción**: Solo se procesan datos verificados de API-Football oficiales.
- **Implementación**: HTTPS + validación de certificado + checksum de respuestas.
- **Verificable**: Sí
  - Solo se conecta a `https://api-football-v3.p.rapidapi.com/`
  - Certificados SSL válidos

#### RNF-0001.3: Autorización de acceso a datos sincronizados
- **Descripción**: Solo administradores pueden acceder a logs y estadísticas crudas.
- **Implementación**: Control de acceso por roles.
- **Verificable**: Sí
  - Endpoint `/admin/sync-logs` requiere rol `ADMIN`

---

### RNF-0002: Performance

#### RNF-0002.1: Latencia de sincronización
- **Descripción**: Sincronización completa debe completarse en < 5 minutos.
- **Métrica**: Tiempo de ejecución
- **Verificable**: Sí
  - Logs registran duración: `duration: 240000ms` (4 minutos)

#### RNF-0002.2: Throughput de escritura
- **Descripción**: Sistema puede procesar > 1000 documentos por minuto en Firestore.
- **Métrica**: Documentos/minuto
- **Verificable**: Sí
  - Test de carga: Escribir 1000 jugadores en < 1 minuto

#### RNF-0002.3: Consumo de requests a API-Football
- **Descripción**: Se optimiza para usar < 100 requests por sincronización.
- **Métrica**: Requests por sincronización
- **Verificable**: Sí
  - Logs: `requests_count: 87`

---

### RNF-0003: Confiabilidad

#### RNF-0003.1: Disponibilidad de sincronización
- **Descripción**: Sistema debe completar sincronización con éxito en ≥ 95% de intentos.
- **Métrica**: Tasa de éxito
- **Verificable**: Sí
  - Monitoreo: success_rate > 0.95 en período de 30 días

#### RNF-0003.2: Recuperación ante fallos
- **Descripción**: Si falla una sincronización, sistema se recupera automáticamente.
- **Implementación**: Reintentos exponenciales, dead letter queue.
- **Verificable**: Sí
  - Test: Simular fallo de API, verificar reintento exitoso

#### RNF-0003.3: Recuperación ante datos inconsistentes
- **Descripción**: Si hay inconsistencia en datos sincronizados, se detecta y se marca.
- **Implementación**: Validación post-sincronización, checksums.
- **Verificable**: Sí
  - Flag `dataQuality: "COMPROMISED"` si hay inconsistencias

---

### RNF-0004: Mantenibilidad

#### RNF-0004.1: Logs estructurados
- **Descripción**: Todos los logs de sincronización usan formato JSON estructurado.
- **Formato**:
  ```json
  {
    "timestamp": "2023-11-15T03:00:00Z",
    "level": "INFO",
    "module": "SyncEstadisticasService",
    "action": "SYNC_INICIO",
    "round": 10,
    "traceId": "abc-123-def"
  }
  ```
- **Verificable**: Sí
  - Parsear logs con jq/LogQL

#### RNF-0004.2: Documentación de errores
- **Descripción**: Cada error tiene código único y documentación en `errores.md`.
- **Ejemplo**: `ERR_SYNC_API_TIMEOUT` → descripción + solución
- **Verificable**: Sí
  - Archivo `errores.md` existe y es exhaustivo

---

## 3. Dependencias de Requisitos

```
RF-0001 (Consumo API) 
  └─> RF-0002 (Mapeo) 
      └─> RF-0003 (Sincronización)
          └─> RF-0004 (Persistencia)
```

Todos los RNF son transversales a los RF.

---

## 4. Criterios de Validación del Requisito

Para considerar este requirements.md **validado**, debe cumplirse:

- [ ] Cada CA tiene escenario Dado/Cuando/Entonces específico
- [ ] Cada CA es verificable (no ambiguo)
- [ ] No hay conflictos entre requisitos
- [ ] Requisitos cubren el alcance de proposal.md
- [ ] RNF son medibles
- [ ] Trazabilidad hacia proposal.md está clara

---

## 5. Próximos Pasos

1. **Revisión**: Stakeholders revisan y aprueban requirements.md
2. **Validación**: Se marca como `validado` cuando recibe aprobación
3. **Generación de tasks.md**: Una vez aprobado, se genera tasks.md con tareas técnicas trazables a cada CA
4. **Implementación**: Backend Agent procede con código basado en tasks.md aprobado

---

## Estado
Pendiente de revisión y aprobación.
