# Dominio: Jornadas

## Descripción general
El dominio Jornadas gestiona el calendario del videojuego, sincronizado con las
jornadas reales de la liga obtenidas desde API-Football.  
Determina cuándo se puede jugar un partido y cuándo solo se pueden realizar acciones.

## Archivos del dominio
- entidades.md → define jornada y sus atributos
- reglas.md → reglas del calendario
- escenarios.md → comportamientos verificables
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Sincronizar jornadas reales desde API-Football.
- Bloquear partidos cuando no exista jornada real.
- Integrarse con Partidos para determinar disponibilidad.
- Integrarse con Jugador para actualizar energía y forma.

## Dependencias externas
- API-Football (fixtures reales)
- Dominio Partidos

## Cambios que afectan este dominio
- CHG-0006 — Jornadas sincronizadas con partidos reales
