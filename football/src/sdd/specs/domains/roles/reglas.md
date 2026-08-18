# Reglas del dominio Roles

## R-201: Un usuario debe tener exactamente un rol
No se permiten múltiples roles simultáneos.

## R-202: El rol admin tiene acceso a funciones avanzadas
Logs, configuraciones, gestión de usuarios.

## R-203: El rol usuario solo accede a funciones del juego
No puede modificar configuraciones internas.

## R-204: El rol debe validarse en cada acción protegida
Acceso restringido por middleware.
