# CHG-0003 - Energía y forma basadas en minutos reales

> Ajustar energía y forma del jugador según los minutos jugados en partidos reales.

## Estado actual
`borrador`

## Problema
La energía y forma del jugador no reflejan su carga real de partidos.  
El sistema actual no considera minutos jugados, partidos consecutivos ni descanso real,
lo que genera valores irreales y gameplay inconsistente.

**Aún no hay soluciones.**

## Objetivo
Ajustar energía y forma automáticamente según los minutos jugados en partidos reales.

## Alcance

### Incluye
- Energía −40 si juega 90 min.
- Energía −20 si juega 45 min.
- Energía +10 si no juega.
- Forma −15 si juega 3 partidos seguidos.
- Actualización automática tras cada jornada real.

### Excluye
- Energía basada en acciones simuladas.
- Modificación de estadísticas reales.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Reglas + cron job |
| Desarrollo frontend | bajo | Mostrar energía/forma |
| Base de datos | media | Guardar energía/forma |
| Infraestructura | baja | Depende de CHG‑0001 |
| Contratos API | no | |
| Seguridad - sensible | no | |

## Dominio afectado
Consulte sdd/specs/domains/jugador/spec.md  
Consulte sdd/specs/domains/acciones-jugador/spec.md  
Consulte sdd/specs/domains/partidos/spec.md

## Dependencias
- Depende de CHG‑0001 (minutos reales).
