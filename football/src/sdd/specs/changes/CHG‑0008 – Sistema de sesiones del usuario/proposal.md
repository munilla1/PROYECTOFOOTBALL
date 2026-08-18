# CHG-0008 - Sistema de sesiones del usuario

> Permitir que los usuarios se conecten y desconecten del juego mediante nombre y contraseña.

## Estado actual
`borrador`

## Problema
El videojuego no tiene un sistema de autenticación.  
Cualquier usuario puede acceder sin credenciales y no existe forma de recuperar su partida personal.

**No hay login, logout ni control de identidad.**

## Objetivo
Implementar un sistema de sesiones seguro que permita iniciar y cerrar sesión con credenciales propias.

## Alcance

### Incluye
- Registro de usuario con email + contraseña.
- Inicio de sesión con validación de credenciales.
- Cierre de sesión manual y automático.
- Tokens de sesión (JWT o equivalente).
- Expiración de sesión por inactividad.
- Middleware de autenticación para rutas protegidas.

### Excluye
- Autenticación social (Google, Apple, etc.).
- Recuperación de contraseña (se añadirá en otro CHG).

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | alto | Autenticación + tokens |
| Desarrollo frontend | medio | Formularios + estados de sesión |
| Base de datos | bajo | Campos adicionales |
| Infraestructura | bajo | No requiere cambios |
| Seguridad - sensible | sí | Manejo de contraseñas y tokens |

## Dominio afectado
Consulte sdd/specs/domains/usuario/spec.md  
Consulte sdd/specs/domains/sesiones/spec.md

## Dependencias
- Depende de CHG‑0007 (usuarios persistentes).
