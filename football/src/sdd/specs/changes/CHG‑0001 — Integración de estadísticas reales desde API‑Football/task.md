# Tareas - CHG-0001

> Cree tareas solo después de que `requirements.md` esté completo y revisado.
Cada tarea hace referencia a al menos un requisito. Sin requisito, no hay tarea.

## Cambiar estado
Actualiza `proposal.md` a "in-progress" cuando se inicie la primera tarea.

---

## Fase 1 - Backend

- [ ] **T-001** - Implementar cliente HTTP para API-Football + *REQ-001*
- [ ] **T-002** - Mapear estadísticas reales de jugadores a entidades internas + *REQ-001*
- [ ] **T-003** - Mapear fixtures reales a partidos internos + *REQ-002*
- [ ] **T-004** - Implementar sistema de reintentos y registro de errores + *REQ-001, REQ-002*

## Fase 2 - Contratos de API

- [ ] **T-005** - Crear contrato `interfaces/api-football-player.ts` + *REQ-001*
- [ ] **T-006** - Crear contrato `interfaces/api-football-fixture.ts` + *REQ-002*

## Fase 3 - Revisión de seguridad

- [ ] **T-SEC** - Completar checklist en `sdd/security/checklists/security-review.md`
- [ ] **T-SEC-2** - Registrar hallazgos y evidencias en `evidence.md`

## Fase 4 - Frontend

- [ ] **T-007** - Mostrar estadísticas reales del jugador en UI + *REQ-001*
- [ ] **T-008** - Mostrar partidos reales sincronizados + *REQ-002*

## Fase 5 - Infraestructura

- [ ] **T-009** - Configurar variables de entorno para API-Football (KEY, HOST)

## Fase 6 - Verificación

- [ ] **T-010** - Escribir pruebas de aceptación para REQ-001
- [ ] **T-011** - Escribir pruebas de aceptación para REQ-002
- [ ] **T-012** - Ejecutar pruebas completas en verde
- [ ] **T-013** - Completar `evidence.md` con resultados y aprobación

## Notas de implementación
- Considerar rate-limit de API-Football.
- Implementar caché para evitar llamadas redundantes.
