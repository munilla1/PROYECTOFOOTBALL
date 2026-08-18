# Dominio: Acciones del jugador

## Descripción general
Este dominio gestiona todas las acciones que un jugador puede realizar fuera de los
partidos: entrenar, descansar, recuperación, etc.  
Las acciones afectan energía, forma, moral y XP, y deben ser coherentes con las
estadísticas reales del jugador.

## Archivos del dominio
- entidades.md → define las acciones y sus atributos
- reglas.md → reglas de negocio de cada acción
- escenarios.md → comportamientos verificables
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Ejecutar acciones que modifican atributos del jugador.
- Validar si el jugador puede realizar una acción (energía mínima, estado, etc.).
- Integrarse con estadísticas reales para evitar inconsistencias.
- Ajustar energía y forma según CHG‑0003.

## Dependencias externas
- Dominio Jugador
- API-Football (minutos jugados)
- Dominio Progresión

## Cambios que afectan este dominio
- CHG-0003 — Energía y forma basadas en minutos reales
- CHG-0005 — Mercado de fichajes basado en estadísticas reales
