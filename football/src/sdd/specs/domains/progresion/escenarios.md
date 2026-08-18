# Escenarios verificables - Progresión

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Aplicar progresión tras acumular XP de partidos

**Dado** que el jugador ha acumulado XP procedente de varios partidos importados desde ApiFootball  
**Cuando** el sistema evalúa la progresión del jugador  
**Entonces** el jugador sube de nivel si supera el umbral de XP  
**Y** recibe las bonificaciones correspondientes (atributos, habilidades, etc.)

> Prueba de aceptación: `project/tests/acceptance/progresion.aplicar-desde-partidos.aceptacion.test`

---

## Escenario: Recalcular umbral de XP por nivel

**Dado** que el jugador ha alcanzado un nuevo nivel  
**Cuando** el sistema recalcula el XP requerido para el siguiente nivel  
**Entonces** el nuevo umbral queda registrado correctamente  
**Y** se utiliza en futuras evaluaciones de progresión

> Prueba de aceptación: `project/tests/acceptance/progresion.recalcular-umbral-xp.aceptacion.test`

---

## Escenario: Bloquear subida de nivel sin XP suficiente (error)

**Dado** que el jugador no ha alcanzado el XP mínimo requerido para el siguiente nivel  
**Cuando** el sistema evalúa la progresión  
**Entonces** se rechaza la subida de nivel  
**Y** se muestra el error `xp.insuficiente`

> Prueba de aceptación: `project/tests/acceptance/progresion.error-xp-insuficiente.aceptacion.test`
