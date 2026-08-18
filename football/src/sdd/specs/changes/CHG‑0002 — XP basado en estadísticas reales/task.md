# Tareas - CHG-0002

> Cree tareas solo después de que `requirements.md` esté completo y revisado.

## Cambiar estado
Actualiza `proposal.md` a "in-progress" cuando se inicie la primera tarea.

---

## Fase 1 - Backend

- [ ] **T-001** - Implementar fórmula de XP basada en estadísticas reales + *REQ-001*
- [ ] **T-002** - Integrar cálculo de XP con sincronización de CHG-0001 + *REQ-002*
- [ ] **T-003** - Persistir XP acumulado en Firestore + *REQ-002*

## Fase 2 - Contratos de API
(No aplica, usa contratos de CHG-0001)

## Fase 3 - Revisión de seguridad
(No aplica)

## Fase 4 - Frontend

- [ ] **T-004** - Mostrar XP actualizado en la ficha del jugador + *REQ-001*
- [ ] **T-005** - Mostrar progreso del nivel + *REQ-002*

## Fase 5 - Infraestructura
(No aplica)

## Fase 6 - Verificación

- [ ] **T-006** - Pruebas de aceptación para REQ-001
- [ ] **T-007** - Pruebas de aceptación para REQ-002
- [ ] **T-008** - Ejecutar pruebas completas en verde
- [ ] **T-009** - Completar `evidence.md`

## Notas de implementación
- Validar que rating y minutos existan antes de calcular XP.
