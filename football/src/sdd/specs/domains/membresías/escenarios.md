# Escenarios verificables - Membresías

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Activar trial al crear usuario

**Dado** que un usuario se registra por primera vez  
**Cuando** ProyectoFootball crea su perfil  
**Entonces** se asigna automáticamente una membresía `trial`  
**Y** se establece la fecha de inicio del trial

> Prueba de aceptación: `project/tests/acceptance/membresias.activar-trial.aceptacion.test`

---

## Escenario: Convertir trial en plan normal tras 7 días

**Dado** que un usuario tiene una membresía `trial`  
**Y** han pasado 7 días desde su creación  
**Cuando** el sistema evalúa el estado de la membresía  
**Entonces** la membresía pasa automáticamente a `normal`  
**Y** se actualiza la fecha de expiración del nuevo plan

> Prueba de aceptación: `project/tests/acceptance/membresias.convertir-trial-normal.aceptacion.test`

---

## Escenario: Error al acceder a funciones premium sin membresía activa

**Dado** que un usuario tiene una membresía expirada  
**Cuando** intenta acceder a una función exclusiva del plan premium  
**Entonces** el sistema rechaza la operación  
**Y** se muestra el error `membresia.expirada`

> Prueba de aceptación: `project/tests/acceptance/membresias.error-acceso-expirado.aceptacion.test`
