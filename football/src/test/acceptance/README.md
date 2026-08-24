# Pruebas de Aceptación - CHG-0001

## Visión General

Este documento explica cómo ejecutar y validar las pruebas de aceptación para CHG-0001 (Integración de estadísticas reales desde API-Football).

Las pruebas están organizadas por Requisito Funcional (RF) y cubren todos los Criterios de Aceptación (CA) especificados en `requirements.md`.

---

## 1. Estructura de Pruebas

```
src/test/acceptance/
└── java/com/example/football/estadisticas/acceptance/
    ├── RF0001_ApiFootballConsumptionAcceptanceTest.java
    ├── RF0002_StatsMapperAcceptanceTest.java
    ├── RF0003_SyncOrchestrationAcceptanceTest.java
    └── RF0004_PersistenceAcceptanceTest.java
```

### Mapeo de Pruebas a Requisitos

| Requisito | Clase de Prueba | CA Cubiertas | Estado |
|-----------|-----------------|--------------|--------|
| RF-0001: Consumo API | `RF0001_ApiFootballConsumptionAcceptanceTest` | CA-0001.1 a CA-0001.5 | ✅ Listo |
| RF-0002: Mapeo de Stats | `RF0002_StatsMapperAcceptanceTest` | CA-0002.1 a CA-0002.3 | ✅ Listo |
| RF-0003: Sincronización | `RF0003_SyncOrchestrationAcceptanceTest` | CA-0003.1 a CA-0003.5 | ⏳ Pendiente* |
| RF-0004: Persistencia | `RF0004_PersistenceAcceptanceTest` | CA-0004.1 a CA-0004.4 | ✅ Listo |

*RF-0003 requiere implementación de `RoundCompletionDetector`, `SyncOrchestrator`, `SyncEstadisticasJob` antes de poder ejecutarse.

---

## 2. Cómo Ejecutar

### 2.1 Ejecutar Todas las Pruebas de Aceptación

```bash
cd c:\Users\mucho\Desktop\ProyectoFootball\football

# Ejecutar todos los tests acceptance
mvn test -Dtest=*AcceptanceTest

# O más específicamente:
mvn test -Dtest=RF*AcceptanceTest
```

### 2.2 Ejecutar Pruebas de un RF Específico

```bash
# Solo RF-0001
mvn test -Dtest=RF0001_ApiFootballConsumptionAcceptanceTest

# Solo RF-0002
mvn test -Dtest=RF0002_StatsMapperAcceptanceTest

# Solo RF-0004
mvn test -Dtest=RF0004_PersistenceAcceptanceTest
```

### 2.3 Ejecutar Prueba Específica

```bash
# Solo CA-0001.1
mvn test -Dtest=RF0001_ApiFootballConsumptionAcceptanceTest#testAuthenticationHeaders

# Solo CA-0002.1
mvn test -Dtest=RF0002_StatsMapperAcceptanceTest#testMapPlayerDtoToPlayer
```

### 2.4 Con Reporte Detallado

```bash
mvn test -Dtest=*AcceptanceTest -X

# O con salida a archivo
mvn test -Dtest=*AcceptanceTest > test_results.txt 2>&1
```

---

## 3. Estructura de Cada Prueba

Cada prueba de aceptación sigue el patrón **Dado/Cuando/Entonces**:

```java
@Test
@DisplayName("CA-XXXX: Descripción del escenario")
void testScenario() {
    // DADO: Configurar precondiciones
    // ...
    
    // CUANDO: Ejecutar la acción
    // ...
    
    // ENTONCES: Verificar resultados
    assertThat(...).isEqualTo(...);
}
```

### Ejemplo: CA-0001.1

```java
@Test
@DisplayName("CA-0001.1: Autenticación con API-Football usando headers correctos")
void testAuthenticationHeaders() {
    // Dado: API key válida en configuración
    String expectedApiKey = "test-key";
    
    // Cuando: Se prepara request a API
    when(restTemplate.exchange(...)).thenReturn(new ResponseEntity<>(...));
    
    // Entonces: Se verifica que headers fueron agregados
    verify(restTemplate, times(1)).exchange(...);
}
```

---

## 4. Dependencias de Test

Las pruebas usan:

- **JUnit 5**: Framework de testing
- **AssertJ**: Assertions fluidas (`assertThat`)
- **Mockito**: Mocking de dependencias
- **Spring Test**: Integración con Spring (@SpringBootTest, @MockBean)

Todas estas dependencias ya están en `pom.xml`.

---

## 5. Perfil Activo para Tests

Las pruebas se ejecutan con perfil `dev`:

```java
@SpringBootTest
@ActiveProfiles("dev")  // Lee application-dev.properties
public class RF0001_ApiFootballConsumptionAcceptanceTest {
    // ...
}
```

Asegúrate de que `src/main/resources/application-dev.properties` esté configurado correctamente.

---

## 6. Resultados Esperados

### 6.1 Pruebas que Deberían Pasar Ahora (RF-0001, 0002, 0004)

```
RF0001_ApiFootballConsumptionAcceptanceTest
  ✅ CA-0001.1: testAuthenticationHeaders
  ✅ CA-0001.2: testGetPlayersFromApiFootball
  ✅ CA-0001.3: testGetFixturesFromApiFootball
  ✅ CA-0001.4: testRateLimitHandling
  ✅ CA-0001.5: testConnectivityErrorRetry

RF0002_StatsMapperAcceptanceTest
  ✅ CA-0002.1: testMapPlayerDtoToPlayer
  ✅ CA-0002.2: testNormalizeStatisticsTo0To100
  ✅ CA-0002.3: testValidatePlayerIntegrity
  ✅ CA-0002.3: testValidatePlayerWithEmptyName
  ✅ CA-0002.3: testValidatePlayerWithInvalidAge
  ✅ CA-0002.3: testValidatePlayerWithNegativeStats
  ✅ CA-0002.3: testValidatePlayerWithInvalidPercentages

RF0004_PersistenceAcceptanceTest
  ✅ CA-0004.1: testPersistPlayerWithCorrectStructure
  ✅ CA-0004.1: testFindPlayerByExternalId
  ✅ CA-0004.1: testFindPlayerByTeamId
  ✅ CA-0004.2: testPersistMatchWithCorrectStructure
  ✅ CA-0004.2: testFindMatchByFixtureId
  ✅ CA-0004.2: testFindMatchesByRound
  ✅ CA-0004.3: testPersistSyncLogForAudit
  ✅ CA-0004.3: testFindSyncLogByStatus
  ✅ CA-0004.3: testFindLastSuccessfulSync
  ✅ CA-0004.4: testUpdateExistingPlayer
  ✅ CA-0004.4: testReferentialIntegrityForPlayer
  ✅ CA-0004.4: testTransactionalMultipleOperations

TOTAL ESPERADO: 23 tests ✅ PASANDO
```

### 6.2 Pruebas que Están Pendientes (RF-0003)

```
RF0003_SyncOrchestrationAcceptanceTest
  ⏳ CA-0003.1: testDetectRoundCompletion_PENDING
  ⏳ CA-0003.2: testScheduledExecution_PENDING
  ⏳ CA-0003.3: testUpdatePlayersAfterRound_PENDING
  ⏳ CA-0003.4: testHandleSyncFailure_PENDING
  ⏳ CA-0003.5: testPreventDuplicates_PENDING

ESTADO: Esperando implementación de servicios de sincronización
```

---

## 7. Troubleshooting

### 7.1 Error: "Cannot find symbol: RestTemplate"

**Problema**: Falta importación de Spring Web.

**Solución**: Asegúrate de que `spring-boot-starter-web` está en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 7.2 Error: "No SqlSession found in transaction synchronization manager"

**Problema**: Base de datos no está inicializada para tests.

**Solución**: 
- Asegúrate de que `application-dev.properties` tiene configuración de BD
- O usa `@DataJpaTest` si necesitas solo JPA

### 7.3 Error: "ClassNotFoundException: com.example.football.estadisticas..."

**Problema**: Compilación incompleta.

**Solución**: Ejecuta `mvn clean compile` antes de tests:

```bash
mvn clean compile
mvn test -Dtest=*AcceptanceTest
```

### 7.4 Timeout en Tests

**Problema**: MockRestTemplate tarda demasiado.

**Solución**: Aumenta timeout en `application-dev.properties`:

```properties
api-football.timeout.seconds=30
```

---

## 8. Integración Continua

Para integrar en CI/CD (GitHub Actions, Jenkins):

```yaml
# .github/workflows/acceptance-tests.yml
name: Acceptance Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '21'
      - run: mvn clean compile
      - run: mvn test -Dtest=*AcceptanceTest
      - uses: actions/upload-artifact@v2
        if: always()
        with:
          name: test-results
          path: target/surefire-reports/
```

---

## 9. Matriz de Validación Final

| CA | Test | Estado | Resultado |
|----|------|--------|-----------|
| CA-0001.1 | `testAuthenticationHeaders` | ⏳ Pendiente | - |
| CA-0001.2 | `testGetPlayersFromApiFootball` | ⏳ Pendiente | - |
| CA-0001.3 | `testGetFixturesFromApiFootball` | ⏳ Pendiente | - |
| CA-0001.4 | `testRateLimitHandling` | ⏳ Pendiente | - |
| CA-0001.5 | `testConnectivityErrorRetry` | ⏳ Pendiente | - |
| CA-0002.1 | `testMapPlayerDtoToPlayer` | ⏳ Pendiente | - |
| CA-0002.2 | `testNormalizeStatisticsTo0To100` | ⏳ Pendiente | - |
| CA-0002.3 | `testValidatePlayer*` | ⏳ Pendiente | - |
| CA-0003.* | `RF0003_*_PENDING` | ⏳ Bloqueado | Espera T-0009 a T-0011 |
| CA-0004.1 | `testPersistPlayer*` | ⏳ Pendiente | - |
| CA-0004.2 | `testPersistMatch*` | ⏳ Pendiente | - |
| CA-0004.3 | `testPersistSyncLog*` | ⏳ Pendiente | - |
| CA-0004.4 | `testUpdate*` | ⏳ Pendiente | - |

---

## 10. Próximos Pasos

1. **Ejecutar pruebas**: `mvn test -Dtest=*AcceptanceTest`
2. **Revisar resultados**: Validar que RF-0001, 0002, 0004 pasen
3. **Documentar hallazgos**: Actualizar `evidence.md` con resultados
4. **Implementar RF-0003**: Una vez que `RoundCompletionDetector` y `SyncOrchestrator` estén listos
5. **Validación final**: Marcar `evidence.md` como `validado`

---

## 11. Referencias

- [requirements.md](../requirements.md) - Requisitos funcionales
- [evidence.md](../evidence.md) - Matriz de pruebas y hallazgos
- [backend-agent.md](../../.agents/backend-agent.md) - Políticas del agente backend
- [tasks.md](../tasks.md) - Tareas técnicas (cuando esté generado)

---

**Última actualización**: 2026-08-24  
**Estado**: ✅ Pruebas de aceptación generadas y listas para ejecutar
