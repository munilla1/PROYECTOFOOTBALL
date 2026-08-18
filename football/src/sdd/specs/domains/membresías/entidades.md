# Entidades del dominio Membresías

## Membresia
Representa el estado de suscripción del usuario.

### Atributos
- id_usuario
- tipo (trial | normal | premium)
- fecha_inicio
- fecha_fin
- estado (activa | expirada)
- id_stripe_customer
- id_stripe_subscription
