# Escenarios verificables - Estadísticas

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Importar estadísticas de partido desde ApiFootball

**Dado** que existe un partido finalizado en ApiFootball para el equipo del jugador  
**Cuando** ProyectoFootball consulta la API para ese partido  
**Entonces** se importan las estadísticas del jugador (minutos, goles, asistencias, etc.)  
**Y** se almacenan asociadas al jugador y al partido correspondiente

> Prueba de aceptación: `project/tests/acceptance/estadisticas.importar-desde-api.aceptacion.test`

---

## Escenario: Actualizar rating del jugador tras nuevo partido

**Dado** que el jugador tiene estadísticas históricas almacenadas  
**Y** se han importado nuevas estadísticas de un partido reciente  
**Cuando** el sistema recalcula el rating del jugador  
**Entonces** el rating se actualiza según las reglas de negocio definidas

> Prueba de aceptación: `project/tests/acceptance/estadisticas.actualizar-rating.aceptacion.test`

---

## Escenario: Error al importar estadísticas inconsistentes

**Dado** que ApiFootball devuelve datos incompletos o inconsistentes para un jugador  
**Cuando** ProyectoFootball intenta importar esas estadísticas  
**Entonces** el sistema rechaza la importación  
**Y** se registra el error `estadisticas.inconsistentes` para auditoría

> Prueba de aceptación: `project/tests/acceptance/estadisticas.error-datos-inconsistentes.aceptacion.test`
