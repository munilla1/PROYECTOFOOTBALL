# CHG-0007 - Sistema de usuarios persistentes

> Guardar y recuperar datos de cada usuario para mantener su progreso en ProyectoFootball.

## Estado actual
`borrador`

## Problema
Actualmente el videojuego no guarda la información del usuario en una base de datos.  
Si el usuario se desconecta o cambia de dispositivo, pierde su progreso, estadísticas, nivel, XP y estado del jugador.

**No existe persistencia de datos.**

## Objetivo
Crear un sistema de almacenamiento persistente que permita guardar y recuperar la partida del usuario en cualquier momento.

## Alcance

### Incluye
- Crear tabla/colección `usuarios`.
- Guardar datos esenciales:  
  - nombre, email, contraseña (hash)  
  - nivel, XP, energía, estado del jugador  
  - membresía (trial, normal, premium)  
  - fecha de creación  
  - progreso del juego  
- Cargar automáticamente la partida al iniciar sesión.
- Guardar automáticamente al cerrar sesión o realizar acciones importantes.

### Excluye
- Sincronización con terceros (Stripe, ApiFootball).
- Exportación manual de partidas.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | alto | CRUD + persistencia + validaciones |
| Desarrollo frontend | medio | Formularios + carga de datos |
| Base de datos | alto | Nuevas tablas y relaciones |
| Infraestructura | medio | Posible necesidad de backups |
| Seguridad - sensible | sí | Manejo de contraseñas y datos personales |

## Dominio afectado
Consulte sdd/specs/domains/usuario/spec.md  
Consulte sdd/specs/domains/jugador/spec.md

## Dependencias
- Depende de CHG‑0008 (sesiones de usuario).
