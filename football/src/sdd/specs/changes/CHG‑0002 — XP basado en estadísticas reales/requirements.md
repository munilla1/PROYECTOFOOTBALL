# Requisitos - CHG-0002

> Este archivo debe completarse y revisarse **antes** de crear tareas o escribir código.

## Cambio de estado
Cuando este archivo esté completo, actualice el estado en `proposal.md` de `borrador` a `listo`.

---

## REQ-001: Calcular XP según estadísticas reales
> El sistema debe calcular XP usando goles, asistencias, minutos y rating real.

### Criterios de aceptación
- [ ] **Dado** que un jugador tiene estadísticas reales  
      **Cuando** se ejecuta el cálculo de XP  
      **Entonces** XP = goles × 50 + asistencias × 30 + minutos × 0.5 + rating × 10  
- [ ] **Dado** que las estadísticas están incompletas  
      **Cuando** se intenta calcular XP  
      **Entonces** el sistema registra un error y no actualiza XP

---

## REQ-002: Actualizar XP automáticamente tras cada partido real
> El sistema debe recalcular XP cuando API-Football actualice estadísticas.

### Criterios de aceptación
- [ ] **Dado** que finaliza un partido real  
      **Cuando** se actualizan estadísticas  
      **Entonces** el XP del jugador se recalcula y persiste  
- [ ] **Dado** que API-Football no devuelve datos  
      **Cuando** se intenta actualizar XP  
      **Entonces** el sistema conserva el XP anterior

---

## Restricciones
1. El cálculo debe ser determinista.
2. No se pueden inventar estadísticas.

## Supuestos
1. Las estadísticas reales contienen rating y minutos.

## Preguntas abiertas
- [ ] ¿Se aplicarán multiplicadores por posición?
