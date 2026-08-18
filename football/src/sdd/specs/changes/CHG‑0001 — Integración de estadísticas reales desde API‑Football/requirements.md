# Requisitos - CHG-0001

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.
Los criterios de aceptación son la fuente de información fidedigna para las pruebas de aceptación.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Sincronizar estadísticas reales de jugadores
> El sistema debe obtener estadísticas reales de jugadores desde API-Football y mapearlas a las entidades internas.

### Criterios de aceptación
- [ ] **Dado** que existe conexión con API-Football  
      **Cuando** se solicita estadísticas de un jugador  
      **Entonces** se obtienen goles, asistencias, minutos, tarjetas y rating reales  
- [ ] **Dado** que API-Football devuelve error  
      **Cuando** se intenta obtener estadísticas  
      **Entonces** el sistema registra el error y reintenta según política definida

---

## REQ-002: Sincronizar partidos reales
> El sistema debe obtener fixtures reales y convertirlos en partidos internos.

### Criterios de aceptación
- [ ] **Dado** que existe un fixture real  
      **Cuando** se sincroniza el calendario  
      **Entonces** se crea un partido interno con fecha, equipos y estado  
- [ ] **Dado** que el fixture está incompleto  
      **Cuando** se intenta sincronizar  
      **Entonces** el sistema marca el partido como inválido

---

## Restricciones
1. API-Football tiene límites de rate-limit.
2. La sincronización debe ser tolerante a fallos externos.

## Supuestos
1. API-Football está disponible y responde correctamente la mayoría del tiempo.

## Preguntas abiertas
- [ ] ¿Qué liga(s) se sincronizarán inicialmente?
- [ ] ¿Se requiere paginación para jugadores?
