# Dominio: Membresías

## Descripción general
Gestiona los planes de suscripción del usuario y su integración con Stripe.

## Responsabilidades
- Crear membresías.
- Actualizar estado según Stripe.
- Controlar acceso según plan.
- Gestionar trial y renovaciones.

## Entidades
Consulte entidades.md

## Reglas
Consulte reglas.md

## Errores
Consulte errores.md

## Eventos del dominio
- membresia.creada
- membresia.expirada
- membresia.renovada
- membresia.actualizada-por-stripe

## Dependencias
- Dominio Usuario
- Stripe API
