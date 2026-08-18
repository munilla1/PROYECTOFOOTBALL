# CHG-0105 – Stripe Checkout UI

> Implementar la interfaz de pago mediante Stripe Checkout para activar membresías.

## Estado actual
`borrador`

## Problema
El backend implementa Stripe (CHG‑0010), pero **no existe una interfaz** donde el usuario pueda:

- elegir un plan,
- iniciar el proceso de pago,
- ser redirigido a Stripe Checkout,
- volver al sistema tras el pago,
- ver su membresía actualizada.

Sin esta pantalla, el sistema de monetización no es utilizable.

## Objetivo
Crear la pantalla de pago con Stripe Checkout y su flujo completo.

## Alcance

### Incluye
- Mostrar planes:
  - trial (si aplica)
  - normal
  - premium
- Botón “Pagar con Stripe”.
- Redirección a Stripe Checkout.
- Manejo de retorno (success / cancel).
- Actualización visual de la membresía.
- Manejo de errores del dominio.

### Excluye
- Implementación backend (ya cubierta en CHG‑0010).
- Beneficios avanzados de cada plan.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Frontend | alto | Nueva pantalla + flujo de pago |
| Backend | medio | Endpoints de sesión de pago |
| Seguridad | crítico | Manejo seguro de tokens y redirecciones |

## Dominio afectado
Consulte sdd/specs/domains/membresias/spec.md

## Dependencias
- Depende de CHG‑0010 (Stripe).
- Depende de CHG‑0103 (panel de usuario).
