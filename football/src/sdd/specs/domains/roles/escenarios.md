# Escenarios verificables - Roles

Cada escenario describe un comportamiento observable del sistema en lenguaje empresarial.
Cada escenario se corresponde 1:1 con una prueba de aceptación en `project/tests/acceptance/`.

---

## Escenario: Asignar rol correctamente al usuario

**Dado** que un usuario recién registrado tiene rol por defecto `usuario`  
**Cuando** un administrador actualiza su rol a `admin`  
**Entonces** el rol del usuario queda registrado como `admin`  
**Y** el usuario obtiene acceso a funciones administrativas

> Prueba de aceptación: `project/tests/acceptance/roles.asignar-rol.aceptacion.test`

---

## Escenario: Acceder a función protegida como admin

**Dado** que un usuario tiene rol `admin`  
**Cuando** intenta acceder a una función exclusiva de administración  
**Entonces** el sistema permite el acceso  
**Y** registra la acción en el log de auditoría

> Prueba de aceptación: `project/tests/acceptance/roles.acceso-admin.aceptacion.test`

---

## Escenario: Error al acceder a función admin con rol usuario

**Dado** que un usuario tiene rol `usuario`  
**Cuando** intenta acceder a una función exclusiva de administradores  
**Entonces** el sistema rechaza la operación  
**Y** se muestra el error `rol.no-autorizado`

> Prueba de aceptación: `project/tests/acceptance/roles.error-acceso-no-autorizado.aceptacion.test`
