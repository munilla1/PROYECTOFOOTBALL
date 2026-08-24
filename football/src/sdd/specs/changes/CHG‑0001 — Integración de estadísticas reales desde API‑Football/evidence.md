# CHG-0001 - Evidencia de Pruebas de Aceptación

## Estado
`en_validacion`

## Trazabilidad
**Cambio**: CHG-0001  
**Proposal**: [proposal.md](proposal.md)  
**Requirements**: [requirements.md](requirements.md)  
**Tasks**: [tasks.md](tasks.md)

---

## 1. Matriz de Pruebas de Aceptación

| Requisito | CA | Estado | Resultado | Fecha | Evidencia |
|-----------|-----|--------|-----------|-------|-----------|
| RF-0001 | CA-0001.1 | ⏳ Pendiente | N/A | N/A | `ApiFootballClientAdapterTest::testAuthenticationHeaders` |
| RF-0001 | CA-0001.2 | ⏳ Pendiente | N/A | N/A | `ApiFootballClientAdapterTest::testGetPlayers` |
| RF-0001 | CA-0001.3 | ⏳ Pendiente | N/A | N/A | `ApiFootballClientAdapterTest::testGetFixtures` |
| RF-0001 | CA-0001.4 | ⏳ Pendiente | N/A | N/A | `ApiFootballClientAdapterTest::testRateLimitHandling` |
| RF-0001 | CA-0001.5 | ⏳ Pendiente | N/A | N/A | `ApiFootballClientAdapterTest::testConnectivityErrorRetry` |
| RF-0002 | CA-0002.1 | ⏳ Pendiente | N/A | N/A | `PlayerMapperServiceTest::testMapPlayerDto` |
| RF-0002 | CA-0002.2 | ⏳ Pendiente | N/A | N/A | `StatsNormalizerServiceTest::testNormalizeGoals` |
| RF-0002 | CA-0002.3 | ⏳ Pendiente | N/A | N/A | `ValidationServiceTest::testValidatePlayer` |
| RF-0003 | CA-0003.1 | ⏳ Pendiente | N/A | N/A | `RoundCompletionDetectorTest::testDetectRoundCompletion` |
| RF-0003 | CA-0003.2 | ⏳ Pendiente | N/A | N/A | `SyncEstadisticasJobTest::testScheduledExecution` |
| RF-0003 | CA-0003.3 | ⏳ Pendiente | N/A | N/A | `SyncOrchestratorTest::testUpdatePlayersAfterRound` |
| RF-0003 | CA-0003.4 | ⏳ Pendiente | N/A | N/A | `SyncOrchestratorTest::testHandleSyncFailure` |
| RF-0003 | CA-0003.5 | ⏳ Pendiente | N/A | N/A | `DuplicatePreventionServiceTest::testPreventDuplicates` |
| RF-0004 | CA-0004.1 | ⏳ Pendiente | N/A | N/A | `PlayersRepositoryAdapterTest::testPersistPlayer` |
| RF-0004 | CA-0004.2 | ⏳ Pendiente | N/A | N/A | `MatchesRepositoryAdapterTest::testPersistMatch` |
| RF-0004 | CA-0004.3 | ⏳ Pendiente | N/A | N/A | `SyncLogsRepositoryAdapterTest::testPersistSyncLog` |
| RF-0004 | CA-0004.4 | ⏳ Pendiente | N/A | N/A | `TransactionManagerTest::testTransactionalConsistency` |

---

## 2. Escenarios de Aceptación Detallados

### RF-0001: Consumo de endpoints de API-Football

#### CA-0001.1: Autenticación con API-Football

**Escenario**: Sistema se autentica con headers correctos

```gherkin
Dado que existe una API key válida configurada en application.properties
Cuando el sistema intenta conectar a https://api-football-v3.p.rapidapi.com/
Entonces la request incluye headers:
  - X-RapidAPI-Key: <valor-de-env>
  - X-RapidAPI-Host: api-football-v3.p.rapidapi.com
Y la respuesta es exitosa (HTTP 200)
```

**Evidencia**:
- ✅ Clase: `ApiFootballClientAdapterTest`
- Test: `testAuthenticationHeaders()`
- Tipo: Prueba unitaria con MockRestTemplate
- Verificación: Captura headers en mock, valida presencia y formato

---

#### CA-0001.2: Obtener datos de jugadores

**Escenario**: Sistema obtiene y mapea jugadores correctamente

```gherkin
Dado que existe conexión a API-Football
Cuando se ejecuta request a /players con league=135&season=2023
Entonces se recibe JSON con estructura:
  - player_id, player_name, position
  - statistics[0].games.appearances
  - statistics[0].goals.total, assists, etc.
Y el sistema mapea cada campo a entidad Player interna
```

**Evidencia**:
- ✅ Clase: `ApiFootballClientAdapterTest`
- Test: `testGetPlayers()`
- Tipo: Prueba unitaria con mock de respuesta JSON
- Verificación: Assert en List<PlayerDto>, valida cantidad y campos

---

#### CA-0001.3: Obtener datos de partidos

**Escenario**: Sistema obtiene y mapea partidos correctamente

```gherkin
Dado que existe conexión a API-Football
Cuando se ejecuta request a /fixtures con league=135&season=2023&round=1
Entonces se recibe JSON con:
  - fixture_id, fixture_date
  - teams.home, teams.away
  - score.fulltime, status
Y se mapean equipos local y visitante correctamente
```

**Evidencia**:
- ✅ Clase: `ApiFootballClientAdapterTest`
- Test: `testGetFixtures()`
- Tipo: Prueba unitaria con mock JSON
- Verificación: Assert en List<FixtureDto>, validación de equipos

---

#### CA-0001.4: Manejo de rate limits

**Escenario**: Sistema respeta HTTP 429 y reintentos

```gherkin
Dado que API-Football devuelve HTTP 429
Cuando el sistema procesa respuesta con header Retry-After: 60
Entonces aguarda 60 segundos
Y reintenta request automáticamente
Y ejecuta máximo 3 reintentos antes de fallar
```

**Evidencia**:
- ✅ Clase: `ApiFootballClientAdapterTest`
- Test: `testRateLimitHandling()`
- Tipo: Prueba unitaria con mock de 429
- Verificación: Captura sleep, valida reintentos, lanza RateLimitExceededException

---

#### CA-0001.5: Manejo de errores de conectividad

**Escenario**: Sistema reintenta con backoff exponencial

```gherkin
Dado que conectividad a API-Football falla (timeout/rechazado)
Cuando se ejecuta request
Entonces reintenta con delays: 1s, 2s, 4s
Y registra fallos en logs
Y tras 3 reintentos, aborta con ApiConnectivityException
```

**Evidencia**:
- ✅ Clase: `ApiFootballClientAdapterTest`
- Test: `testConnectivityErrorRetry()`
- Tipo: Prueba unitaria con mock de timeout
- Verificación: Assert en delays, validación de exception

---

### RF-0002: Mapeo de estadísticas reales a entidades internas

#### CA-0002.1: Mapear datos de jugador

**Escenario**: PlayerDto se convierte a Player con campos correctos

```gherkin
Dado que se recibe PlayerDto desde API-Football con:
  - player.id = 1234
  - player.name = "Cristiano Ronaldo"
  - statistics[0].team.id = 1
Cuando se mapea a entidad interna
Entonces se crea Player con:
  - externalId = "1234"
  - name = "Cristiano Ronaldo"
  - position = detectada (ST/CM/CB)
  - age, nationality, teamId
  - realStats con estadísticas normalizadas
```

**Evidencia**:
- ✅ Clase: `PlayerMapperServiceTest`
- Test: `testMapPlayerDto()`
- Tipo: Prueba unitaria
- Verificación: Assert en cada campo, valida tipos y no-nulos

---

#### CA-0002.2: Mapear estadísticas de rendimiento

**Escenario**: Estadísticas se normalizan a escala 0-100

```gherkin
Dado que jugador tiene:
  - goals.total = 18
  - passes.accuracy = 87
  - dribbles.success = 70%
  - tackles.total = 15
Cuando se normalizan
Entonces:
  - goals → normalizado a 0-100 (posición-dependiente)
  - passesAccuracy → 87 (ya es %)
  - dribblesSuccess → 70 (ya es %)
  - tackles → normalizado a 0-100 (posición-dependiente)
  - performanceScore = promedio ponderado (0-100)
```

**Evidencia**:
- ✅ Clase: `StatsNormalizerServiceTest`
- Test: `testNormalizeGoals()`, `testNormalizeTackles()`, etc.
- Tipo: Prueba unitaria
- Verificación: Assert en rango 0-100 para cada métrica

---

#### CA-0002.3: Validar integridad de datos mapeados

**Escenario**: Validación pre-persistencia rechaza datos inválidos

```gherkin
Dado que se mapea Player con campos inválidos
Cuando se valida
Entonces se detectan errores:
  - name vacío → error
  - age = 15 (< 16) → error
  - age = 51 (> 50) → error
  - externalId duplicado → error
  - estadísticas negativas → error
  - porcentaje > 100 → error
Y se rechaza persistencia
```

**Evidencia**:
- ✅ Clase: `ValidationServiceTest`
- Test: `testValidatePlayer()`, múltiples escenarios
- Tipo: Prueba unitaria
- Verificación: Assert en List<String> errores, no es vacía

---

### RF-0003: Sincronización automática tras cada jornada real

#### CA-0003.1: Detectar conclusión de jornada

**Escenario**: Sistema identifica Round completo

```gherkin
Dado que jornada actual = "Round 1" LaLiga 2023-24
Cuando ejecuta RoundCompletionDetector
Entonces consulta /fixtures con league=135&season=2023&round=1
Y verifica que todos match.status = "Match Finished"
Y solo entonces procede a sincronizar
```

**Evidencia**:
- ✅ Clase: `RoundCompletionDetectorTest`
- Test: `testDetectRoundCompletion()`
- Tipo: Prueba unitaria con mock de fixtures
- Verificación: Assert en boolean isRoundComplete

---

#### CA-0003.2: Ejecutar sincronización programada

**Escenario**: Job se ejecuta según cron schedule

```gherkin
Dado que existe cron job: 0 0 3 * * *
Cuando se alcanza horario programado (03:00 UTC)
Entonces SyncEstadisticasJob se dispara
Y registra: [SYNC_INICIO] Sincronización iniciada
Y ejecuta sincronización completa
```

**Evidencia**:
- ✅ Clase: `SyncEstadisticasJobTest`
- Test: `testScheduledExecution()`
- Tipo: Prueba de integración con @SpringBootTest
- Verificación: Mock de scheduler, valida invocación

---

#### CA-0003.3: Actualizar jugadores tras jornada

**Escenario**: Nuevas estadísticas se persisten

```gherkin
Dado que Cristiano Ronaldo tiene 18 goles antes de Round 10
Cuando se sincroniza tras Round 10
Entonces consulta /players y obtiene 19 goles
Y actualiza Player.realStats.goals = 19
Y registra lastUpdated = ahora
```

**Evidencia**:
- ✅ Clase: `SyncOrchestratorTest`
- Test: `testUpdatePlayersAfterRound()`
- Tipo: Prueba unitaria con mocks
- Verificación: Assert en campo goals actualizado

---

#### CA-0003.4: Manejar sincronización fallida

**Escenario**: Fallos se registran y reintenta

```gherkin
Dado que durante sincronización falla API-Football
Cuando job captura excepción
Entonces registra: [SYNC_ERROR] Sincronización fallida
Y programa reintento en 1 hora
Y notifica admin si falla 3 veces
```

**Evidencia**:
- ✅ Clase: `SyncOrchestratorTest`
- Test: `testHandleSyncFailure()`
- Tipo: Prueba unitaria
- Verificación: Assert en logs, retry scheduling, admin notification

---

#### CA-0003.5: Evitar duplicados en sincronización

**Escenario**: Reprocessamiento no crea duplicados

```gherkin
Dado que Round 1 ya se sincronizó hace 2 horas
Cuando ejecuta sync para Round 1 nuevamente
Entonces detecta lastSyncRound = 1
Y evita reprocessar datos
O si hay cambios, actualiza docs existentes (no crea nuevos)
```

**Evidencia**:
- ✅ Clase: `DuplicatePreventionServiceTest`
- Test: `testPreventDuplicates()`
- Tipo: Prueba unitaria
- Verificación: Assert en count pre/post, valida upsert

---

### RF-0004: Persistencia de estadísticas reales en Firestore

#### CA-0004.1: Crear colección `players` con estadísticas

**Escenario**: Player se persiste con estructura correcta

```gherkin
Dado que se sincroniza Cristiano Ronaldo desde API
Cuando se persiste en Firestore
Entonces documento en players/{externalId} tiene:
  - externalId: 1234
  - name: "Cristiano Ronaldo"
  - position: "ST"
  - realStats.goals: 18
  - realStats.performanceScore: 82
  - metadata.syncedAt: timestamp
```

**Evidencia**:
- ✅ Clase: `PlayersRepositoryAdapterTest`
- Test: `testPersistPlayer()`
- Tipo: Prueba unitaria con mock de Firestore
- Verificación: Assert en campos guardados

---

#### CA-0004.2: Crear colección `matches` con datos de partidos

**Escenario**: Match se persiste con estructura correcta

```gherkin
Dado que concluyó Match: Manchester 3-2 Arsenal
Cuando se sincroniza
Entonces documento en matches/{fixtureId} tiene:
  - fixtureId: 567890
  - round: 10
  - finalScore.home: 3, away: 2
  - status: "Match Finished"
  - playerStats: array con goles, asistencias, etc.
```

**Evidencia**:
- ✅ Clase: `MatchesRepositoryAdapterTest`
- Test: `testPersistMatch()`
- Tipo: Prueba unitaria
- Verificación: Assert en estructura JSON

---

#### CA-0004.3: Mantener historial de sincronizaciones

**Escenario**: Sync log se crea y es auditable

```gherkin
Dado que ejecutó sincronización
Cuando se persisten datos
Entonces documento en syncs/{timestamp} tiene:
  - status: "SUCCESS" | "FAILED"
  - roundSynced: 10
  - playersUpdated: 457
  - duration: "2400ms"
```

**Evidencia**:
- ✅ Clase: `SyncLogsRepositoryAdapterTest`
- Test: `testPersistSyncLog()`
- Tipo: Prueba unitaria
- Verificación: Assert en documento sincronización

---

#### CA-0004.4: Garantizar consistencia transaccional

**Escenario**: Actualización de múltiples docs es atómica

```gherkin
Dado que se actualizan Player y SyncLog simultáneamente
Cuando se persisten transaccionalmente
Entonces si una falla, ambas se revierten
Y no hay estado inconsistente
```

**Evidencia**:
- ✅ Clase: `TransactionManagerTest`
- Test: `testTransactionalConsistency()`
- Tipo: Prueba de integración
- Verificación: Assert en rollback, estado final consistente

---

## 3. Resumen de Hallazgos

| Categoría | Pendiente | Validado | Fallido |
|-----------|-----------|----------|---------|
| RF-0001 (Consumo API) | 5 | 0 | 0 |
| RF-0002 (Mapeo) | 3 | 0 | 0 |
| RF-0003 (Sincronización) | 5 | 0 | 0 |
| RF-0004 (Persistencia) | 4 | 0 | 0 |
| **TOTAL** | **17** | **0** | **0** |

---

## 4. Próximos Pasos

1. ✅ **Generación de tests**: Crear archivos JUnit en `src/test/java/com/example/football/estadisticas/`
2. ⏳ **Ejecución de tests**: Ejecutar `mvn test` para validar cada CA
3. ⏳ **Reporte de resultados**: Actualizar esta matriz con resultados
4. ⏳ **Validación final**: Marcar evidence.md como `validado` cuando todos pasen

---

## 5. Criterios de Aceptación de Evidence

Para considerar esta evidence.md **completa**:

- [x] Todos los CA de requirements.md tienen escenario de prueba
- [x] Cada escenario es verificable y automatizable
- [x] Matriz de pruebas está actualizada
- [x] No hay conflictos de cobertura
- [ ] Todos los tests pasan (pendiente ejecución)
- [ ] Logs registran trazabilidad (pendiente ejecución)

---

## Estado
✅ **Evidence generado y listo para pruebas**  
Próximo estado: `validado` (tras ejecutar y pasar todos los tests)
