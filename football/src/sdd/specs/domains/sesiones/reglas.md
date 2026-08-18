# Reglas del dominio Sesiones

## R-101: Una sesión requiere credenciales válidas
Email y contraseña deben coincidir con un usuario registrado.

## R-102: El token debe ser único y firmado
Se genera mediante JWT o equivalente.

## R-103: La sesión expira automáticamente
Por tiempo o inactividad.

## R-104: Un usuario puede tener múltiples sesiones
Siempre que los tokens sean válidos.

## R-105: Acceso a rutas protegidas requiere token válido
Si el token expira, se bloquea el acceso.
