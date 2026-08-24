# CHG-0006 - Tasks (Tareas Técnicas)

**Estado**: `ready for implementation`  
**Fecha**: 2026-08-24  
**Prioridad**: Media  
**Complejidad**: Alta (orquestación de sincronización + validaciones)

---

## 1. Resumen Técnico

CHG-0006 requiere implementar un sistema de sincronización de jornadas desde API-Football con validaciones de bloqueo de partidos. Esto involucra:

1. **Dominio**: Entidades `Jornada`, `JornadaStatus`, excepciones
2. **Aplicación**: Puertos y servicios de sincronización y validación
3. **Infraestructura**: JPA entities, repositorios, adaptadores, cliente API
4. **Base de datos**: Tabla `jornadas`, índices, relaciones con `partidos`
5. **Scheduling**: Job programado diario para actualizaciones
6. **Validación**: Bloqueos de partidos en capa de dominio

---

## 2. Tareas por Requisito

### Requisito RF-0001: Sincronización de jornadas desde API-Football

---

#### T-0001: Entidad de Dominio `Jornada`

**Objetivo**: Crear entidad de dominio pura que represente una jornada real sincronizada.

**Descripción**:
- Crear record `Jornada.java` en `com.example.football.jornadas.domain`
- Campos: `id` (UUID), `roundNumber` (int), `league` (String), `season` (int), `status` (JornadaStatus enum), `matchCount` (int), `createdAt` (Instant), `synchronizedAt` (Instant)
- Implementar compact constructor con validaciones:
  - `roundNumber` > 0 y ≤ 38 (máximo en ligas reales)
  - `league` no vacío
  - `season` >= 2000
  - `matchCount` >= 0
  - `createdAt` y `synchronizedAt` no nulos
- Factory method: `Jornada.nueva(roundNumber, league, season, status, matchCount)`
- Método helper: `isPlayable()` → retorna true si status == IN_PROGRESS

**Trazabilidad**:
- CA-0001.1: Estructura de datos ✓
- CA-0001.2: Estados sincronizados ✓
- CA-0001.3: Identidad única (validado en DB con UNIQUE constraint)

**Archivos**:
- `src/main/java/com/example/football/jornadas/domain/Jornada.java`
- `src/main/java/com/example/football/jornadas/domain/JornadaStatus.java` (ENUM)
- `src/main/java/com/example/football/jornadas/domain/JornadasException.java`

**Dependencias**: Ninguna (puro dominio)

---

#### T-0002: Puerto y DTO de API-Football para Jornadas

**Objetivo**: Definir contrato de sincronización con API-Football para obtener datos de jornadas.

**Descripción**:
- Crear interfaz `JornadasApiPort.java` en `com.example.football.jornadas.application.ports`
  - Método: `getJornadas(league: String, season: Int): List<JornadaDto>`
  - Método: `getJornadaStatus(league: String, season: Int, round: Int): JornadaDto`
- Extender `ApiFootballDtos.java` con nuevos DTOs:
  - `JornadaDto`: roundNumber, league, season, status, matchCount, timestamp
  - Usar @JsonIgnoreProperties para flexibilidad con API
- Crear `JornadasApiException.java` extends `EstadisticasException`
  - Para errores de conectividad específicos de jornadas

**Trazabilidad**:
- CA-0001.1: Estructura de datos (API mapeada a DTO) ✓
- CA-0001.2: Estados de API → estado interno ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/application/ports/JornadasApiPort.java`
- `src/main/java/com/example/football/jornadas/infrastructure/dtos/JornadaDto.java` (en ApiFootballDtos.java)
- `src/main/java/com/example/football/jornadas/domain/JornadasApiException.java`

**Dependencias**: T-0001 (requiere JornadaStatus)

---

#### T-0003: Adaptador de Cliente API para Jornadas

**Objetivo**: Implementar cliente HTTP para consumir endpoint de jornadas desde API-Football.

**Descripción**:
- Crear clase `JornadasApiClientAdapter.java` en `com.example.football.jornadas.infrastructure.adapters`
- Implementar interfaz `JornadasApiPort`
- Usar RestTemplate (heredado de CHG-0001)
- Método `getJornadas(league, season)`:
  - Llamada GET a `/fixtures?league={league_id}&season={season}`
  - Extrae lista de fixtures
  - Agrupa por roundNumber
  - Mapea cada group a `JornadaDto`
- Método `getJornadaStatus(league, season, round)`:
  - GET a `/fixtures?league={league_id}&season={season}&round={round}`
  - Extrae estado dominante (NOT_STARTED, IN_PROGRESS, FINISHED, POSTPONED)
- Incluir retry logic (heredado de T-0001 de CHG-0001):
  - Exponential backoff: 1s, 2s, 4s
  - Máximo 3 intentos
  - Rate limit handling (HTTP 429)

**Trazabilidad**:
- CA-0001.1: Obtención de datos ✓
- CA-0001.2: Mapeo de estados ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/infrastructure/adapters/JornadasApiClientAdapter.java`

**Dependencias**: T-0002 (requiere puertos y DTOs)

---

#### T-0004: Servicio de Mapeo de Jornadas

**Objetivo**: Convertir DTOs de API-Football a entidades de dominio `Jornada`.

**Descripción**:
- Crear clase `JornadasMapperService.java` en `com.example.football.jornadas.application.services`
- Método: `mapDtoToJornada(JornadaDto): Jornada`
  - Convierte estado API → JornadaStatus (ej: "Not Started" → NOT_STARTED)
  - Extrae roundNumber, league, season, matchCount
  - Genera timestamps
  - Valida estructura en constructor de Jornada
- Método: `mapStatusApiToDomain(String apiStatus): JornadaStatus`
  - Mapeo explícito: "Not Started" → NOT_STARTED, "In Progress" → IN_PROGRESS, etc.
  - Lanza `JornadasException` si status desconocido
- Método: `validateJornadaStructure(JornadaDto): Boolean`
  - Verifica campos requeridos no nulos
  - Verifica tipos correctos

**Trazabilidad**:
- CA-0001.1: Mapeo de estructura ✓
- CA-0001.2: Mapeo de estados ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/application/services/JornadasMapperService.java`

**Dependencias**: T-0001, T-0002

---

### Requisito RF-0002: Bloqueo de partidos fuera de jornada real

---

#### T-0005: Entidad de Dominio `JornadaValidator`

**Objetivo**: Crear especificación de dominio que valida si se puede jugar un partido en una jornada.

**Descripción**:
- Crear clase `PuedoJugarPartidoEnJornada.java` (especificación DDD) en `com.example.football.jornadas.domain`
- Constructor: `PuedoJugarPartidoEnJornada(Jornada jornada, Usuario jugador)`
- Método: `esValida(): Boolean`
  - Retorna true si jornada.status == IN_PROGRESS
  - Retorna false si status == NOT_STARTED, FINISHED, POSTPONED
- Método: `obtenerMensajeError(): String`
  - Retorna mensaje descriptivo según status:
    - NOT_STARTED: "La jornada XX aún no ha comenzado"
    - FINISHED: "La jornada XX ya ha finalizado"
    - POSTPONED: "La jornada XX ha sido aplazada"

**Trazabilidad**:
- CA-0002.1: Bloqueo NOT_STARTED ✓
- CA-0002.2: Permiso IN_PROGRESS ✓
- CA-0002.3: Bloqueo FINISHED ✓
- CA-0002.4: Bloqueo POSTPONED ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/domain/PuedoJugarPartidoEnJornada.java`

**Dependencias**: T-0001

---

#### T-0006: Puertos de Repositorio para Jornadas

**Objetivo**: Definir contratos de persistencia para acceder a jornadas.

**Descripción**:
- Crear interfaz `JornadasRepositoryPort.java` en `com.example.football.jornadas.application.ports`
  - Método: `save(jornada: Jornada): Jornada`
  - Método: `update(jornada: Jornada): Jornada`
  - Método: `findByRound(league: String, season: Int, roundNumber: Int): Optional<Jornada>`
  - Método: `findAllByLeagueAndSeason(league: String, season: Int): List<Jornada>`
  - Método: `existsByRound(league: String, season: Int, roundNumber: Int): Boolean`
- Método: `findByRoundAndId(league: String, season: Int, roundNumber: Int, jornada_id: UUID): Optional<Jornada>`

**Trazabilidad**:
- CA-0001.3: Identidad única (query por league+season+round) ✓
- CA-0001.4: Sincronización incremental (findByRound existente) ✓
- CA-0002.5: Búsqueda de jornada para validación ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/application/ports/JornadasRepositoryPort.java`

**Dependencias**: T-0001

---

#### T-0007: JPA Entities y Repositorios para Jornadas

**Objetivo**: Implementar persistencia PostgreSQL de jornadas.

**Descripción**:
- Crear `JornadaJpaEntity.java` en `com.example.football.jornadas.infrastructure.persistence`
  - Tabla: `jornadas`
  - Campos: id (PK), round_number, league, season, status (VARCHAR), match_count, created_at, synchronized_at, updated_at
  - Índices:
    - UNIQUE (league, season, round_number) para CA-0001.3
    - INDEX on (league, season) para búsquedas
    - INDEX on (status) para filtros de bloqueo
  - Relación: @OneToMany hacia PartidoJpaEntity (fk jornada_id)
- Crear `JornadaJpaRepository.java` extends JpaRepository<JornadaJpaEntity, String>
  - Método: `findByLeagueAndSeasonAndRoundNumber(String, Integer, Integer): Optional<JornadaJpaEntity>`
  - Método: `findByLeagueAndSeason(String, Integer): List<JornadaJpaEntity>`
  - Método: `existsByLeagueAndSeasonAndRoundNumber(String, Integer, Integer): Boolean`
- Usar @JsonFormat, @Convert para mapeo de status enum ↔ VARCHAR

**Trazabilidad**:
- CA-0001.3: Identidad única (UNIQUE constraint) ✓
- CA-0002.1-0002.5: Búsquedas eficientes por jornada ✓
- CA-0004.3: FK constraint (relación con partidos) ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/infrastructure/persistence/JornadaJpaEntity.java`
- `src/main/java/com/example/football/jornadas/infrastructure/persistence/JornadaJpaRepository.java`

**Dependencias**: T-0001, T-0006

---

#### T-0008: Adaptador de Repositorio para Jornadas

**Objetivo**: Convertir entre entidades de dominio y JPA entities para jornadas.

**Descripción**:
- Crear `JornadasRepositoryAdapter.java` en `com.example.football.jornadas.infrastructure.adapters`
- Implementar `JornadasRepositoryPort`
- Inyectar `JornadaJpaRepository`
- Método `save(Jornada): Jornada`
  - Convierte Jornada → JornadaJpaEntity
  - Persiste en BD
  - Convierte resultado → Jornada
- Método `update(Jornada): Jornada`
  - Similar a save pero usando merge
  - Actualiza `synchronized_at` timestamp
- Método `findByRound(league, season, roundNumber): Optional<Jornada>`
  - Llamada a repositorio Spring Data
  - Mapeo JornadaJpaEntity → Jornada
- Método `findAllByLeagueAndSeason(league, season): List<Jornada>`
  - Retorna lista de jornadas sincronizadas
- Usar ObjectMapper para conversiones (patrón de CHG-0001)

**Trazabilidad**:
- CA-0001.1-0001.4: Persistencia completa ✓
- CA-0002.1-0002.5: Búsquedas de validación ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/infrastructure/adapters/JornadasRepositoryAdapter.java`

**Dependencias**: T-0001, T-0006, T-0007

---

#### T-0009: Servicio de Sincronización Inicial de Jornadas

**Objetivo**: Crear servicio que sincroniza jornadas inicialmente desde API-Football a BD.

**Descripción**:
- Crear `SincronizarJornadasService.java` en `com.example.football.jornadas.application.services`
- Inyectar: `JornadasApiPort`, `JornadasRepositoryPort`, `JornadasMapperService`, `SyncLogsRepositoryPort`
- Método: `sincronizarJornadas(league: String, season: Int): SyncResult`
  - Obtiene lista de jornadas desde API via `JornadasApiPort.getJornadas(league, season)`
  - Para cada JornadaDto:
    - Mapea a Jornada via `JornadasMapperService`
    - Verifica si existe via `findByRound(league, season, round)`
    - Si no existe: inserta con `save()`
    - Si existe: actualiza con `update()` solo si status cambió
  - Registra cada operación en `sync_logs` con:
    - action: "JORNADA_CREATED" o "JORNADA_UPDATED"
    - status: SUCCESS o FAILURE
    - detalles: roundNumber, oldStatus, newStatus
  - Retorna SyncResult con conteo de creadas/actualizadas/errores
- Manejo de errores: captura excepciones de API, registra en logs, continúa con siguientes jornadas
- Transacción: @Transactional para atomicidad

**Trazabilidad**:
- CA-0001.1: Creación de estructura ✓
- CA-0001.4: Sincronización incremental ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/application/services/SincronizarJornadasService.java`
- `src/main/java/com/example/football/jornadas/domain/SyncResult.java` (record con conteos)

**Dependencias**: T-0002, T-0004, T-0006, T-0008, CHG-0001 (SyncLogsRepositoryPort)

---

### Requisito RF-0003: Actualización automática de estado de jornadas

---

#### T-0010: Job Programado de Sincronización de Jornadas

**Objetivo**: Crear tarea programada que actualiza estado de jornadas diariamente.

**Descripción**:
- Crear `SincronizarJornadasJob.java` en `com.example.football.jornadas.infrastructure.jobs`
- Anotar con `@Service` y `@EnableScheduling` (si no existe globalmente)
- Método: `ejecutarSincronizacion()` anotado con `@Scheduled(cron = "0 0 3 * * *")` (03:00 UTC)
  - Itera sobre ligas configuradas: ["LaLiga", "Premier League", "Serie A", "Bundesliga", "Ligue 1"]
  - Para cada liga + season actual:
    - Llama a `SincronizarJornadasService.sincronizarJornadas(liga, season)`
    - Registra resultado en logs
  - Captura excepciones globales, registra en logs con timestamp
  - No lanza excepciones (no interrumpe otras tareas)
- Inyectar: `SincronizarJornadasService`, `Logger`
- Configuración: Properties para:
  - Horario cron (configurable)
  - Ligas a sincronizar
  - Temporada actual

**Trazabilidad**:
- CA-0003.1: Job programado diario ✓
- CA-0003.2-0003.5: Cambios de estado sincronizados y registrados ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/infrastructure/jobs/SincronizarJornadasJob.java`
- `application.properties` (adiciones: jornadas.sync.cron, jornadas.sync.leagues)

**Dependencias**: T-0009

---

#### T-0011: Servicio de Actualización de Estado de Jornada

**Objetivo**: Crear lógica para detectar y actualizar cambios de estado de jornada.

**Descripción**:
- Crear `ActualizarEstadoJornadaService.java` en `com.example.football.jornadas.application.services`
- Método: `actualizarEstados(jornadas: List<Jornada>): UpdateResult`
  - Para cada jornada existente:
    - Obtiene estado actual de API via `JornadasApiPort.getJornadaStatus(league, season, round)`
    - Compara con estado local
    - Si cambió:
      - Valida nuevo estado (es un JornadaStatus válido)
      - Actualiza via repositorio
      - Registra en sync_logs con oldStatus, newStatus
    - Si no cambió: omite
  - Registra timestamp de actualización (`synchronized_at`)
  - Retorna UpdateResult con conteo de cambios
- Transacción: @Transactional
- Reintentos: Si API falla, usa último estado conocido (RNF-0002)

**Trazabilidad**:
- CA-0003.2: Transición NOT_STARTED → IN_PROGRESS ✓
- CA-0003.3: Transición IN_PROGRESS → FINISHED ✓
- CA-0003.4: Detección de POSTPONED ✓
- CA-0003.5: Registro en sync_logs ✓

**Archivos**:
- `src/main/java/com/example/football/jornadas/application/services/ActualizarEstadoJornadaService.java`
- `src/main/java/com/example/football/jornadas/domain/UpdateResult.java` (record)

**Dependencias**: T-0002, T-0006, T-0008

---

### Requisito RF-0004: Asociación de partidos a jornadas reales

---

#### T-0012: Migración de BD: Agregar FK a Jornadas en Partidos

**Objetivo**: Crear y ejecutar migración SQL para agregar referencia de jornada a partidos.

**Descripción**:
- Crear archivo `V004__add_jornada_reference_to_partidos.sql`
- Operaciones SQL:
  1. Crear tabla `jornadas` (si no existe de T-0007):
     ```sql
     CREATE TABLE jornadas (
       id UUID PRIMARY KEY,
       league VARCHAR(50) NOT NULL,
       season INTEGER NOT NULL,
       round_number INTEGER NOT NULL,
       status VARCHAR(20) NOT NULL,
       match_count INTEGER NOT NULL,
       created_at TIMESTAMP NOT NULL,
       synchronized_at TIMESTAMP NOT NULL,
       updated_at TIMESTAMP,
       UNIQUE(league, season, round_number)
     );
     ```
  2. Agregar índices:
     ```sql
     CREATE INDEX idx_jornada_league_season ON jornadas(league, season);
     CREATE INDEX idx_jornada_status ON jornadas(status);
     ```
  3. Agregar columna FK a partidos:
     ```sql
     ALTER TABLE partidos ADD COLUMN jornada_id UUID;
     ALTER TABLE partidos ADD CONSTRAINT fk_partidos_jornada 
       FOREIGN KEY (jornada_id) REFERENCES jornadas(id) ON DELETE RESTRICT;
     CREATE INDEX idx_partido_jornada ON partidos(jornada_id);
     ```
  4. Nota: Partidos existentes quedarán con jornada_id = NULL hasta que se completen migraciones posteriores

**Trazabilidad**:
- CA-0004.2: Referencia FK ✓
- CA-0004.3: Constraint de integridad ✓

**Archivos**:
- `src/main/resources/db/migration/V004__add_jornada_reference_to_partidos.sql`

**Dependencias**: T-0007 (tabla jornadas debe existir)

---

#### T-0013: Validador de Jornada en Creación de Partidos

**Objetivo**: Implementar lógica que valida existencia de jornada al crear/jugar un partido.

**Descripción**:
- Crear `PartidoJornadaValidator.java` en `com.example.football.partidos.domain`
- Interfaz: `JornadaValidator` (a ser inyectada)
  - Método: `validarJornadaExiste(league, season, roundNumber): Optional<Jornada>`
  - Método: `validarJornadaDisponible(jornada): Boolean` (verifica status IN_PROGRESS)
- Crear excepción `PartidoJornadaBloqueadaException.java` en `com.example.football.partidos.domain`:
  - Campos: `errorCode` (MATCH_BLOCKED_ROUND_NOT_STARTED, etc.), `message`, `roundNumber`
  - Subtipo de `PartidosException`
- En especificación DDD existente (o crear `PuedoCrearPartido.java`):
  - Agregar validación: "¿Existe jornada real para este partido?"
  - Si no existe: `esValida() = false`
  - Si existe pero status != IN_PROGRESS: `esValida() = false`

**Trazabilidad**:
- CA-0002.1-0002.5: Bloqueos de partidos ✓
- CA-0004.1: Validación de jornada al crear ✓

**Archivos**:
- `src/main/java/com/example/football/partidos/domain/PartidoJornadaValidator.java`
- `src/main/java/com/example/football/partidos/domain/PartidoJornadaBloqueadaException.java`
- Modificación: `src/main/java/com/example/football/partidos/domain/PuedoCrearPartido.java` (si existe) o crear nueva

**Dependencias**: T-0005, T-0006, T-0001

---

#### T-0014: Adaptador de Inyección de Jornadas en Partidos

**Objetivo**: Conectar repositorio de jornadas con validador de partidos.

**Descripción**:
- Crear `PartidoJornadaValidatorAdapter.java` en `com.example.football.partidos.infrastructure.adapters`
- Implementar interfaz `JornadaValidator`
- Inyectar `JornadasRepositoryPort`
- Método `validarJornadaExiste(league, season, roundNumber): Optional<Jornada>`
  - Llamada a `JornadasRepositoryPort.findByRound(league, season, roundNumber)`
  - Retorna Optional
- Método `validarJornadaDisponible(jornada): Boolean`
  - Verifica `jornada.isPlayable()` (equals IN_PROGRESS)
- Usar en capas de aplicación al crear/validar partidos

**Trazabilidad**:
- CA-0004.1: Validación antes de crear ✓
- CA-0002.1-0002.5: Bloqueos efectivos ✓

**Archivos**:
- `src/main/java/com/example/football/partidos/infrastructure/adapters/PartidoJornadaValidatorAdapter.java`

**Dependencias**: T-0013, T-0006, T-0008

---

#### T-0015: Servicio de Visualización de Partidos Disponibles

**Objetivo**: Crear servicio que filtra partidos según disponibilidad de jornada.

**Descripción**:
- Crear `PartidosDisponiblesService.java` en `com.example.football.partidos.application.services`
- Método: `obtenerPartidosDisponibles(usuario: Usuario, liga: String, season: Int): List<Partido>`
  - Obtiene partidos por liga/season del repositorio de partidos
  - Filtra solo aquellos cuya jornada tiene status = IN_PROGRESS
  - Retorna lista filtrada
  - Nota: Partidos sin jornada_id se omiten (no deberían existir post-T-0013)
- Inyectar: `PartidosRepositoryPort`, `JornadasRepositoryPort`

**Trazabilidad**:
- CA-0004.4: Visualización filtrada ✓

**Archivos**:
- `src/main/java/com/example/football/partidos/application/services/PartidosDisponiblesService.java`

**Dependencias**: T-0006, T-0013

---

## 3. Tareas de Testing y Documentación

---

#### T-0016: Pruebas de Aceptación para Jornadas

**Objetivo**: Crear suite de pruebas que validan todos los CA de CHG-0006.

**Descripción**:
- Crear `RF0006_JornadasSincronizacionAcceptanceTest.java` en `src/test/acceptance/java/com/example/football/jornadas/acceptance/`
  - Clase base con @SpringBootTest, @ActiveProfiles("test")
  - Test methods para cada CA:
    - CA-0001.1: testEstructuraDatos()
    - CA-0001.2: testEstadosSincronizados()
    - CA-0001.3: testIdentidadUnica()
    - CA-0001.4: testSincronizacionIncremental()
    - CA-0002.1 a CA-0002.5: testBloqueoPartidos_*()
    - CA-0003.1 a CA-0003.5: testActualizacionAutomatica_*()
    - CA-0004.1 a CA-0004.4: testAsociacionPartidos_*()

- Crear `RF0006_JornadasValidacionAcceptanceTest.java` en `src/test/acceptance/java/com/example/football/jornadas/acceptance/`
  - Pruebas de validación de dominio

- Usar AssertJ, JUnit 5, sin MockMvc (similares a tests de CHG-0001)

**Trazabilidad**: Todos los CA (CA-0001.1 a CA-0004.4) ✓

**Archivos**:
- `src/test/acceptance/java/com/example/football/jornadas/acceptance/RF0006_JornadasSincronizacionAcceptanceTest.java`
- `src/test/acceptance/java/com/example/football/jornadas/acceptance/RF0006_JornadasValidacionAcceptanceTest.java`

**Dependencias**: T-0001 a T-0015

---

#### T-0017: Actualización de pom.xml

**Objetivo**: Agregar dependencias necesarias para CHG-0006.

**Descripción**:
- Si no están presentes (verificar si ya existen de CHG-0001):
  - spring-boot-starter-data-jpa
  - postgresql driver
  - spring-boot-starter-test
  - junit-jupiter
  - assertj
- Verificar que versiones son compatibles con Spring Boot 4.1.1

**Archivos**:
- `pom.xml` (sin modificaciones si CHG-0001 ya está completo)

**Dependencias**: Ninguna

---

#### T-0018: Documentación: Ejemplos de Uso

**Objetivo**: Crear ejemplos de uso de CHG-0006 para desarrolladores.

**Descripción**:
- Crear archivo `src/main/java/com/example/football/jornadas/README.md`
- Secciones:
  - Arquitectura de jornadas
  - Cómo sincronizar jornadas
  - Cómo validar si se puede jugar un partido
  - Ejemplos de código
  - Estados de jornada y transiciones
  - Integración con CHG-0001
  - Troubleshooting

**Archivos**:
- `src/main/java/com/example/football/jornadas/README.md`

**Dependencias**: Ninguna

---

## 4. Matriz de Trazabilidad Completa

| Requisito | CA | Tareas |
|-----------|-----|--------|
| RF-0001 | CA-0001.1 | T-0001, T-0002, T-0004, T-0009 |
| RF-0001 | CA-0001.2 | T-0001, T-0002, T-0003, T-0004, T-0009 |
| RF-0001 | CA-0001.3 | T-0001, T-0007, T-0008, T-0009 |
| RF-0001 | CA-0001.4 | T-0001, T-0006, T-0008, T-0009 |
| RF-0002 | CA-0002.1 | T-0001, T-0005, T-0006, T-0013, T-0014 |
| RF-0002 | CA-0002.2 | T-0001, T-0005, T-0006, T-0013, T-0014 |
| RF-0002 | CA-0002.3 | T-0001, T-0005, T-0006, T-0013, T-0014 |
| RF-0002 | CA-0002.4 | T-0001, T-0005, T-0006, T-0013, T-0014 |
| RF-0002 | CA-0002.5 | T-0001, T-0005, T-0006, T-0013, T-0014 |
| RF-0003 | CA-0003.1 | T-0010 |
| RF-0003 | CA-0003.2 | T-0001, T-0010, T-0011 |
| RF-0003 | CA-0003.3 | T-0001, T-0010, T-0011 |
| RF-0003 | CA-0003.4 | T-0001, T-0010, T-0011 |
| RF-0003 | CA-0003.5 | T-0001, T-0010, T-0011 (requiere sync_logs de CHG-0001) |
| RF-0004 | CA-0004.1 | T-0006, T-0013, T-0014 |
| RF-0004 | CA-0004.2 | T-0007, T-0012 |
| RF-0004 | CA-0004.3 | T-0007, T-0012 |
| RF-0004 | CA-0004.4 | T-0006, T-0013, T-0015 |

---

## 5. Dependencias Inter-Tareas

```
T-0001 (Jornada domain)
  ↓
T-0002 (API Port) → T-0003 (API Client)
  ↓
T-0004 (Mapper Service)
  ↓
T-0006 (Repository Port)
  ↓
T-0007 (JPA Entity) → T-0008 (Repository Adapter)
  ↓
T-0009 (Sync Service)
  ↓
T-0010 (Job)
  ↓
T-0011 (Update Service)

Paralelo (no depende de T-0001-T-0011):
T-0005 (JornadaValidator)
  ↓
T-0013 (Partido Validator)
  ↓
T-0014 (Adapter)

T-0012 (BD Migration) - Depende de T-0007
  ↓
T-0015 (Partidos disponibles)

T-0016 (Pruebas) - Depende de todas T-0001 a T-0015
T-0017 (pom.xml) - No depende
T-0018 (Docs) - No depende
```

---

## 6. Estimaciones

| Tarea | Complejidad | Estimado | Horas |
|-------|-------------|----------|-------|
| T-0001 | Baja | 1-2h | 1.5h |
| T-0002 | Baja | 1-2h | 1.5h |
| T-0003 | Media | 3-4h | 3.5h |
| T-0004 | Baja | 1-2h | 1.5h |
| T-0005 | Baja | 1h | 1h |
| T-0006 | Baja | 1h | 1h |
| T-0007 | Media | 2-3h | 2.5h |
| T-0008 | Media | 2-3h | 2.5h |
| T-0009 | Media | 3-4h | 3.5h |
| T-0010 | Baja | 1-2h | 1.5h |
| T-0011 | Media | 2-3h | 2.5h |
| T-0012 | Baja | 1h | 1h |
| T-0013 | Media | 2-3h | 2.5h |
| T-0014 | Baja | 1-2h | 1.5h |
| T-0015 | Baja | 1-2h | 1.5h |
| T-0016 | Alta | 6-8h | 7h |
| T-0017 | Baja | 0.5h | 0.5h |
| T-0018 | Baja | 1-2h | 1.5h |
| **TOTAL** | **Media** | **38-55h** | **43h** |

---

## 7. Criterios de Aceptación de Tareas

### Por tarea:
- ✅ Código compila sin errores
- ✅ Sigue patrones de proyecto (hexagonal, records, records para DTOs)
- ✅ Tests de aceptación pasan
- ✅ Documentación incluida
- ✅ Cumple con verificación de RNF

### Global (para considerar CHG-0006 completo):
- ✅ Todas las tareas T-0001 a T-0018 completadas
- ✅ 18 criterios de aceptación (CA-0001.1 a CA-0004.4) verificados
- ✅ RNF-0001 a RNF-0006 validados
- ✅ Sync logs contienen mínimo 2 ciclos de sincronización
- ✅ Base de datos con datos reales sincronizados desde API-Football
- ✅ Partidos bloqueados correctamente cuando jornada no está IN_PROGRESS
- ✅ Code review completado

---

## 8. Próximos Pasos

1. Aprobación de este tasks.md por Product Owner
2. Priorización: Orden recomendado:
   - Fase 1 (Dominio): T-0001, T-0005 (entidades base)
   - Fase 2 (Infraestructura): T-0002, T-0003, T-0007 (acceso a datos)
   - Fase 3 (Servicios): T-0004, T-0008, T-0009 (lógica core)
   - Fase 4 (Validación): T-0006, T-0013, T-0014 (bloqueos)
   - Fase 5 (Actualización): T-0010, T-0011 (jobs)
   - Fase 6 (Integración): T-0012, T-0015 (asociación con partidos)
   - Fase 7 (Testing & Docs): T-0016, T-0017, T-0018

3. Implementación: Una vez aprobado, proceder con T-0001 en adelante
4. Generación de evidence.md después de completar todas las tareas

---

**Estado final**: Listo para implementación

