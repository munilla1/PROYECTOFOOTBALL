# CHG-0102 – Pantalla de login

> Crear la interfaz de inicio de sesión conectada al sistema de sesiones del backend.

## Estado actual
`borrador`

## Problema
El backend implementa el sistema de sesiones (CHG‑0008), pero **no existe una pantalla de login** que permita:

- introducir credenciales,
- recibir tokens,
- iniciar sesión,
- manejar errores de autenticación.

Sin esta pantalla, los usuarios no pueden acceder al juego.

## Objetivo
Crear una pantalla de login que permita iniciar sesión y almacenar el token de sesión de forma segura.

## Alcance

### Incluye
- Formulario con:
  - email
  - contraseña
- Llamada al endpoint de login.
- Manejo de errores del dominio (credenciales inválidas, usuario inexistente).
- Almacenamiento seguro del token.
- Redirección al panel de usuario.

### Excluye
- Registro (CHG‑0101).
- Panel de usuario (CHG‑0103).
- Panel de administrador (CHG‑0104).

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Frontend | alto | Nueva pantalla + manejo de token |
| Backend | bajo | Solo consumo de endpoint existente |
| Seguridad | alto | Manejo de token y rutas protegidas |

## Dominio afectado
Consulte sdd/specs/domains/sesiones/spec.md

## Dependencias
- Depende de CHG‑0008 (sesiones del usuario).
