# CHG-0010 - Sistema de membresías y cobro con Stripe

> Implementar suscripciones trial, normal y premium mediante Stripe.

## Estado actual
`borrador`

## Problema
El videojuego no tiene un sistema de monetización.  
No existe forma de limitar funciones según membresía ni de cobrar por el servicio.

**No hay suscripciones ni integración con Stripe.**

## Objetivo
Crear un sistema de membresías con Stripe que permita gestionar pagos y beneficios del usuario.

## Alcance

### Incluye
- Membresías disponibles:
  - `trial` → 7 días gratis desde creación del usuario  
  - `normal` → 5 €/año  
  - `premium` → 10 €/año  
- Integración con Stripe:
  - creación de cliente  
  - creación de suscripción  
  - webhooks para actualizar estado  
- Cambio automático de `trial` a `normal` tras 7 días si no se elige otro plan.
- Restricción de funciones según membresía (en futuro CHG).

### Excluye
- Beneficios avanzados de cada plan (se añadirán más adelante).
- Pago por funcionalidades individuales.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Backend | alto | Integración Stripe + webhooks |
| Desarrollo frontend | medio | Formularios de pago |
| Base de datos | medio | Campos de membresía + fechas |
| Infraestructura | medio | Webhooks + seguridad |
| Seguridad - sensible | sí | Datos de pago (tokenizados por Stripe) |

## Dominio afectado
Consulte sdd/specs/domains/membresias/spec.md  
Consulte sdd/specs/domains/usuario/spec.md

## Dependencias
- Depende de CHG‑0007 (usuarios persistentes).
- Depende de CHG‑0008 (sesiones del usuario).
