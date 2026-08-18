# Escenarios verificables - Usuario

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Registrar usuario correctamente

**Dado** que un usuario proporciona un email y contraseña válidos  
**Cuando** ProyectoFootball procesa el registro  
**Entonces** se crea un nuevo usuario en la base de datos  
**Y** se inicializa su progreso de jugador y su membresía en estado `trial`

> Prueba de aceptación: `project/tests/acceptance/usuario.registrar.aceptacion.test`

---

## Escenario: Cargar progreso del usuario al iniciar sesión

**Dado** que un usuario ya existe en la base de datos  
**Y** tiene progreso guardado previamente  
**Cuando** el usuario inicia sesión correctamente  
**Entonces** ProyectoFootball carga su progreso (nivel, XP, energía, estado, historial)  
**Y** el usuario continúa la partida donde la dejó

> Prueba de aceptación: `project/tests/acceptance/usuario.cargar-progreso.aceptacion.test`

---

## Escenario: Error al registrar usuario con email duplicado

**Dado** que ya existe un usuario registrado con el mismo email  
**Cuando** se intenta registrar un nuevo usuario con ese email  
**Entonces** el sistema rechaza la operación  
**Y** se muestra el error `usuario.email-duplicado`

> Prueba de aceptación: `project/tests/acceptance/usuario.error-email-duplicado.aceptacion.test`
