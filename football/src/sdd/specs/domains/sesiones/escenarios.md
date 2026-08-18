# Escenarios verificables - Sesiones

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Iniciar sesión correctamente

**Dado** que un usuario registrado proporciona credenciales válidas  
**Cuando** ProyectoFootball valida el email y la contraseña  
**Entonces** se genera un token de sesión válido  
**Y** la sesión queda marcada como `activa`

> Prueba de aceptación: `project/tests/acceptance/sesiones.iniciar-sesion.aceptacion.test`

---

## Escenario: Cerrar sesión correctamente

**Dado** que un usuario tiene una sesión activa  
**Cuando** solicita cerrar sesión  
**Entonces** el token queda invalidado  
**Y** la sesión pasa al estado `inactiva`

> Prueba de aceptación: `project/tests/acceptance/sesiones.cerrar-sesion.aceptacion.test`

---

## Escenario: Error al acceder con token expirado

**Dado** que un usuario tiene una sesión cuyo token ha expirado  
**Cuando** intenta acceder a una ruta protegida  
**Entonces** el sistema rechaza la operación  
**Y** se muestra el error `sesion.expirada`

> Prueba de aceptación: `project/tests/acceptance/sesiones.error-token-expirado.aceptacion.test`
