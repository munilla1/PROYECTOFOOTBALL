# Requisitos - CHG-0006

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Sincronizar jornadas reales
> Las jornadas internas deben coincidir con las jornadas reales de API-Football.

### Criterios de aceptación
- [ ] **Dado** que existe una jornada real  
      **Cuando** se sincroniza el calendario  
      **Entonces** se crea una jornada interna con sus partidos  
- [ ] **Dado** que API-Football no devuelve fixtures  
      **Cuando** se sincroniza  
      **Entonces** se conserva la última jornada válida

---

## REQ-002: Bloquear partidos cuando no haya jornada real
> El jugador solo puede jugar partidos cuando exista jornada real.

### Criterios de aceptación
- [ ] **Dado** que hoy no hay jornada real  
      **Cuando** el jugador intenta jugar un partido  
      **Entonces** el sistema bloquea la acción  
- [ ] **Dado** que hoy sí hay jornada real  
      **Cuando** el jugador intenta jugar  
      **Entonces** el partido se habilita

---

## Restricciones
1. No se pueden crear jornadas simuladas.
2. La sincronización debe ejecutarse diariamente.

## Supuestos
1. API-Football provee fixtures por fecha.

## Preguntas abiertas
- [ ] ¿Se deben soportar múltiples ligas simultáneamente?
