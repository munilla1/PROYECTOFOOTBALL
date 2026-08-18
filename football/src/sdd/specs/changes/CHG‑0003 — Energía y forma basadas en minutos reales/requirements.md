# Requisitos - CHG-0003

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Ajustar energía según minutos reales
> La energía del jugador debe ajustarse según los minutos jugados.

### Criterios de aceptación
- [ ] **Dado** que un jugador juega 90 minutos  
      **Cuando** se actualiza energía  
      **Entonces** energía = energía - 40  
- [ ] **Dado** que un jugador no juega  
      **Cuando** se actualiza energía  
      **Entonces** energía = energía + 10

---

## REQ-002: Ajustar forma según carga real
> La forma debe disminuir si el jugador juega demasiados partidos consecutivos.

### Criterios de aceptación
- [ ] **Dado** que un jugador juega 3 partidos seguidos  
      **Cuando** se actualiza forma  
      **Entonces** forma = forma - 15  
- [ ] **Dado** que el jugador no tiene suficientes partidos consecutivos  
      **Cuando** se actualiza forma  
      **Entonces** la forma no cambia

---

## Restricciones
1. Energía nunca puede ser menor que 0.
2. Forma nunca puede ser menor que 0.

## Supuestos
1. API-Football provee minutos jugados por partido.

## Preguntas abiertas
- [ ] ¿Se debe considerar la posición para el desgaste?
