# Reglas del dominio Usuario

## R-001: Un usuario debe tener email único
No pueden existir dos usuarios con el mismo email.

## R-002: La contraseña debe almacenarse como hash
Nunca se guarda texto plano.

## R-003: Un usuario siempre tiene un rol válido
Valores permitidos: usuario, admin.

## R-004: Un usuario siempre tiene una membresía válida
Valores permitidos: trial, normal, premium.

## R-005: El progreso del jugador debe persistirse automáticamente
Cada acción relevante actualiza la base de datos.

## R-006: El trial dura exactamente 7 días desde la creación
Después pasa automáticamente a plan normal si no se elige otro plan.

## R-007: Un usuario solo puede acceder a su propio progreso
Acceso restringido por autenticación.
