# Dominio: Estadísticas

## Descripción general
El dominio Estadísticas gestiona los datos reales de rendimiento de los jugadores:
goles, asistencias, minutos, tarjetas, rating, etc.  
Toda la información proviene de API-Football y se utiliza para calcular XP, forma,
moral y progresión.

## Archivos del dominio
- entidades.md → define las estadísticas y sus atributos
- reglas.md → reglas de coherencia y cálculo
- escenarios.md → comportamientos verificables
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Sincronizar estadísticas reales desde API-Football.
- Validar coherencia según posición del jugador.
- Proveer datos al dominio Progresión.
- Proveer datos al dominio Jugador.

## Dependencias externas
- API-Football (stats reales)
- Dominio Partidos
- Dominio Jugador

## Cambios que afectan este dominio
- CHG-0001 — Integración de estadísticas reales
- CHG-0002 — XP basado en estadísticas reales
- CHG-0004 — Progresión basada en rendimiento real
