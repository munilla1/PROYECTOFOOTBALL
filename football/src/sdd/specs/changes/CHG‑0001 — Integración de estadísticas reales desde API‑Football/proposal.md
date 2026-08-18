# CHG-0001 - Integración de estadísticas reales desde API-Football

> Integrar API-Football para obtener estadísticas reales de jugadores y partidos,
> sincronizarlas con los dominios internos y habilitar la base del gameplay realista.

## Estado actual
`borrador`

> Estados posibles: borrador + listo + en progreso + validado + archivado

## Problema
El videojuego depende de estadísticas reales para calcular XP, energía, forma, moral,
progresión y rendimiento. Actualmente no existe un sistema que obtenga, procese y
sincronice datos reales desde API-Football.  
Esto afecta directamente a los dominios: jugador, partidos, estadísticas y progresión,
dejando el juego sin datos fiables y sin conexión con el fútbol real.

**Aún no hay soluciones.**

## Objetivo
El sistema obtiene estadísticas reales desde API-Football y las sincroniza con los
dominios internos del videojuego de forma automática y confiable.

## Alcance

### Incluye
- Consumo de endpoints de API-Football (players, fixtures).
- Mapeo de estadísticas reales a entidades internas.
- Sincronización automática tras cada jornada real.
- Persistencia de estadísticas reales en Firestore.
- Manejo de errores y reintentos.

### Excluye
- Generación de estadísticas simuladas.
- Modificación de datos reales.
- Cálculo de XP, energía o forma (se realizan en otros cambios).

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | alto | Integración API externa + cron jobs |
| Desarrollo frontend | medio | Mostrar estadísticas reales |
| Base de datos | alta | Nuevas colecciones y documentos |
| Infraestructura | media | Llamadas externas + rate limits |
| Contratos API | sí | Crear interfaces en project/interfaces/ |
| Seguridad - sensible | sí | Revisar sdd/security/checklists/security-review.md |

## Dominio afectado
Consulte sdd/specs/domains/jugador/spec.md  
Consulte sdd/specs/domains/partidos/spec.md  
Consulte sdd/specs/domains/estadisticas/spec.md  
Consulte sdd/specs/domains/progresion/spec.md

## Dependencias
- Ninguna. Este cambio es la base para CHG‑0002, CHG‑0003, CHG‑0004, CHG‑0005 y CHG‑0006.
