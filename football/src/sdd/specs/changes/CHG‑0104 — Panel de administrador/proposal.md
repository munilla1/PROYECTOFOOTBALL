# CHG-0104 – Panel de administrador

> Crear la interfaz de administración para usuarios con rol `admin`.

## Estado actual
`borrador`

## Problema
El backend implementa roles (CHG‑0009), pero **no existe una interfaz** donde los administradores puedan:

- ver logs,
- revisar errores,
- gestionar usuarios,
- acceder a configuraciones internas.

Actualmente, los administradores no tienen herramientas visuales.

## Objetivo
Crear un panel de administración accesible solo para usuarios con rol `admin`.

## Alcance

### Incluye
- Protección de ruta por rol.
- Vista con:
  - lista de usuarios
  - estado de membresías
  - logs básicos
  - errores del sistema
- Botones para acciones administrativas (solo las permitidas por backend).

### Excluye
- Funcionalidades avanzadas de administración.
- Edición profunda de configuraciones internas.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Frontend | alto | Nueva vista + controles admin |
| Backend | medio | Endpoints adicionales |
| Seguridad | crítico | Control de acceso por rol |

## Dominio afectado
Consulte sdd/specs/domains/roles/spec.md  
Consulte sdd/specs/domains/admin/spec.md

## Dependencias
- Depende de CHG‑0009 (sistema de roles).
