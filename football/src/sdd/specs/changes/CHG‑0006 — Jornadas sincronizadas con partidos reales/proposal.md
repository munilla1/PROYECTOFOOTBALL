# CHG-0006 - Jornadas sincronizadas con partidos reales

> Sincronizar jornadas del videojuego con jornadas reales de API-Football.

## Estado actual
`borrador`

## Problema
Las jornadas del videojuego no coinciden con las jornadas reales.  
El jugador puede jugar partidos cuando no existen partidos reales, lo que rompe la
coherencia del gameplay.

**Aún no hay soluciones.**

## Objetivo
Sincronizar jornadas internas con jornadas reales y bloquear partidos cuando no haya
jornada real.

## Alcance

### Incluye
- Crear jornadas internas basadas en fixtures reales.
- Bloquear partidos si no hay jornada real.
- Actualización automática diaria.

### Excluye
- Jornadas simuladas.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Sincronización + reglas |
| Desarrollo frontend | bajo | Mostrar calendario real |
| Base de datos | media | Guardar jornadas |
| Infraestructura | baja | Depende de CHG‑0001 |
| Contratos API | no | |
| Seguridad - sensible | no | |

## Dominio afectado
Consulte sdd/specs/domains/jornadas/spec.md  
Consulte sdd/specs/domains/partidos/spec.md

## Dependencias
- Depende de CHG‑0001 (fixtures reales).
