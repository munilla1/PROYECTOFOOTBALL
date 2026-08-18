# CHG-0103 – Panel de usuario

> Crear el panel principal del usuario autenticado, mostrando su información y accesos básicos.

## Estado actual
`borrador`

## Problema
Tras iniciar sesión, el usuario no tiene un espacio visual donde:

- ver su información,
- consultar su membresía,
- acceder a funcionalidades del juego,
- navegar por las vistas principales.

El sistema carece de un “home” para usuarios autenticados.

## Objetivo
Crear un panel de usuario que muestre información básica y permita navegar por las funcionalidades del juego.

## Alcance

### Incluye
- Mostrar:
  - nombre
  - email
  - membresía
  - estado del jugador (si aplica)
- Botones de navegación a:
  - estadísticas reales
  - jornadas
  - fichajes
  - progresión
  - Stripe Checkout (si no tiene membresía)
- Protección de ruta (solo usuarios autenticados).

### Excluye
- Panel de administrador (CHG‑0104).
- Implementación de vistas secundarias (se harán en CHG específicos).

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Frontend | alto | Nueva vista + navegación |
| Backend | bajo | Solo consumo de endpoints |
| Seguridad | alto | Ruta protegida |

## Dominio afectado
Consulte sdd/specs/domains/usuario/spec.md  
Consulte sdd/specs/domains/membresias/spec.md

## Dependencias
- Depende de CHG‑0102 (pantalla de login).
