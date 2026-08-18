# Dominio: Jugador

## Descripción general
El dominio Jugador representa toda la información, atributos, estados y comportamientos
relacionados con los jugadores del videojuego.  
Incluye energía, forma, moral, nivel, XP y rendimiento, todos ellos influenciados por
estadísticas reales obtenidas desde API-Football.

Este dominio es central en el gameplay, ya que determina la capacidad del jugador para
participar en partidos, progresar y ejecutar acciones.

## Archivos del dominio
- entidades.md → define las entidades del jugador y sus atributos
- reglas.md → define las reglas de negocio del jugador
- escenarios.md → comportamientos verificables del jugador
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Gestionar atributos del jugador (energía, forma, moral, nivel, XP).
- Sincronizar estadísticas reales provenientes de API-Football.
- Determinar si el jugador puede participar en partidos.
- Calcular rendimiento y progresión.
- Integrarse con los dominios Partidos, Estadísticas y Progresión.

## Dependencias externas
- API-Football (stats reales)
- Dominio Partidos
- Dominio Estadísticas
- Dominio Progresión

## Cambios que afectan este dominio
- CHG-0001 — Integración de estadísticas reales
- CHG-0002 — XP basado en estadísticas reales
- CHG-0003 — Energía y forma basadas en minutos reales
- CHG-0004 — Progresión basada en rendimiento real
- CHG-0005 — Mercado de fichajes basado en estadísticas reales
