# CHG-0001 - Tasks: Integración de estadísticas reales desde API-Football

## Estado
`borrador`

## Trazabilidad
**Cambio**: CHG-0001  
**Requirements**: [requirements.md](requirements.md)  
**Próximo**: Implementación + evidence.md

---

## Resumen Ejecutivo

Este documento define **17 tareas técnicas** organizadas en 4 bloques alineados con los requisitos funcionales (RF-0001 a RF-0004). Cada tarea es trazable a criterios de aceptación (CA) específicos y especifica componentes, patrones arquitectónicos y criterios de verificación.

**Flujo de tareas**:
```
BLOQUE 1: Infraestructura API
  ├─ T-0001: ApiFootballClient
  ├─ T-0002: RateLimitHandler
  └─ T-0003: Reintentos exponenciales

BLOQUE 2: Mapeo y Validación
  ├─ T-0004: PlayerMapper
  ├─ T-0005: MatchMapper
  ├─ T-0006: StatsNormalizer
  └─ T-0007: ValidationService

BLOQUE 3: Sincronización
  ├─ T-0008: SyncEstadisticasJob
  ├─ T-0009: RoundCompletionDetector
  ├─ T-0010: DuplicatePreventionService
  └─ T-0011: SyncOrchestrator

BLOQUE 4: Persistencia
  ├─ T-0012: PlayersRepository
  ├─ T-0013: MatchesRepository
  ├─ T-0014: SyncLogsRepository
  ├─ T-0015: TransactionManager
  └─ T-0016: FirestoreModels

BLOQUE 5: Observabilidad y Testing
  ├─ T-0017: StructuredLogging
  └─ T-0018: AcceptanceTests
```

---

## BLOQUE 1: Infraestructura de Consumo API

### T-0001: Crear ApiFootballClient (HTTP Client)

**Trazable a**: CA-0001.1, CA-0001.2, CA-0001.3

**Descripción**  
Implementar un cliente HTTP robusto que encapsula toda la comunicación con API-Football, incluyendo autenticación, manejo de headers, y construcción de requests tipadas.

**Componentes a Crear**

1. **Interfaz** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/ports/ApiFootballPort.java`
   - Métodos:
     - `List<PlayerDto> getPlayers(String league, int season) throws ApiException`
     - `List<FixtureDto> getFixtures(String league, int season, String round) throws ApiException`
     - `PlayerStatisticsDto getPlayerStats(int playerId, String league, int season) throws ApiException`

2. **Implementación** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/adapters/ApiFootballClientAdapter.java`
   - Usar: `RestTemplate` o `WebClient` (Spring Boot)
   - Configuración:
     - Base URL: `https://api-football-v3.p.rapidapi.com/`
     - Headers: `X-RapidAPI-Key`, `X-RapidAPI-Host`
     - Timeout: 10 segundos
     - Connection pool: máx 20 conexiones

3. **DTOs** (capa de presentación externa):
   - Ubicación: `src/main/java/com/example/football/infrastructure/dtos/`
   - `PlayerDto`, `FixtureDto`, `PlayerStatisticsDto`
   - Mapeo 1:1 de respuesta JSON de API-Football

4. **Configuración**:
   - Ubicación: `src/main/resources/application.properties`
   - Propiedades:
     ```
     api-football.key=${API_FOOTBALL_KEY}
     api-football.host=api-football-v3.p.rapidapi.com
     api-football.base-url=https://api-football-v3.p.rapidapi.com
     api-football.timeout.seconds=10
     api-football.pool.size=20
     ```

**Verificación**
- [ ] Cliente se autentica con headers correctos (test unitario)
- [ ] Request a `/players?league=135&season=2023` devuelve HTTP 200 (test de integración)
- [ ] DTOs se deserealizan correctamente desde JSON (test unitario)
- [ ] Timeout configurado en 10 segundos (revisión de código)
- [ ] API key nunca se expone en logs (test de seguridad)

**Dependencias**
- Spring Framework 6.x
- Jackson para serialización JSON
- Variable de entorno: `API_FOOTBALL_KEY`

---

### T-0002: Implementar RateLimitHandler

**Trazable a**: CA-0001.4

**Descripción**  
Crear middleware que intercepta responses HTTP 429 (Too Many Requests) y maneja el header `Retry-After` de forma automática.

**Componentes a Crear**

1. **Interceptor** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/filters/RateLimitInterceptor.java`
   - Interfaz: `ClientHttpRequestInterceptor` (Spring)
   - Lógica:
     ```
     Si status == 429:
       1. Leer header "Retry-After"
       2. Esperar N segundos
       3. Reintentar request (máx 3 intentos)
       4. Si sigue fallando, lanzar RateLimitExceededException
     ```

2. **Excepción personalizada**:
   - Ubicación: `src/main/java/com/example/football/domain/exceptions/RateLimitExceededException.java`
   - Debe incluir:
     - Timestamp de cuando se puede reintentar
     - Número de intentos realizados
     - Endpoint que fue rate limitado

3. **Configuración del cliente**:
   - Registrar interceptor en `RestTemplate` o `WebClient`
   - Configurar pool de reintentos

**Verificación**
- [ ] Al recibir HTTP 429 con `Retry-After: 30`, cliente aguarda 30 segundos (test de integración)
- [ ] Máximo 3 reintentos antes de fallar (test unitario)
- [ ] Log registra cada reintento: `[RATE_LIMIT] Retry attempt 1/3 after 30s` (test de logging)
- [ ] Excepción contiene información útil para debugging (test unitario)

**Dependencias**
- T-0001 (ApiFootballClient debe usar este interceptor)

---

### T-0003: Implementar Reintentos Exponenciales para Errores de Conectividad

**Trazable a**: CA-0001.5

**Descripción**  
Crear política de reintentos con backoff exponencial (1s, 2s, 4s) para timeouts y errores de conexión.

**Componentes a Crear**

1. **Retry Policy** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/services/RetryPolicy.java`
   - Configuración:
     ```
     maxAttempts = 3
     initialDelayMs = 1000
     backoffMultiplier = 2.0
     jitterFactor = 0.1 (±10% de variación para evitar thundering herd)
     ```
   - Excepciones que reintenta:
     - `SocketTimeoutException`
     - `ConnectException`
     - `IOException`
   - Excepciones que NO reintenta:
     - `HttpClientErrorException` (4xx)
     - `RateLimitExceededException`

2. **Decorator/AOP** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/aspects/RetryableAspect.java`
   - Anotación personalizada: `@Retryable(policy = "exponentialBackoff")`
   - Aplicar a métodos de `ApiFootballClientAdapter`

3. **Configuración**:
   - Ubicación: `src/main/resources/application.properties`
   - Propiedades:
     ```
     retry.max-attempts=3
     retry.initial-delay-ms=1000
     retry.backoff-multiplier=2.0
     retry.jitter-factor=0.1
     ```

**Verificación**
- [ ] Primer reintento ocurre a los 1000ms (test de timing)
- [ ] Segundo reintento ocurre a los 2000ms (test de timing)
- [ ] Tercer reintento ocurre a los 4000ms (test de timing)
- [ ] Jitter añade variación ±10% (test estadístico)
- [ ] Log registra cada reintento: `[RETRY] Attempt 2/3 after 2050ms delay` (test de logging)
- [ ] Después de 3 intentos fallidos, lanza `ConnectivityException` (test unitario)

**Dependencias**
- T-0001 (ApiFootballClient)
- Spring AOP o Resilience4j

---

## BLOQUE 2: Mapeo y Validación

### T-0004: Crear PlayerMapper

**Trazable a**: CA-0002.1

**Descripción**  
Implementar mapper que convierte `PlayerDto` de API-Football a entidad de dominio `Player` con estadísticas normalizadas.

**Componentes a Crear**

1. **Entidad de Dominio** (capa de dominio):
   - Ubicación: `src/main/java/com/example/football/domain/entities/Player.java`
   - Campos:
     ```java
     private String externalId;           // ID desde API-Football
     private String name;
     private String position;             // Ej: "ST", "CM", "CB"
     private Integer age;
     private String nationality;
     private String teamId;
     private RealStats realStats;         // Objeto anidado
     private LocalDateTime lastUpdated;
     ```
   - Entidad pura (sin dependencias externas)

2. **Value Object - RealStats**:
   - Ubicación: `src/main/java/com/example/football/domain/valueobjects/RealStats.java`
   - Campos:
     ```java
     private Integer season;
     private String league;
     private Integer appearances;
     private Integer goals;
     private Integer assists;
     private Integer passesAccuracy;      // 0-100
     private Integer dribblesSuccess;     // 0-100
     private Integer tackles;
     private Integer performanceScore;    // 0-100 (calculado)
     private LocalDateTime lastUpdated;
     ```

3. **Mapper** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/mappers/PlayerMapper.java`
   - Métodos:
     ```java
     public Player mapFromApiDto(PlayerDto apiDto, String teamId) 
     public PlayerDto mapToApiDto(Player domainEntity)
     ```
   - Lógica de mapeo:
     - `PlayerDto.player.id` → `Player.externalId`
     - `PlayerDto.player.name` → `Player.name`
     - `PlayerDto.statistics[0].team.id` → `Player.teamId`
     - `PlayerDto.statistics[0].games.appearances` → `RealStats.appearances`
     - Detectar `position` desde historial de `statistics` (no siempre viene en API)

4. **Validador de Mapeo**:
   - Ubicación: `src/main/java/com/example/football/application/validators/PlayerMapperValidator.java`
   - Validaciones pre-mapeo:
     - `PlayerDto.player` no es null
     - `PlayerDto.statistics` no está vacío
     - Campos obligatorios existen (id, name, age)

**Verificación**
- [ ] `PlayerDto` con Cristiano Ronaldo se mapea correctamente a `Player` (test unitario)
- [ ] `externalId` es correcto (test unitario)
- [ ] `RealStats` se crea con valores correctos (test unitario)
- [ ] Si `PlayerDto.statistics` está vacío, se lanza `MappingException` (test unitario)
- [ ] Mapper no modifica `Player` original (test de immutabilidad)

**Dependencias**
- T-0001 (DTOs de ApiFootballClient)
- T-0007 (ValidationService)

---

### T-0005: Crear MatchMapper

**Trazable a**: CA-0001.3

**Descripción**  
Implementar mapper que convierte `FixtureDto` de API-Football a entidad `Match` con estadísticas de jugadores por partido.

**Componentes a Crear**

1. **Entidad de Dominio** (capa de dominio):
   - Ubicación: `src/main/java/com/example/football/domain/entities/Match.java`
   - Campos:
     ```java
     private String fixtureId;            // ID desde API-Football
     private Integer round;
     private String league;
     private Integer season;
     private LocalDateTime date;
     private String homeTeamId;
     private String awayTeamId;
     private Score finalScore;            // Value Object
     private String status;               // "Match Finished", "Not Started", etc.
     private List<PlayerMatchStats> playerStats;
     private LocalDateTime lastUpdated;
     ```

2. **Value Object - Score**:
   - Ubicación: `src/main/java/com/example/football/domain/valueobjects/Score.java`
   - Campos:
     ```java
     private Integer homeGoals;
     private Integer awayGoals;
     ```

3. **Value Object - PlayerMatchStats**:
   - Ubicación: `src/main/java/com/example/football/domain/valueobjects/PlayerMatchStats.java`
   - Campos:
     ```java
     private String playerId;
     private String playerName;
     private String team;
     private Integer goals;
     private Integer assists;
     private Integer minutesPlayed;
     private Double rating;               // 0-10
     ```

4. **Mapper** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/mappers/MatchMapper.java`
   - Métodos:
     ```java
     public Match mapFromApiDto(FixtureDto apiDto)
     public MatchDto mapToApiDto(Match domainEntity)
     ```
   - Lógica:
     - `FixtureDto.fixture.id` → `Match.fixtureId`
     - `FixtureDto.fixture.teams.home.id` → `Match.homeTeamId`
     - `FixtureDto.fixture.teams.away.id` → `Match.awayTeamId`
     - `FixtureDto.goals.home` → `Score.homeGoals`
     - Extraer `PlayerMatchStats` desde `events` de API-Football

**Verificación**
- [ ] `FixtureDto` de "Man United 3-2 Arsenal" se mapea correctamente (test unitario)
- [ ] `Score` se crea con 3-2 (test unitario)
- [ ] `playerStats` incluye stats de Cristiano Ronaldo (1 gol, 1 asistencia) (test unitario)
- [ ] Si `status` ≠ "Match Finished", se marca como incompleto (test unitario)

**Dependencias**
- T-0001 (DTOs de ApiFootballClient)
- T-0007 (ValidationService)

---

### T-0006: Crear StatsNormalizer

**Trazable a**: CA-0002.2

**Descripción**  
Implementar normalización de estadísticas brutas (valores heterogéneos) a escala 0-100 para cálculos internos.

**Componentes a Crear**

1. **Servicio de Normalización** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/services/StatsNormalizerService.java`
   - Métodos:
     ```java
     public Integer normalizeGoals(Integer goals, String position)
     public Integer normalizePassAccuracy(Integer accuracy)
     public Integer normalizeDribblesSuccess(Integer success)
     public Integer normalizeTackles(Integer tackles, String position)
     public Integer calculatePerformanceScore(RealStats stats)
     ```

2. **Normalizadores Específicos**:
   - **Goles**: 
     - Máximo histórico por posición (ST: 50, CM: 20, CB: 5)
     - Fórmula: `(goals / max) * 100`
     - Mínimo 0, máximo 100
   - **Pases Precisos**: 
     - Ya es porcentaje (0-100), validar rango
   - **Regates Exitosos**:
     - Ya es porcentaje (0-100), validar rango
   - **Tackles**:
     - Máximo por posición (CB: 30, CM: 20, ST: 5)
     - Fórmula: `(tackles / max) * 100`
   - **Performance Score**:
     - Promedio ponderado de normalizaciones:
       - Goles: 30%
       - Pases: 25%
       - Regates: 25%
       - Tackles: 20%
     - Resultado final 0-100

3. **Configuración**:
   - Ubicación: `src/main/resources/normalization-config.json`
   - Máximos históricos por posición (actualizable)

**Verificación**
- [ ] 18 goles de ST normaliza a ~36 (18/50*100) (test unitario)
- [ ] 87% de pases queda en 87 (test unitario)
- [ ] 15 tackles de CB normaliza a 50 (15/30*100) (test unitario)
- [ ] Performance score final está entre 0-100 (test unitario)
- [ ] Valores negativos se clampean a 0 (test unitario)
- [ ] Valores > máximo se clampean a 100 (test unitario)

**Dependencias**
- T-0004 (PlayerMapper)
- T-0005 (MatchMapper)

---

### T-0007: Crear ValidationService

**Trazable a**: CA-0002.3

**Descripción**  
Implementar servicio centralizado de validación que verifica integridad de datos mapeados antes de persistencia.

**Componentes a Crear**

1. **Servicio de Validación** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/services/ValidationService.java`
   - Métodos:
     ```java
     public ValidationResult validatePlayer(Player player)
     public ValidationResult validateMatch(Match match)
     public ValidationResult validateRealStats(RealStats stats)
     ```

2. **Validadores Específicos**:
   - **Player**:
     - `name` no está vacío y no es null
     - `age` entre 16 y 50
     - `externalId` es único (usa repo)
     - `teamId` referencia equipo existente (usa repo)
     - `position` es válido (ST, CM, CB, etc.)
   - **RealStats**:
     - Todos los campos numéricos ≥ 0
     - Porcentajes entre 0-100
     - `season` es válido (actual ± 2 años)
     - `league` es reconocido (LaLiga, Premier, etc.)
   - **Match**:
     - `fixtureId` es único
     - `status` es válido
     - `homeTeamId` ≠ `awayTeamId`
     - `finalScore` es consistente (∃ goles si status=="Match Finished")

3. **Result Object**:
   - Ubicación: `src/main/java/com/example/football/application/valueobjects/ValidationResult.java`
   - Campos:
     ```java
     private boolean valid;
     private List<String> errors;
     private List<String> warnings;
     ```

**Verificación**
- [ ] `Player` válido retorna `ValidationResult.valid=true` (test unitario)
- [ ] `age=15` retorna error "Age must be >= 16" (test unitario)
- [ ] `externalId` duplicado retorna error (test de integración)
- [ ] `RealStats` con `-5` goles retorna error "Goals cannot be negative" (test unitario)
- [ ] `Match` con `homeTeamId==awayTeamId` retorna error (test unitario)

**Dependencias**
- T-0004 (PlayerMapper)
- T-0005 (MatchMapper)
- T-0006 (StatsNormalizer)
- Repositorios (T-0012, T-0013)

---

## BLOQUE 3: Sincronización

### T-0008: Crear SyncEstadisticasJob (Scheduled Job)

**Trazable a**: CA-0003.2

**Descripción**  
Implementar job programado (cron) que dispara la sincronización automática de estadísticas según horario configurado.

**Componentes a Crear**

1. **Job Configuration** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/config/SchedulingConfig.java`
   - Usar: `@EnableScheduling` y `@Scheduled`
   - Configuración:
     ```
     cron.sync.estadisticas=0 0 3 * * * (diariamente a las 03:00 UTC)
     ```

2. **Job Class** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/jobs/SyncEstadisticasJob.java`
   - Métodos:
     ```java
     @Scheduled(cron = "${cron.sync.estadisticas}")
     public void execute() throws SyncException
     ```
   - Lógica:
     ```
     1. Log: [SYNC_INICIO] Sincronización de estadísticas iniciada
     2. Obtener ronda actual del dominio Partidos
     3. Llamar SyncOrchestrator.sync(round)
     4. Si éxito: Log [SYNC_EXITOSO]
     5. Si falla: Capturar, registrar [SYNC_ERROR], schedule reintento
     ```

3. **Configuración**:
   - Ubicación: `src/main/resources/application.properties`
   - Propiedades:
     ```
     cron.sync.estadisticas=0 0 3 * * *
     sync.enabled=true
     sync.max-concurrent-jobs=1
     ```

4. **Monitoreo**:
   - Métrica: Timestamp de última ejecución exitosa
   - Métrica: Contador de ejecuciones fallidas consecutivas

**Verificación**
- [ ] Job se ejecuta a las 03:00 UTC cada día (test de timing)
- [ ] Log registra inicio: `[SYNC_INICIO]` (test de logging)
- [ ] Si sincronización exitosa, log registra `[SYNC_EXITOSO]` (test de logging)
- [ ] Si sincronización falla, log registra `[SYNC_ERROR]` (test de logging)
- [ ] Solo 1 job en ejecución simultáneamente (test de concurrencia)

**Dependencias**
- T-0011 (SyncOrchestrator)
- Spring Framework scheduling

---

### T-0009: Crear RoundCompletionDetector

**Trazable a**: CA-0003.1

**Descripción**  
Implementar servicio que detecta cuándo ha concluido una jornada (todos los partidos terminados) consultando API-Football.

**Componentes a Crear**

1. **Servicio de Detección** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/services/RoundCompletionDetectorService.java`
   - Métodos:
     ```java
     public boolean isRoundComplete(String league, Integer season, String round)
     public String getCurrentRound(String league, Integer season)
     public List<Match> getIncompleteMatches(String league, Integer season, String round)
     ```

2. **Lógica de Detección**:
   - Consultar `/fixtures` con parámetros `league`, `season`, `round`
   - Filtrar partidos con `status == "Match Finished"`
   - Contar partidos terminados vs total esperado
   - Si ≥ 95% completados (por si hay algún retraso), marcar como complete
   - Registrar en logs: cantidad de partidos completados

3. **Cache**:
   - Cachear resultado por 30 minutos (evitar múltiples llamadas)
   - Clave: `round_complete:{league}:{season}:{round}`

**Verificación**
- [ ] Consulta correcta a `/fixtures?league=135&season=2023&round=10` (test de integración)
- [ ] Retorna `true` si todos (o 95%) partidos tienen `status="Match Finished"` (test unitario)
- [ ] Retorna `false` si hay partidos con `status="Not Started"` (test unitario)
- [ ] Log registra: `[ROUND_CHECK] Round 10: 15/15 matches finished` (test de logging)
- [ ] Cache evita segunda consulta en 30 min (test de cache)

**Dependencias**
- T-0001 (ApiFootballClient)
- T-0005 (MatchMapper)

---

### T-0010: Crear DuplicatePreventionService

**Trazable a**: CA-0003.5

**Descripción**  
Implementar servicio que previene re-procesamiento de datos ya sincronizados para la misma jornada.

**Componentes a Crear**

1. **Servicio de Prevención** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/services/DuplicatePreventionService.java`
   - Métodos:
     ```java
     public boolean hasRoundBeenSynced(String league, Integer season, String round)
     public SyncMetadata getLastSyncForRound(String league, Integer season, String round)
     public void recordRoundSync(String league, Integer season, String round, SyncMetadata metadata)
     ```

2. **Lógica**:
   - Consultar `syncs` collection en Firestore
   - Buscar documento con clave: `{league}_{season}_{round}`
   - Si existe y `status="SUCCESS"`, retornar `true`
   - Si existe pero `status="FAILED"`, permitir reintento
   - Si no existe, retornar `false`

3. **Metadata**:
   - Ubicación: `src/main/java/com/example/football/domain/valueobjects/SyncMetadata.java`
   - Campos:
     ```java
     private LocalDateTime timestamp;
     private String status;          // SUCCESS, FAILED, PARTIAL
     private Integer playersUpdated;
     private Integer matchesUpdated;
     private Long durationMs;
     private List<String> errors;
     ```

**Verificación**
- [ ] Primera sincronización de Round 1 retorna `false` (test unitario)
- [ ] Segunda sincronización de Round 1 retorna `true` (test de integración)
- [ ] Si Round 1 tiene `status="FAILED"`, permite reintento (test unitario)
- [ ] Metadata se persiste correctamente en Firestore (test de integración)

**Dependencias**
- T-0013 (SyncLogsRepository)

---

### T-0011: Crear SyncOrchestrator (Orquestador Principal)

**Trazable a**: CA-0003.2, CA-0003.3, CA-0003.4

**Descripción**  
Implementar servicio orquestador que coordina todo el flujo de sincronización: detección de jornada, mapeo, validación, persistencia y manejo de errores.

**Componentes a Crear**

1. **Orquestador** (capa de aplicación - caso de uso):
   - Ubicación: `src/main/java/com/example/football/application/usecases/SyncEstadisticasUseCase.java`
   - Métodos:
     ```java
     public SyncResult sync(String league, Integer season) throws SyncException
     public SyncResult syncRound(String league, Integer season, String round) throws SyncException
     ```

2. **Lógica de Orquestación**:
   ```
   ENTRADA: league="LaLiga", season=2023
   
   1. Detectar ronda actual (T-0009)
      Si no hay ronda actual → retornar error
   
   2. Verificar si ya fue sincronizada (T-0010)
      Si sí y status=SUCCESS → registrar [SYNC_SKIPPED] y retornar
      Si sí y status=FAILED → registrar [SYNC_RETRY]
      Si no → proceder
   
   3. Obtener datos de API (T-0001, T-0003)
      - getPlayers(league, season)
      - getFixtures(league, season, round)
      Si error de conectividad → aplicar reintentos (T-0003)
      Si rate limit → esperar (T-0002)
      Si falla 3 veces → registrar y programar reintento en 1 hora
   
   4. Mapear datos (T-0004, T-0005)
      - Convertir PlayerDto → Player
      - Convertir FixtureDto → Match
      Si error de mapeo → registrar y saltar ese documento
   
   5. Normalizar estadísticas (T-0006)
      - Normalizar RealStats de cada Player
      - Normalizar PlayerMatchStats de cada Match
   
   6. Validar integridad (T-0007)
      - Validar cada Player
      - Validar cada Match
      Si validación falla → registrar error y saltar documento
   
   7. Persistir en Firestore (T-0012, T-0013, T-0014, T-0015)
      - Usar transacción para atomicidad
      - Actualizar players collection
      - Actualizar matches collection
      - Registrar sync log
      Si error de persistencia → rollback y retornar error
   
   8. Registrar resultado (T-0017)
      - Log: [SYNC_COMPLETADO]
        playersUpdated: 457
        matchesUpdated: 15
        duration: 240000ms
        status: SUCCESS
   
   SALIDA: SyncResult {
     status: "SUCCESS",
     playersUpdated: 457,
     matchesUpdated: 15,
     errors: [],
     durationMs: 240000
   }
   ```

3. **Manejo de Errores** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/exceptions/`
   - `SyncException` (base)
   - `ApiConnectivityException` → reintenta, luego falla
   - `RateLimitException` → espera, luego reintenta
   - `MappingException` → registra y sigue con siguiente
   - `ValidationException` → registra y sigue
   - `PersistenceException` → rollback y falla

4. **Result Object**:
   - Ubicación: `src/main/java/com/example/football/application/valueobjects/SyncResult.java`

**Verificación**
- [ ] Flujo completo sincroniza 457 jugadores en < 5 min (test de integración)
- [ ] Si falla API, reintenta con backoff exponencial (test de integración)
- [ ] Si hay error de mapeo, continúa con siguiente (test unitario)
- [ ] Transacción se revierte si hay error de persistencia (test de integración)
- [ ] Log registra todos los eventos críticos (test de logging)

**Dependencias**
- T-0001 a T-0010 (todos los servicios previos)
- T-0015 (TransactionManager)

---

## BLOQUE 4: Persistencia

### T-0012: Crear PlayersRepository

**Trazable a**: CA-0004.1

**Descripción**  
Implementar repositorio que persiste y consulta jugadores en Firestore con estructura normalizada.

**Componentes a Crear**

1. **Interfaz de Repositorio** (capa de aplicación - puerto):
   - Ubicación: `src/main/java/com/example/football/application/ports/PlayersRepositoryPort.java`
   - Métodos:
     ```java
     public void save(Player player)
     public Optional<Player> findByExternalId(String externalId)
     public List<Player> findByTeam(String teamId)
     public List<Player> findAll()
     public void delete(String externalId)
     public long count()
     ```

2. **Implementación Firestore** (capa de infraestructura - adaptador):
   - Ubicación: `src/main/java/com/example/football/infrastructure/adapters/FirestorePlayersRepository.java`
   - Collection: `players`
   - Document ID: `{externalId}`
   - Estructura:
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

3. **Índices Firestore**:
   - `team` (ascending)
   - `realStats.season` (ascending)
   - `realStats.performanceScore` (descending)
   - Índice compuesto: `(team, realStats.performanceScore)`

4. **Mapper Firestore**:
   - Ubicación: `src/main/java/com/example/football/infrastructure/mappers/FirestorePlayerMapper.java`
   - Convertir `Player` ↔ `DocumentSnapshot`

**Verificación**
- [ ] `save()` persiste Player correctamente en Firestore (test de integración)
- [ ] `findByExternalId(1234)` retorna Player completo (test de integración)
- [ ] `findByTeam("Manchester United")` retorna lista correcta (test de integración)
- [ ] Documento Firestore tiene estructura esperada (test de validación de schema)
- [ ] Índices están creados en Firestore (revisión manual)

**Dependencias**
- Firebase Admin SDK (Firestore)
- T-0016 (FirestoreModels)

---

### T-0013: Crear MatchesRepository

**Trazable a**: CA-0004.2

**Descripción**  
Implementar repositorio que persiste y consulta partidos en Firestore.

**Componentes a Crear**

1. **Interfaz de Repositorio** (capa de aplicación - puerto):
   - Ubicación: `src/main/java/com/example/football/application/ports/MatchesRepositoryPort.java`
   - Métodos:
     ```java
     public void save(Match match)
     public Optional<Match> findByFixtureId(String fixtureId)
     public List<Match> findByRound(Integer round)
     public List<Match> findByTeam(String teamId)
     public List<Match> findAll()
     ```

2. **Implementación Firestore** (capa de infraestructura - adaptador):
   - Ubicación: `src/main/java/com/example/football/infrastructure/adapters/FirestoreMatchesRepository.java`
   - Collection: `matches`
   - Document ID: `{fixtureId}`
   - Estructura:
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

3. **Índices Firestore**:
   - `round` (ascending)
   - `homeTeamId` (ascending)
   - `awayTeamId` (ascending)
   - `season` (ascending)
   - Índice compuesto: `(round, season)`

4. **Mapper Firestore**:
   - Ubicación: `src/main/java/com/example/football/infrastructure/mappers/FirestoreMatchMapper.java`

**Verificación**
- [ ] `save()` persiste Match correctamente (test de integración)
- [ ] `findByFixtureId(567890)` retorna Match completo (test de integración)
- [ ] `findByRound(10)` retorna lista de 15 partidos (test de integración)
- [ ] Documento Firestore tiene estructura esperada (test de validación)
- [ ] Índices están creados (revisión manual)

**Dependencias**
- Firebase Admin SDK (Firestore)
- T-0016 (FirestoreModels)

---

### T-0014: Crear SyncLogsRepository

**Trazable a**: CA-0004.3

**Descripción**  
Implementar repositorio que registra y consulta historiales de sincronización para auditoría.

**Componentes a Crear**

1. **Interfaz de Repositorio** (capa de aplicación - puerto):
   - Ubicación: `src/main/java/com/example/football/application/ports/SyncLogsRepositoryPort.java`
   - Métodos:
     ```java
     public void save(SyncLog log)
     public Optional<SyncLog> findByTimestamp(LocalDateTime timestamp)
     public List<SyncLog> findByStatus(String status)
     public List<SyncLog> findByDateRange(LocalDateTime from, LocalDateTime to)
     public SyncLog findLastSync()
     ```

2. **Implementación Firestore** (capa de infraestructura - adaptador):
   - Ubicación: `src/main/java/com/example/football/infrastructure/adapters/FirestoreSyncLogsRepository.java`
   - Collection: `syncs`
   - Document ID: `{timestamp}` (ej: "2023-11-15T03:00:00Z")
   - Estructura:
     ```json
     {
       "timestamp": "2023-11-15T03:00:00Z",
       "status": "SUCCESS",
       "league": "LaLiga",
       "season": 2023,
       "roundSynced": 10,
       "playersUpdated": 457,
       "matchesUpdated": 15,
       "errors": [],
       "duration": 240000,
       "details": {
         "playersCreated": 100,
         "playersUpdated": 357,
         "matchesCreated": 5,
         "matchesUpdated": 10
       }
     }
     ```

3. **Entity SyncLog**:
   - Ubicación: `src/main/java/com/example/football/domain/entities/SyncLog.java`

4. **Índices Firestore**:
   - `status` (ascending)
   - `timestamp` (descending)
   - `league` (ascending)

**Verificación**
- [ ] `save()` persiste SyncLog correctamente (test de integración)
- [ ] `findLastSync()` retorna sync más reciente (test de integración)
- [ ] `findByStatus("SUCCESS")` filtra correctamente (test de integración)
- [ ] Histórico es consultable por rango de fechas (test de integración)

**Dependencias**
- Firebase Admin SDK (Firestore)

---

### T-0015: Crear TransactionManager

**Trazable a**: CA-0004.4

**Descripción**  
Implementar gestor de transacciones que asegura atomicidad en operaciones multi-documento.

**Componentes a Crear**

1. **Interfaz de Transacciones** (capa de aplicación - puerto):
   - Ubicación: `src/main/java/com/example/football/application/ports/TransactionPort.java`
   - Métodos:
     ```java
     public <T> T execute(TransactionCallback<T> callback) throws TransactionException
     public void rollback()
     ```

2. **Implementación Firestore** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/adapters/FirestoreTransactionManager.java`
   - Usar: `FirebaseFirestore.runTransaction()`
   - Lógica:
     ```java
     db.runTransaction(transaction -> {
       // Operaciones dentro de transacción
       transaction.set(playersRef, playerDoc);
       transaction.set(matchesRef, matchDoc);
       transaction.set(syncsRef, syncLogDoc);
       return result;
     })
     ```

3. **Callback Interface**:
   - Ubicación: `src/main/java/com/example/football/application/callbacks/TransactionCallback.java`
   - Permite pasar operaciones como lambda

4. **Configuración**:
   - Timeout: 30 segundos
   - Reintentos: 3
   - Backoff entre reintentos: exponencial

**Verificación**
- [ ] Si todas las operaciones son exitosas, commit ocurre (test de integración)
- [ ] Si una operación falla, rollback ocurre y no se persiste nada (test de integración)
- [ ] Timeout de 30s se respeta (test de timing)
- [ ] Reintentos funcionan ante conflictos transaccionales (test de resiliencia)

**Dependencias**
- Firebase Admin SDK (Firestore)

---

### T-0016: Crear FirestoreModels (Entidades de Persistencia)

**Trazable a**: CA-0004.1, CA-0004.2, CA-0004.3

**Descripción**  
Definir clases que mapean directamente a documentos Firestore (con anotaciones de Firebase).

**Componentes a Crear**

1. **PlayerDocument** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/persistence/models/PlayerDocument.java`
   - Anotaciones: `@Document` (si usa framework) o PlainJava con Getters/Setters
   - Campos como en CA-0004.1

2. **MatchDocument** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/persistence/models/MatchDocument.java`
   - Campos como en CA-0004.2

3. **SyncLogDocument** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/persistence/models/SyncLogDocument.java`
   - Campos como en CA-0004.3

4. **Nested Value Objects**:
   - `RealStatsDocument` (dentro de PlayerDocument)
   - `ScoreDocument` (dentro de MatchDocument)
   - `PlayerMatchStatsDocument` (dentro de MatchDocument)
   - `MetadataDocument` (en Player y Match)

**Verificación**
- [ ] Clases se deserealizan correctamente desde Firestore JSON (test unitario)
- [ ] Campos opcionales se manejan correctamente (test unitario)
- [ ] Tipos complejos (List, Map) se mapean bien (test unitario)

**Dependencias**
- Firebase Admin SDK

---

## BLOQUE 5: Observabilidad y Testing

### T-0017: Implementar Structured Logging

**Trazable a**: RNF-0004.1, CA-0003.2, CA-0003.3, CA-0003.4

**Descripción**  
Crear sistema centralizado de logs JSON estructurados para rastrear todo el flujo de sincronización.

**Componentes a Crear**

1. **Logger Estructurado** (capa de infraestructura):
   - Ubicación: `src/main/java/com/example/football/infrastructure/logging/StructuredLogger.java`
   - Usar: SLF4J + Logback
   - Formato JSON en `logback-spring.xml`:
     ```xml
     <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
     ```

2. **Log Events** (capa de aplicación):
   - Ubicación: `src/main/java/com/example/football/application/events/SyncEvent.java`
   - Eventos:
     - `SYNC_INICIO`: timestamp, league, season
     - `SYNC_ROUND_CHECK`: round, matchesTotal, matchesFinished
     - `API_REQUEST`: endpoint, params, duration
     - `RATE_LIMIT_HIT`: endpoint, retryAfter
     - `RETRY_ATTEMPT`: attempt, nextDelayMs
     - `MAPPING_ERROR`: entityType, externalId, errorMessage
     - `VALIDATION_ERROR`: entityType, externalId, violations
     - `PERSIST_START`: entityCount
     - `PERSIST_SUCCESS`: entitiesWritten, duration
     - `PERSIST_ERROR`: errorMessage, rollback
     - `SYNC_COMPLETADO`: status, playersUpdated, matchesUpdated, duration

3. **Facade de Logging**:
   - Ubicación: `src/main/java/com/example/football/infrastructure/logging/SyncLoggerFacade.java`
   - Métodos:
     ```java
     public void logSyncStart(String league, Integer season)
     public void logApiRequest(String endpoint, Map<String, String> params, long durationMs)
     public void logMappingError(String entityType, String externalId, String error)
     public void logPersistSuccess(int count, long durationMs)
     // ... etc
     ```

4. **Formato de Logs**:
   ```json
   {
     "timestamp": "2023-11-15T03:00:00Z",
     "level": "INFO",
     "logger": "com.example.football.application.jobs.SyncEstadisticasJob",
     "message": "Synchronization completed successfully",
     "event": "SYNC_COMPLETADO",
     "traceId": "550e8400-e29b-41d4-a716-446655440000",
     "sync": {
       "league": "LaLiga",
       "season": 2023,
       "round": 10,
       "status": "SUCCESS",
       "playersUpdated": 457,
       "matchesUpdated": 15,
       "durationMs": 240000
     }
   }
   ```

5. **Contexto Distribuido**:
   - Generar `traceId` único per sincronización
   - Propagarloendido a todos los logs (MDC - Mapped Diagnostic Context)

**Verificación**
- [ ] Logs se escriben en formato JSON válido (test de validación)
- [ ] Cada evento incluye timestamp, level, traceId (test de completitud)
- [ ] No hay información sensible en logs (API keys, tokens) (test de seguridad)
- [ ] Logs pueden ser parseados con jq (test de tooling)
- [ ] Timestamp está en ISO-8601 (test de formato)

**Dependencias**
- SLF4J
- Logback
- Logstash Logback Encoder

---

### T-0018: Crear Acceptance Tests

**Trazable a**: Todos los CA

**Descripción**  
Implementar suite de pruebas de aceptación que valida end-to-end la sincronización completa.

**Componentes a Crear**

1. **Test Feature File** (BDD - Cucumber):
   - Ubicación: `src/test/resources/features/sync-estadisticas.feature`
   - Escenarios:
     ```gherkin
     Feature: Integración de estadísticas reales desde API-Football
       
       Scenario: Sincronización exitosa de jugadores y partidos
         Given existe configuración de API-Football válida
         And la jornada actual es Round 10 de LaLiga 2023
         When se ejecuta sincronización automática
         Then se obtienen jugadores desde API
         And se obtienen partidos desde API
         And se mapean correctamente a entidades internas
         And se normalizan estadísticas a escala 0-100
         And se persisten en Firestore sin duplicados
         And se registra log de sincronización exitosa
         
       Scenario: Manejo de rate limit de API
         Given API-Football devuelve HTTP 429
         When se ejecuta request a /players
         Then se respeta header Retry-After
         And se reintenta automáticamente
         And se completa exitosamente tras reintento
       
       Scenario: Prevención de duplicados
         Given ya se sincronizó Round 10
         When se ejecuta sincronización nuevamente para Round 10
         Then se detecta que ya fue sincronizado
         And se salta procesamiento
         And se registra log [SYNC_SKIPPED]
     ```

2. **Step Definitions** (Glue Code):
   - Ubicación: `src/test/java/com/example/football/acceptance/steps/SyncStepDefinitions.java`
   - Implementar steps del feature file
   - Usar: `@Given`, `@When`, `@Then`

3. **Test Base Class**:
   - Ubicación: `src/test/java/com/example/football/acceptance/AcceptanceTestBase.java`
   - Setup: Mock de API-Football, Firestore emulator, Spring context
   - Teardown: Limpiar estado entre tests

4. **Mock de API-Football**:
   - Usar: WireMock o TestContainers
   - Endpoints mockeados:
     - `GET /players?league=135&season=2023` → respuesta JSON completa
     - `GET /fixtures?league=135&season=2023&round=10` → 15 partidos
     - `GET /players?league=135&season=2023` con HTTP 429 → rate limit

5. **Firestore Emulator**:
   - Usar: Firebase Emulator Suite
   - Inicializar colecciones vacías antes de cada test
   - Validar documentos después de sincronización

6. **Assertions**:
   - Validar cantidad de documentos persistidos
   - Validar estructura JSON de documentos
   - Validar logs registrados
   - Validar timestamps y duraciones

**Verificación**
- [ ] Test de flujo completo exitoso pasa (test de integración)
- [ ] Test de rate limit maneja reintentos (test de integración)
- [ ] Test de duplicados salta procesamiento (test de integración)
- [ ] Todos los logs esperados están presentes (test de logging)
- [ ] Tests pasan con Firestore emulator (test de persistencia)

**Dependencias**
- Cucumber/Gherkin
- Spring Test Framework
- WireMock o TestContainers
- Firebase Emulator Suite

---

## 5. Matriz de Trazabilidad

### Requisitos Funcionales → Tasks

| RF | CA | Tasks |
|----|----|-------|
| RF-0001 | CA-0001.1 | T-0001, T-0002 |
| RF-0001 | CA-0001.2 | T-0001, T-0004 |
| RF-0001 | CA-0001.3 | T-0001, T-0005 |
| RF-0001 | CA-0001.4 | T-0002 |
| RF-0001 | CA-0001.5 | T-0003 |
| RF-0002 | CA-0002.1 | T-0004 |
| RF-0002 | CA-0002.2 | T-0006 |
| RF-0002 | CA-0002.3 | T-0007 |
| RF-0003 | CA-0003.1 | T-0009 |
| RF-0003 | CA-0003.2 | T-0008, T-0011 |
| RF-0003 | CA-0003.3 | T-0011, T-0012 |
| RF-0003 | CA-0003.4 | T-0011, T-0017 |
| RF-0003 | CA-0003.5 | T-0010, T-0011 |
| RF-0004 | CA-0004.1 | T-0012, T-0016 |
| RF-0004 | CA-0004.2 | T-0013, T-0016 |
| RF-0004 | CA-0004.3 | T-0014 |
| RF-0004 | CA-0004.4 | T-0015 |

### Requisitos No Funcionales → Tasks

| RNF | Descripción | Tasks |
|-----|-------------|-------|
| RNF-0001.1 | Protección API key | T-0001, T-0017 |
| RNF-0001.2 | Validación de origen | T-0001 |
| RNF-0001.3 | Autorización | T-0017 (logs) |
| RNF-0002.1 | Latencia < 5 min | T-0008, T-0011 |
| RNF-0002.2 | Throughput > 1000 doc/min | T-0012, T-0013 |
| RNF-0002.3 | < 100 requests | T-0001, T-0011 |
| RNF-0003.1 | 95% disponibilidad | T-0003, T-0008, T-0011 |
| RNF-0003.2 | Recuperación automática | T-0003, T-0011 |
| RNF-0003.3 | Detección de inconsistencias | T-0007, T-0011 |
| RNF-0004.1 | Logs JSON | T-0017 |
| RNF-0004.2 | Documentación errores | Archivo `errores.md` (pendiente) |

---

## 6. Dependencias Entre Tasks

```
T-0001 (ApiFootballClient)
├─ T-0002 (RateLimitHandler)
├─ T-0003 (Reintentos)
├─ T-0004 (PlayerMapper) ─┐
├─ T-0005 (MatchMapper) ──┼─> T-0006 (StatsNormalizer)
└─ T-0009 (RoundCompletion)    └─> T-0007 (ValidationService)

T-0007 (ValidationService) ┐
                           ├─> T-0011 (SyncOrchestrator)
T-0009 (RoundCompletion) ─┘    ├─> T-0015 (TransactionManager)
T-0010 (DuplicatePrevention)   ├─> T-0012 (PlayersRepository)
                               ├─> T-0013 (MatchesRepository)
                               ├─> T-0014 (SyncLogsRepository)
                               └─> T-0017 (StructuredLogging)

T-0008 (SyncEstadisticasJob) ──> T-0011 (SyncOrchestrator) ─> T-0018 (Acceptance Tests)

T-0016 (FirestoreModels)
├─> T-0012 (PlayersRepository)
├─> T-0013 (MatchesRepository)
└─> T-0014 (SyncLogsRepository)
```

---

## 7. Orden de Implementación Recomendado

### Fase 1: Infraestructura Base (Sin dependencias externas)
1. T-0016 (FirestoreModels)
2. T-0001 (ApiFootballClient - interfaz primero)
3. T-0002 (RateLimitHandler)
4. T-0003 (Reintentos)

### Fase 2: Mapeo y Validación (Lógica pura)
5. T-0004 (PlayerMapper)
6. T-0005 (MatchMapper)
7. T-0006 (StatsNormalizer)
8. T-0007 (ValidationService)

### Fase 3: Persistencia (Adaptadores)
9. T-0012 (PlayersRepository)
10. T-0013 (MatchesRepository)
11. T-0014 (SyncLogsRepository)
12. T-0015 (TransactionManager)

### Fase 4: Sincronización (Orquestación)
13. T-0009 (RoundCompletionDetector)
14. T-0010 (DuplicatePreventionService)
15. T-0011 (SyncOrchestrator)
16. T-0008 (SyncEstadisticasJob)

### Fase 5: Observabilidad y Testing
17. T-0017 (StructuredLogging)
18. T-0018 (Acceptance Tests)

---

## 8. Criterios de Completitud

Un task se considera **COMPLETADO** cuando:
- [ ] Código implementado según descripción
- [ ] Tests unitarios pasan (>80% coverage)
- [ ] Tests de integración pasan
- [ ] Código sigue arquitectura limpia (no mezcla capas)
- [ ] Logs estructurados presentes
- [ ] Documentación actualizada
- [ ] No hay warnings de compilación
- [ ] Verificaciones de seguridad aplicadas

Una **FASE** se considera completada cuando:
- [ ] Todos los tasks de la fase están COMPLETADOS
- [ ] Acceptance tests de esa fase pasan
- [ ] No hay regressions en fases anteriores
- [ ] Code review aprobado

---

## 9. Próximos Pasos

1. **Revisión y aprobación** de este tasks.md por stakeholders
2. **Planificación de sprints** basada en Fases de implementación
3. **Asignación de tasks** a desarrolladores
4. **Implementación iterativa** comenzando por Fase 1
5. **Validación con acceptance tests** tras cada Fase
6. **Generación de evidence.md** registrando resultados de tests

---

## Estado
Pendiente de revisión y aprobación.

**Última actualización**: 2026-08-24
