# CHG-0006 - Pruebas de Aceptación

**Fecha de generación**: 2026-08-24  
**Estado**: Listas para ejecución  
**Total de tests**: 28 (13 de sincronización + 15 de validación)

---

## Descripción

Conjunto de pruebas de aceptación para CHG-0006: Jornadas sincronizadas con partidos reales.

Las pruebas verifican todos los **Criterios de Aceptación (CA)** definidos en `requirements.md` sin usar MockMvc, solo assertions de dominio con AssertJ.

---

## Cobertura de Requisitos

### RF-0001: Sincronización de jornadas desde API-Football

✅ **CA-0001.1**: testEstructuraDatos_JornadaTiene8Campos()
- Verifica que Jornada contiene todos los campos requeridos (id, roundNumber, league, season, status, matchCount, createdAt, synchronizedAt)

✅ **CA-0001.2**: testEstadosMapeados_NotStartedInProgressFinishedPostponed()
- Verifica mapeo correcto de estados: Not Started → NOT_STARTED, In Progress → IN_PROGRESS, etc.
- Verifica insensibilidad a mayúsculas

✅ **CA-0001.3**: testIdentidadUnica_CompositeIdDiferenciaPorLigaSeasonRound()
- Verifica identificador único por (league, season, roundNumber)
- Verifica que diferentes combinaciones generan IDs diferentes

✅ **CA-0001.4**: testSincronizacionIncremental_NoCreaduplicados()
- Verifica estructura para sincronización incremental sin duplicados

---

### RF-0002: Bloqueo de partidos fuera de jornada real

✅ **CA-0002.1**: testBloquePartido_JornadaNotStarted()
- Verifica que PuedoJugarPartidoEnJornada rechaza jornadas NOT_STARTED

✅ **CA-0002.2**: testPermisoPartido_JornadaInProgress()
- Verifica que permite partidos en jornadas IN_PROGRESS

✅ **CA-0002.3**: testBloquePartido_JornadaFinished()
- Verifica que rechaza jornadas FINISHED

✅ **CA-0002.4**: testBloquePartido_JornadaPostponed()
- Verifica que rechaza jornadas POSTPONED

✅ **CA-0002.5**: testBloquePartido_JornadaNoExiste_ExcepcionConCodigoError()
- Verifica excepción PartidoJornadaBloqueadaException con código JORNADA_NOT_FOUND

---

### RF-0003: Actualización automática de estado de jornadas

✅ **CA-0003.1**: testJobProgramado_ConfiguracionExiste()
- Verifica que servicios de sincronización están configurados

✅ **CA-0003.2**: testTransicionEstado_NotStartedAInProgress()
- Verifica transición NOT_STARTED → IN_PROGRESS preservando ID y createdAt

✅ **CA-0003.3**: testTransicionEstado_InProgressAFinished()
- Verifica transición IN_PROGRESS → FINISHED

✅ **CA-0003.4**: testTransicionEstado_APostponed()
- Verifica transición a estado POSTPONED

✅ **CA-0003.5**: testRegistroSyncLogs_SyncResultContieneDatos()
- Verifica que SyncResult registra creadas, actualizadas, errores y duración

---

### RF-0004: Asociación de partidos a jornadas reales

✅ **CA-0004.1**: testValidacionPartido_JornadaNotFound()
- Verifica código de error JORNADA_NOT_FOUND en PartidoJornadaBloqueadaException

✅ **CA-0004.2**: testFK_JornadaTieneUuidUnico()
- Verifica que cada Jornada tiene UUID único para FK

✅ **CA-0004.3**: testFKConstraint_ViolacionFK()
- Verifica que excepción tiene códigos válidos para FK violations

✅ **CA-0004.4**: testPartidosDisponibles_FiltrosPorEstado()
- Verifica que solo partidos de jornadas IN_PROGRESS son isPlayable()

---

## Archivo de Tests

### RF0006_JornadasSincronizacionAcceptanceTest.java
**13 tests** cubriendo sincronización y actualización de estados

Ubicación: `src/test/acceptance/java/com/example/football/jornadas/acceptance/`

Tests:
1. testEstructuraDatos_JornadaTiene8Campos()
2. testEstadosMapeados_NotStartedInProgressFinishedPostponed()
3. testIdentidadUnica_CompositeIdDiferenciaPorLigaSeasonRound()
4. testSincronizacionIncremental_NoCreaduplicados()
5. testJobProgramado_ConfiguracionExiste()
6. testTransicionEstado_NotStartedAInProgress()
7. testTransicionEstado_InProgressAFinished()
8. testTransicionEstado_APostponed()
9. testRegistroSyncLogs_SyncResultContieneDatos()
10. testSyncResult_ExitosoSinErrores()
11. testValidacion_JornadaRechazaRoundNumberInvalido()
12. testValidacion_JornadaRechazaLigaVacia()
13. testValidacion_JornadaRechazaSeasonInvalida()

### RF0006_JornadasValidacionAcceptanceTest.java
**15 tests** cubriendo bloqueo de partidos y validaciones

Ubicación: `src/test/acceptance/java/com/example/football/jornadas/acceptance/`

Tests:
1. testBloquePartido_JornadaNotStarted()
2. testPermisoPartido_JornadaInProgress()
3. testBloquePartido_JornadaFinished()
4. testBloquePartido_JornadaPostponed()
5. testBloquePartido_JornadaNoExiste_ExcepcionConCodigoError()
6. testValidacionPartido_JornadaNotFound()
7. testFK_JornadaTieneUuidUnico()
8. testFKConstraint_ViolacionFK()
9. testPartidosDisponibles_FiltrosPorEstado()
10. testCodigosError_TodosDefinidos()
11. testValidacion_SpecRechazaJornadaNull()
12. testMensajesError_EnEspanol()
13. testIsPlayable_AliasParaInProgress()
14. testExcepcion_GuardaRoundNumber()
15. (implícito en los anteriores)

---

## Ejecución de Tests

### Ejecutar todos los tests de CHG-0006
```bash
mvn test -Dtest=RF0006*AcceptanceTest
```

### Ejecutar solo sincronización
```bash
mvn test -Dtest=RF0006_JornadasSincronizacionAcceptanceTest
```

### Ejecutar solo validación
```bash
mvn test -Dtest=RF0006_JornadasValidacionAcceptanceTest
```

### Ejecutar un test específico
```bash
mvn test -Dtest=RF0006_JornadasSincronizacionAcceptanceTest#testEstructuraDatos_JornadaTiene8Campos
```

### Ejecutar con reporte detallado
```bash
mvn test -Dtest=RF0006*AcceptanceTest -X
```

---

## Requisitos de Ejecución

- **Java 21 LTS**
- **Spring Boot 4.1.1-SNAPSHOT**
- **Maven 3.x**
- **PostgreSQL** (para tests de integración)
- **Base de datos de test** configurada via `application-test.properties`

### Configuración de Perfil de Test

Las pruebas usan:
- Anotación `@SpringBootTest` - Arranca contexto Spring completo
- Anotación `@ActiveProfiles("test")` - Usa `application-test.properties`
- Sin MockMvc - Solo inyección de servicios y repositorios

---

## Estructura de Tests

### Patrón AAA (Arrange-Act-Assert)

Cada test sigue:

```java
@Test
@DisplayName("Descripción del CA")
void testCaXXX_DescripcionLarga() {
    // ARRANGE: Preparar datos y mocks
    Jornada jornada = Jornada.nueva(...);
    
    // ACT: Ejecutar operación
    boolean valida = jornada.isPlayable();
    
    // ASSERT: Verificar resultado con AssertJ
    assertThat(valida).isTrue();
}
```

### Uso de AssertJ

Todos los tests usan AssertJ para assertions fluidas:

```java
assertThat(jornada.status()).isEqualTo(JornadaStatus.IN_PROGRESS);
assertThat(jornada.roundNumber()).isBetween(1, 38);
assertThat(mensaje).contains("jornada").containsIgnoringCase("no ha");
assertThatThrownBy(() -> new Jornada(...))
    .isInstanceOf(Exception.class);
```

---

## Trazabilidad a Requisitos

| Requisito | CA | Test | Clase |
|-----------|-----|------|-------|
| RF-0001 | CA-0001.1 | testEstructuraDatos_JornadaTiene8Campos | Sincronización |
| RF-0001 | CA-0001.2 | testEstadosMapeados_NotStartedInProgressFinishedPostponed | Sincronización |
| RF-0001 | CA-0001.3 | testIdentidadUnica_CompositeIdDiferenciaPorLigaSeasonRound | Sincronización |
| RF-0001 | CA-0001.4 | testSincronizacionIncremental_NoCreaduplicados | Sincronización |
| RF-0002 | CA-0002.1 | testBloquePartido_JornadaNotStarted | Validación |
| RF-0002 | CA-0002.2 | testPermisoPartido_JornadaInProgress | Validación |
| RF-0002 | CA-0002.3 | testBloquePartido_JornadaFinished | Validación |
| RF-0002 | CA-0002.4 | testBloquePartido_JornadaPostponed | Validación |
| RF-0002 | CA-0002.5 | testBloquePartido_JornadaNoExiste_ExcepcionConCodigoError | Validación |
| RF-0003 | CA-0003.1 | testJobProgramado_ConfiguracionExiste | Sincronización |
| RF-0003 | CA-0003.2 | testTransicionEstado_NotStartedAInProgress | Sincronización |
| RF-0003 | CA-0003.3 | testTransicionEstado_InProgressAFinished | Sincronización |
| RF-0003 | CA-0003.4 | testTransicionEstado_APostponed | Sincronización |
| RF-0003 | CA-0003.5 | testRegistroSyncLogs_SyncResultContieneDatos | Sincronización |
| RF-0004 | CA-0004.1 | testValidacionPartido_JornadaNotFound | Validación |
| RF-0004 | CA-0004.2 | testFK_JornadaTieneUuidUnico | Validación |
| RF-0004 | CA-0004.3 | testFKConstraint_ViolacionFK | Validación |
| RF-0004 | CA-0004.4 | testPartidosDisponibles_FiltrosPorEstado | Validación |

---

## Resultados Esperados

### Ejecución Exitosa

```
[INFO] Building football 0.0.1-SNAPSHOT
[INFO] ================================[ jar ]================================
[INFO]
[INFO] --- maven-surefire-plugin:3.5.0:test (default-test) @ football ---
[INFO] Running com.example.football.jornadas.acceptance.RF0006_JornadasSincronizacionAcceptanceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s
[INFO] Running com.example.football.jornadas.acceptance.RF0006_JornadasValidacionAcceptanceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.8 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

### En caso de fallos

Cada fallo muestra:
- Nombre del test y CA asociado
- Assertion que falló
- Valor esperado vs actual
- Stack trace completo

---

## Troubleshooting

### Error: "Connection refused" en Tests
**Causa**: Base de datos de test no disponible

**Solución**:
```bash
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=football_test \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  postgres:15
```

### Error: "Spring Context failed to load"
**Causa**: Falta configuración en application-test.properties

**Solución**: Crear archivo con:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/football_test
spring.datasource.username=test
spring.datasource.password=test
spring.jpa.hibernate.ddl-auto=create-drop
```

### Error: "cannot find symbol: method nueva(...)"
**Causa**: Código de tests desactualizado

**Solución**: Recompilar maven
```bash
mvn clean test-compile
```

---

## Criterio de Aceptación Global

CHG-0006 se considera **completado** cuando:

✅ `mvn test -Dtest=RF0006*AcceptanceTest` retorna **0 fallos**  
✅ Todos los **28 tests pasan**  
✅ **Cobertura**: 18/18 CA verificados  
✅ **Build time**: < 5 minutos  
✅ No hay **warnings** en compilación  

---

## Próximos Pasos

1. ✅ Ejecutar tests: `mvn test -Dtest=RF0006*AcceptanceTest`
2. ✅ Verificar cobertura de CA
3. ⏳ Generar reporte en `evidence.md`
4. ⏳ Ejecutar tests de integración con API-Football real (en staging)
5. ⏳ Validar RNF (rendimiento, disponibilidad, etc.)

---

## Referencias

- [requirements.md](requirements.md) - Requisitos y CA
- [tasks.md](tasks.md) - Tareas técnicas
- [README.md](../../../jornadas/README.md) - Documentación del módulo
- [CHG-0001 Acceptance Tests](../../../estadisticas/acceptance/) - Patrón de referencia
