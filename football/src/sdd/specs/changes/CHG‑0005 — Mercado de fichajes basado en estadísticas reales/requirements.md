# Requisitos - CHG-0005

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Calcular precio según estadísticas reales
> El precio del jugador debe basarse en rating y minutos reales.

### Criterios de aceptación
- [ ] **Dado** que un jugador tiene rating y minutos  
      **Cuando** se calcula su precio  
      **Entonces** precio = rating × 100 + minutos × 0.2  
- [ ] **Dado** que el jugador no tiene estadísticas  
      **Cuando** se calcula su precio  
      **Entonces** el sistema marca el jugador como no disponible

---

## REQ-002: Disponibilidad según estado real
> Jugadores lesionados no pueden ser fichados.

### Criterios de aceptación
- [ ] **Dado** que un jugador está lesionado  
      **Cuando** se consulta disponibilidad  
      **Entonces** el jugador aparece como “no disponible”  
- [ ] **Dado** que un jugador está sano  
      **Cuando** se consulta disponibilidad  
      **Entonces** el jugador aparece como “disponible”

---

## Restricciones
1. No se pueden inventar precios.
2. No se pueden fichar jugadores sin estadísticas reales.

## Supuestos
1. API-Football provee estado de lesión.

## Preguntas abiertas
- [ ] ¿Habrá negociación o precio fijo?
