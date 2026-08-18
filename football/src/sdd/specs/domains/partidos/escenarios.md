# Escenarios verificables - Partidos

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Registrar partido desde ApiFootball

**Dado** que ApiFootball tiene un partido programado para el equipo del jugador  
**Cuando** ProyectoFootball sincroniza los partidos del calendario  
**Entonces** el partido queda registrado en ProyectoFootball con su identificador de ApiFootball  
**Y** se almacena su fecha, hora y equipos participantes

> Prueba de aceptación: `project/tests/acceptance/partidos.registrar-desde-api.aceptacion.test`

---

## Escenario: Marcar partido como finalizado tras ApiFootball

**Dado** que ApiFootball marca un partido como finalizado  
**Cuando** ProyectoFootball consulta el estado del partido  
**Entonces** el partido pasa al estado `finalizado` en ProyectoFootball  
**Y** queda listo para importar estadísticas de jugadores

> Prueba de aceptación: `project/tests/acceptance/partidos.marcar-finalizado-desde-api.aceptacion.test`

---

## Escenario: Error al registrar partido sin identificador de ApiFootball

**Dado** que se intenta registrar un partido en ProyectoFootball sin un identificador válido de ApiFootball  
**Cuando** el sistema valida los datos del partido  
**Entonces** se rechaza la operación  
**Y** se muestra el error `partido.sin-identificador-api`

> Prueba de aceptación: `project/tests/acceptance/partidos.error-sin-identificador-api.aceptacion.test`
