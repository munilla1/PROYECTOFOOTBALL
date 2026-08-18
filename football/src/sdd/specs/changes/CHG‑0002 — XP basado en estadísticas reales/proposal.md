# CHG-0002 - XP basado en estadísticas reales

> Calcular XP del jugador usando estadísticas reales obtenidas desde API-Football.

## Estado actual
`borrador`

## Problema
El jugador no gana XP basado en su rendimiento real.  
Actualmente el XP no se calcula a partir de goles, asistencias, minutos o rating real,
lo que provoca progresión artificial y desconectada del fútbol real.

**Aún no hay soluciones.**

## Objetivo
Calcular XP usando estadísticas reales de API-Football y actualizar la progresión del
jugador automáticamente tras cada partido real.

## Alcance

### Incluye
- Fórmula de XP basada en estadísticas reales.
- Actualización automática tras cada jornada real.
- Persistencia del XP acumulado.
- Integración con CHG‑0001.

### Excluye
- XP basado en acciones simuladas.
- Modificación de estadísticas reales.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Cálculo + persistencia |
| Desarrollo frontend | bajo | Mostrar XP |
| Base de datos | media | Guardar XP acumulado |
| Infraestructura | baja | Depende de CHG‑0001 |
| Contratos API | no | Usa los ya creados |
| Seguridad - sensible | no | |

## Dominio afectado
Consulte sdd/specs/domains/jugador/spec.md  
Consulte sdd/specs/domains/progresion/spec.md  
Consulte sdd/specs/domains/estadisticas/spec.md

## Dependencias
- Depende de CHG‑0001 (estadísticas reales).
