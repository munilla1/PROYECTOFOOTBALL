# Tareas - CHG-0006

> Cree tareas solo después de que `requirements.md` esté completo y revisado.

## Cambiar estado
Actualiza `proposal.md` a "in-progress" cuando se inicie la primera tarea.

---

## Fase 1 - Backend

- [ ] **T-001** - Implementar sincronización de jornadas reales + *REQ-001*
- [ ] **T-002** - Implementar bloqueo de partidos cuando no haya jornada real + *REQ-002*
- [ ] **T-003** - Integrar jornadas con fixtures reales de CHG-0001 + *REQ-001, REQ-002*

## Fase 2 - Contratos de API
(No aplica)

## Fase 3 - Revisión de seguridad
(No aplica)

## Fase 4 - Frontend

- [ ] **T-004** - Mostrar calendario real sincronizado + *REQ-001*
- [ ] **T-005** - Mostrar estado “partido bloqueado” cuando no haya jornada real + *REQ-002*

## Fase 5 - Infraestructura

- [ ] **T-006** - Configurar cron job diario para sincronización de jornadas

## Fase 6 - Verificación

- [ ] **T-007** - Pruebas de aceptación para REQ-001
- [ ] **T-008** - Pruebas de aceptación para REQ-002
- [ ] **T-009** - Ejecutar pruebas completas en verde
- [ ] **T-010** - Completar `evidence.md`

## Notas de implementación
- Considerar zonas horarias de API-Football.
