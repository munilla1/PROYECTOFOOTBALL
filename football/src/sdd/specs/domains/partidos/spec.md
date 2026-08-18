# Dominio: Partidos

## Descripción general
El dominio Partidos representa los encuentros reales obtenidos desde API-Football.
Incluye equipos, fecha, estado, resultado y estadísticas de los jugadores en cada
partido.

Este dominio es fundamental para calcular XP, energía, forma, moral y progresión.

## Archivos del dominio
- entidades.md → define partido, resultado y sus atributos
- reglas.md → reglas de negocio del partido
- escenarios.md → comportamientos verificables
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Sincronizar partidos reales desde API-Football.
- Validar si un jugador puede participar (energía mínima).
- Proveer estadísticas reales al dominio Estadísticas.
- Integrarse con Jornadas para determinar disponibilidad.

## Dependencias externas
- API-Football (fixtures reales)
- Dominio Estadísticas
- Dominio Jugador
- Dominio Jornadas

## Cambios que afectan este dominio
- CHG-0001 — Integración de estadísticas reales
- CHG-0003 — Energía y forma basadas en minutos reales
- CHG-0006 — Jornadas sincronizadas con partidos reales
