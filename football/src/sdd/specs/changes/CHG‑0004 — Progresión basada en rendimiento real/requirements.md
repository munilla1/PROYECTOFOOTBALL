# Requisitos - CHG-0004

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Ajustar forma según rating real
> La forma debe aumentar o disminuir según el rating real del jugador.

### Criterios de aceptación
- [ ] **Dado** que rating > 7.5  
      **Cuando** se actualiza forma  
      **Entonces** forma = forma + 10  
- [ ] **Dado** que rating < 6.0  
      **Cuando** se actualiza forma  
      **Entonces** forma = forma - 10

---

## REQ-002: Ajustar moral según rendimiento real
> La moral debe reflejar rachas positivas o negativas.

### Criterios de aceptación
- [ ] **Dado** que el jugador marca goles consecutivos  
      **Cuando** se actualiza moral  
      **Entonces** moral = moral + 15  
- [ ] **Dado** que el jugador no marca en 5 partidos  
      **Cuando** se actualiza moral  
      **Entonces** moral = moral - 10

---

## Restricciones
1. Moral nunca puede ser menor que 0 ni mayor que 100.

## Supuestos
1. API-Football provee rating y goles por partido.

## Preguntas abiertas
- [ ] ¿La moral debe influir en XP?
