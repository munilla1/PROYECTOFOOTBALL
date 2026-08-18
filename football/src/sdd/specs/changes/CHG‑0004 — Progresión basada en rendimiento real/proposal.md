# CHG-0004 - Progresión basada en rendimiento real

> Ajustar forma, moral y progresión del jugador según su rendimiento real.

## Estado actual
`borrador`

## Problema
La progresión del jugador no depende de su rendimiento real.  
El sistema actual no usa rating real, goles consecutivos o rachas negativas para
modificar forma o moral, lo que genera progresión artificial.

**Aún no hay soluciones.**

## Objetivo
Usar rating real y estadísticas reales para determinar progresión, forma y moral.

## Alcance

### Incluye
- Rating > 7.5 → forma +10
- Rating < 6.0 → forma −10
- Goles consecutivos → moral +15
- 5 partidos sin marcar → moral −10
- Actualización automática tras cada partido real

### Excluye
- Progresión basada en acciones simuladas.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Reglas + cron job |
| Desarrollo frontend | bajo | Mostrar moral/forma |
| Base de datos | media | Guardar moral/forma |
| Infraestructura | baja | Depende de CHG‑0001 |
| Contratos API | no | |
| Seguridad - sensible | no | |

## Dominio afectado
Consulte sdd/specs/domains/progresion/spec.md  
Consulte sdd/specs/domains/jugador/spec.md  
Consulte sdd/specs/domains/estadisticas/spec.md

## Dependencias
- Depende de CHG‑0001 (rating real).
