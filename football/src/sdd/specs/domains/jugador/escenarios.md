# Escenarios verificables - Jugador

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Actualizar estado del jugador tras partido

**Dado** que ApiFootball ha registrado un partido finalizado para el equipo del jugador  
**Cuando** ProyectoFootball importa las estadísticas de ese partido  
**Entonces** se actualiza la energía del jugador según la carga de minutos jugados  
**Y** se actualiza su XP acumulado según el rendimiento

> Prueba de aceptación: `project/tests/acceptance/jugador.actualizar-tras-partido.aceptacion.test`

---

## Escenario: Recuperar energía entre jornadas

**Dado** que el jugador tiene energía inferior al máximo permitido  
**Cuando** el sistema aplica la regla de recuperación entre jornadas (descanso natural)  
**Entonces** la energía del jugador aumenta según la regla definida  
**Y** nunca supera el máximo permitido

> Prueba de aceptación: `project/tests/acceptance/jugador.recuperar-energia-entre-jornadas.aceptacion.test`

---

## Escenario: Bloquear participación si el jugador está lesionado

**Dado** que el jugador está marcado como lesionado en ProyectoFootball  
**Cuando** se importan datos de un partido donde aparece alineado  
**Entonces** el sistema marca una inconsistencia  
**Y** se registra el error `jugador.lesionado-en-partido` para revisión

> Prueba de aceptación: `project/tests/acceptance/jugador.error-lesionado-en-partido.aceptacion.test`
