# Escenarios verificables - Acciones del jugador

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Entrenar fuera de partido

**Dado** que un jugador tiene energía igual o superior al mínimo requerido  
**Cuando** el usuario registra una acción de entrenamiento  
**Entonces** la energía del jugador disminuye según el coste definido  
**Y** su XP aumenta según la recompensa de entrenamiento

> Prueba de aceptación: `project/tests/acceptance/acciones-del-jugador.entrenar-fuera-partido.aceptacion.test`

---

## Escenario: Aplicar XP ganado en partido

**Dado** que ApiFootball ha devuelto las estadísticas de un partido ya finalizado  
**Cuando** el sistema procesa las acciones del jugador en ese partido  
**Entonces** se calcula el XP ganado según las reglas del dominio  
**Y** se acumula en el XP total del jugador

> Prueba de aceptación: `project/tests/acceptance/acciones-del-jugador.aplicar-xp-partido.aceptacion.test`

---

## Escenario: Intentar registrar acción con jugador lesionado (error)

**Dado** que un jugador está marcado como lesionado en ProyectoFootball  
**Cuando** el usuario intenta registrar una nueva acción manual (entrenamiento, sesión especial, etc.)  
**Entonces** el sistema rechaza la operación  
**Y** se muestra el error `jugador.lesionado`

> Prueba de aceptación: `project/tests/acceptance/acciones-del-jugador.error-accion-lesionado.aceptacion.test`
