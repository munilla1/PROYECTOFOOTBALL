# Escenarios verificables - Jornadas

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Detectar jornada con partido para el jugador

**Dado** que ApiFootball tiene programados partidos para el equipo del jugador en una jornada concreta  
**Cuando** ProyectoFootball sincroniza el calendario de jornadas  
**Entonces** la jornada queda registrada como `con_partido` para ese jugador  
**Y** se asocian los partidos relevantes a la jornada en ProyectoFootball

> Prueba de aceptación: `project/tests/acceptance/jornadas.detectar-con-partido.aceptacion.test`

---

## Escenario: Marcar jornada como completada tras importar todos los partidos

**Dado** que todos los partidos de la jornada han finalizado en ApiFootball  
**Y** ProyectoFootball ha importado las estadísticas de todos esos partidos  
**Cuando** el sistema evalúa el estado de la jornada  
**Entonces** la jornada pasa al estado `completada`

> Prueba de aceptación: `project/tests/acceptance/jornadas.marcar-completada.aceptacion.test`

---

## Escenario: Intentar completar jornada con partidos sin importar (error)

**Dado** que existen partidos finalizados en ApiFootball que aún no se han importado en ProyectoFootball  
**Cuando** el sistema intenta marcar la jornada como `completada`  
**Entonces** se rechaza la operación  
**Y** se muestra el error `jornada.pendiente-importacion`

> Prueba de aceptación: `project/tests/acceptance/jornadas.error-pendiente-importacion.aceptacion.test`
