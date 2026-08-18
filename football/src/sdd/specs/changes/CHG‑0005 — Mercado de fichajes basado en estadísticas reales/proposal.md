# CHG-0005 - Mercado de fichajes basado en estadísticas reales

> Crear mercado de fichajes basado en estadísticas reales de API-Football.

## Estado actual
`borrador`

## Problema
El mercado de fichajes no usa estadísticas reales para determinar precios y
disponibilidad.  
Los jugadores no reflejan su valor real según rendimiento, minutos o lesiones.

**Aún no hay soluciones.**

## Objetivo
Crear mercado dinámico basado en estadísticas reales.

## Alcance

### Incluye
- Precio = rating × 100 + minutos × 0.2
- Jugadores lesionados → no disponibles
- Actualización automática cada jornada real

### Excluye
- Precios aleatorios
- Jugadores inventados

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | medio | Cálculo + sincronización |
| Desarrollo frontend | medio | Mostrar mercado dinámico |
| Base de datos | media | Guardar mercado |
| Infraestructura | baja | Depende de CHG‑0001 |
| Contratos API | no | |
| Seguridad - sensible | no | |

## Dominio afectado
Consulte sdd/specs/domains/jugador/spec.md  
Consulte sdd/specs/domains/progresion/spec.md  
Consulte sdd/specs/domains/acciones-jugador/spec.md

## Dependencias
- Depende de CHG‑0001 (estadísticas reales).
