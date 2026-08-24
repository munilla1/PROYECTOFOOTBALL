# CHG-0006: Jornadas Sincronizadas con Partidos Reales

## Descripción General

Este módulo implementa sincronización automática de jornadas (rondas de competición) desde API-Football. 

**Objetivo principal**: Sincronizar jornadas reales desde API-Football y bloquear la creación de partidos fuera del estado permitido.

---

## Arquitectura

### Capas del módulo

```
Presentación (Controllers - no implementados en CHG-0006)
        ↓
Aplicación (Services)
    - SincronizarJornadasService: Orquesta sincronización
    - ActualizarEstadoJornadaService: Detecta cambios de estado
    - JornadasMapperService: Convierte DTOs a dominio
    - PartidosDisponiblesService: Valida disponibilidad
        ↓
Dominio (Entities & Specifications)
    - Jornada (record)
    - JornadaStatus (enum)
    - PuedoJugarPartidoEnJornada (spec)
    - JornadaBloqueadaException
        ↓
Infraestructura (Adapters & Persistence)
    - JornadasApiClientAdapter: HTTP client con retry
    - JornadasRepositoryAdapter: DAO pattern
    - PartidoJornadaValidatorAdapter: Inyección de dependencias
    - JornadaJpaEntity & JornadaJpaRepository
    - SincronizarJornadasJob: Job programado
```

### Patrones aplicados

- **Hexagonal Architecture**: Puertos (interfaces) en aplicación, adaptadores en infraestructura
- **Domain-Driven Design**: Especificaciones, value objects, excepciones de dominio
- **Clean Architecture**: Separación de capas, sin dependencias inversas
- **Repository Pattern**: Abstracción de persistencia
- **Adapter Pattern**: Inyección de dependencias mediante puertos

---

## Dependencias del Proyecto

Las dependencias necesarias ya están incluidas en `pom.xml` desde CHG-0001:

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Logging -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>

<!-- Jackson (ObjectMapper) -->
<!-- Incluida en spring-boot-starter-web -->
```

---

## Configuración (application.properties)

Agregue las siguientes propiedades para configurar el job:

```properties
# Sincronización de Jornadas (CHG-0006)
jornadas.sync.enabled=true
jornadas.sync.cron=0 0 3 * * *
jornadas.sync.current-season=2024
jornadas.sync.leagues=LaLiga,Premier League,Serie A,Bundesliga,Ligue 1
```

### Explicación de propiedades

| Propiedad | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| `jornadas.sync.enabled` | Activa/desactiva job de sincronización | `true` |
| `jornadas.sync.cron` | Expresión cron (03:00 UTC diariamente) | `0 0 3 * * *` |
| `jornadas.sync.current-season` | Temporada a sincronizar | `2024` |
| `jornadas.sync.leagues` | Ligas a sincronizar (separadas por coma) | Predefinidas |

---

## API de Jornadas

### Clases Principales

#### 1. Entidad de Dominio: `Jornada`

```java
public record Jornada(
    UUID id,
    Integer roundNumber,      // 1-38
    String league,            // "LaLiga", "Premier League", etc.
    Integer season,           // 2024, 2025, etc.
    JornadaStatus status,     // NOT_STARTED, IN_PROGRESS, FINISHED, POSTPONED
    Integer matchCount,       // Total de partidos
    Instant createdAt,        // Timestamp de creación
    Instant synchronizedAt    // Último sync desde API
)
```

Factory method:
```java
Jornada jornada = Jornada.nueva(1, "LaLiga", 2024, JornadaStatus.IN_PROGRESS, 10);
```

Métodos helper:
```java
boolean playable = jornada.isPlayable();  // true si status == IN_PROGRESS
String id = jornada.getCompositeId();     // "LaLiga/2024/R01"
```

#### 2. Estados de Jornada

```java
public enum JornadaStatus {
    NOT_STARTED,  // Jornada no ha comenzado aún
    IN_PROGRESS,  // Jornada en curso (solo estado permitido para jugar)
    FINISHED,     // Jornada finalizada
    POSTPONED     // Jornada aplazada
}
```

#### 3. Especificación DDD: `PuedoJugarPartidoEnJornada`

```java
PuedoJugarPartidoEnJornada spec = new PuedoJugarPartidoEnJornada(jornada);

boolean esValida = spec.esValida();  // true si IN_PROGRESS
String error = spec.obtenerMensajeError();  // Mensaje descriptivo
```

---

## Flujo de Sincronización

### Fase 1: Sincronización Inicial

```
SincronizarJornadasJob
    ↓
SincronizarJornadasService.sincronizarJornadas(liga, season)
    ↓ (para cada jornada desde API)
    - Obtiene lista de JornadaDto desde API-Football (con retry)
    - Mapea a entidad Jornada (mapperService)
    - Verifica si existe (findByRound)
    - Si NO existe: inserta nueva
    - Si EXISTE y cambió estado: actualiza
    ↓
Persistencia en BD
    ↓
Registro en sync_logs
```

### Fase 2: Actualización de Estados (Diaria)

```
SincronizarJornadasJob (después de sincronizar)
    ↓
ActualizarEstadoJornadaService.actualizarEstados(jornadasList)
    ↓ (para cada jornada)
    - Obtiene estado actual desde API
    - Compara con estado local
    - Si cambió: actualiza y registra en sync_logs
    ↓
Base de datos actualizada
```

### Validación en Partidos

```
PartidosDisponiblesService.validarPartidoDisponibleEnJornada(liga, season, round)
    ↓
PartidoJornadaValidatorAdapter
    ↓
JornadasRepositoryAdapter.findByRound()
    ↓
SI no existe: throw PartidoJornadaBloqueadaException(JORNADA_NOT_FOUND)
SI existe pero status != IN_PROGRESS: throw PartidoJornadaBloqueadaException(...)
SI existe y IN_PROGRESS: PERMITIR crear partido
```

---

## Excepciones

### Jerarquía de excepciones

```
EstadisticasException (desde CHG-0001)
    ├── JornadasException
    │   ├── JornadasApiException
    │   └── JornadaValidationException
    │
PartidosException
    └── PartidoJornadaBloqueadaException
        - errorCode: JORNADA_NOT_STARTED, JORNADA_FINISHED, JORNADA_POSTPONED, JORNADA_NOT_FOUND
```

### Manejo de errores

```java
try {
    partidosService.crearPartido(usuario, partido, league, season, round);
} catch (PartidoJornadaBloqueadaException e) {
    String errorCode = e.getErrorCode();  // JORNADA_NOT_STARTED, etc.
    Integer roundNumber = e.getRoundNumber();
    String message = e.getMessage();  // Mensaje en español
    
    // Responder al cliente con código de error específico
    switch (errorCode) {
        case JORNADA_NOT_STARTED -> return "Jornada no ha comenzado";
        case JORNADA_FINISHED -> return "Jornada finalizada";
        case JORNADA_POSTPONED -> return "Jornada aplazada";
        case JORNADA_NOT_FOUND -> return "Jornada no existe";
    }
}
```

---

## Retry Logic

El cliente HTTP (`JornadasApiClientAdapter`) implementa reintentos automáticos:

- **Intentos**: Máximo 3
- **Backoff**: Exponencial (1s, 2s, 4s)
- **Manejo de 429**: Captura HTTP 429 (rate limit)
- **Manejo de 5xx**: Captura HTTP 503, 504 (servicios no disponibles)

```java
// Internamente, el adaptador reintentas automáticamente
SyncResult result = sincronizarService.sincronizarJornadas("LaLiga", 2024);
// Si API-Football no responde, se reintenta hasta 3 veces
// Si fallan todos: lanza JornadasApiException
```

---

## Integración con Otros Dominios

### CHG-0001 (Estadísticas)

- **Usa**: `SyncLogsRepositoryPort` para registrar operaciones
- **Mantiene compatibilidad**: Misma estructura de SyncLog

### CHG-0008 (Partidos)

- **Valida**: Antes de permitir crear partido
- **Usa**: `PartidosDisponiblesService`
- **Lanza**: `PartidoJornadaBloqueadaException`

---

## Auditoría y Logging

### Logs de sincronización

Cada operación de sincronización se registra con:

```
action: JORNADA_CREATED, JORNADA_UPDATED, JORNADA_ERROR
status: SUCCESS, FAILURE
details: String con información detallada
timestamp: Instant.now()
```

### Niveles de log

- `INFO`: Inicio/fin de jobs, resumen de operaciones
- `DEBUG`: Detalles de cada jornada (creación, actualización, búsqueda)
- `WARN`: Anomalías (jornadas sin cambios, API lenta)
- `ERROR`: Fallos en operaciones críticas

### Monitoreo

```
2026-08-24 03:00:00 INFO  [SincronizarJornadasJob] === Starting Jornadas Sync Job at 2026-08-24 03:00:00 ===
2026-08-24 03:00:01 INFO  [SincronizarJornadasJob] Syncing 5 leagues for season 2024
2026-08-24 03:00:02 INFO  [SincronizarJornadasService] Fetched 38 jornadas from API for LaLiga 2024
2026-08-24 03:00:05 INFO  [SincronizarJornadasService] Jornadas sync completed for LaLiga season 2024: 38 created, 0 updated, 0 errors
...
2026-08-24 03:02:30 INFO  [SincronizarJornadasJob] === Jornadas Sync Job completed at 2026-08-24 03:02:30 ===
2026-08-24 03:02:30 INFO  [SincronizarJornadasJob] Total results: 190 created, 0 updated, 0 errors in 150000ms
```

---

## Testing

### Estructura de tests (implementados en T-0016)

- `RF0006_JornadasSincronizacionAcceptanceTest`: Tests de sincronización
- `RF0006_JornadasValidacionAcceptanceTest`: Tests de validación

### Ejecutar tests

```bash
mvn test -Dtest=RF0006*AcceptanceTest
```

---

## Troubleshooting

### Problema: "Jornada not found" al crear partido

**Causa**: Jornadas no han sido sincronizadas aún

**Solución**:
1. Verificar que job está habilitado: `jornadas.sync.enabled=true`
2. Ejecutar manualmente:
   ```bash
   curl -X POST /api/jornadas/sync?league=LaLiga&season=2024
   ```
3. Esperar siguiente ejecución del job (03:00 UTC)

### Problema: "Rate limit exceeded" en logs

**Causa**: API-Football devuelve HTTP 429

**Solución**:
1. Cliente reintentar automáticamente (hasta 3 veces)
2. Si persiste: aumentar delay en `RETRY_DELAYS_MS`
3. Contactar soporte API-Football

### Problema: Jornadas con estado NULL en BD

**Causa**: Datos corruptos o migración incompleta

**Solución**:
1. Verificar que V004 se ejecutó: `SELECT * FROM jornadas;`
2. Limpiar datos: `DELETE FROM jornadas WHERE status IS NULL;`
3. Ejecutar sincronización nuevamente

### Problema: "FK constraint violation" al eliminar

**Causa**: Intento de eliminar jornada con partidos asociados

**Solución**:
1. Constraint `ON DELETE RESTRICT` lo impide (correcto)
2. Primero eliminar partidos asociados
3. Luego eliminar jornada

---

## Roadmap Futuro

- [ ] Endpoint REST para sincronización manual `/api/jornadas/sync`
- [ ] Endpoint para ver estado de jornadas `/api/jornadas/{league}/{season}`
- [ ] Notificaciones en tiempo real cuando cambia estado de jornada
- [ ] Métricas de Prometheus para sync success rate
- [ ] Dashboard de auditoría de cambios

---

## Contacto

Para preguntas o reportes de bugs sobre CHG-0006:
- Revisar: [requirements.md](requirements.md)
- Revisar: [tasks.md](tasks.md)
