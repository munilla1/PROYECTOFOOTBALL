# CHG-0009 - Sistema de roles

> Añadir roles "usuario" y "admin" para controlar permisos y funciones internas del juego.

## Estado actual
`borrador`

## Problema
Actualmente todos los usuarios tienen los mismos permisos.  
No existe forma de que un administrador gestione configuraciones internas, revise errores o supervise datos del sistema.

**No hay control de acceso basado en roles.**

## Objetivo
Implementar un sistema de roles que permita distinguir entre usuarios normales y administradores.

## Alcance

### Incluye
- Añadir campo `rol` en la tabla de usuarios.
- Roles disponibles:  
  - `usuario`  
  - `admin`
- Middleware de autorización por rol.
- Panel de administración con funciones especiales:
  - ver logs  
  - revisar errores  
  - modificar configuraciones internas  
  - gestionar usuarios

### Excluye
- Roles avanzados (moderador, soporte, etc.).
- Panel visual completo (solo endpoints básicos en esta fase).

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Autorización por rol |
| Desarrollo frontend | medio | Panel admin |
| Base de datos | bajo | Campo adicional |
| Infraestructura | bajo | No requiere cambios |
| Seguridad - sensible | sí | Control de acceso crítico |

## Dominio afectado
Consulte sdd/specs/domains/usuario/spec.md  
Consulte sdd/specs/domains/admin/spec.md

## Dependencias
- Depende de CHG‑0008 (sesiones del usuario).
