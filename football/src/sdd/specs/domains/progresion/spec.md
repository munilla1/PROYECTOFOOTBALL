# Dominio: Progresión

## Descripción general
El dominio Progresión gestiona el nivel del jugador, XP acumulado, bonificaciones y
evolución del rendimiento.  
Toda la progresión depende de estadísticas reales obtenidas desde API-Football.

## Archivos del dominio
- entidades.md → define nivel, XP y bonificaciones
- reglas.md → reglas de progresión
- escenarios.md → comportamientos verificables
- errores.md → errores esperados del dominio

## Responsabilidades del dominio
- Calcular progresión según estadísticas reales.
- Ajustar forma y moral según rendimiento real.
- Integrarse con Jugador para actualizar atributos.
- Integrarse con Estadísticas para obtener datos reales.

## Dependencias externas
- API-Football (stats reales)
- Dominio Jugador
- Dominio Estadísticas

## Cambios que afectan este dominio
- CHG-0002 — XP basado en estadísticas reales
- CHG-0004 — Progresión basada en rendimiento real
- CHG-0005 — Mercado de fichajes basado en estadísticas reales
