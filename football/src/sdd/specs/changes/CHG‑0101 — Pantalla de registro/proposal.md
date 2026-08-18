# CHG-0101 – Pantalla de registro

> Crear la interfaz de registro de usuario para ProyectoFootball, conectada al backend del dominio Usuario.

## Estado actual
`borrador`

## Problema
El sistema permite registrar usuarios mediante el backend (CHG‑0007), pero **no existe una interfaz gráfica** donde los usuarios puedan:

- introducir sus datos,
- validar la información,
- recibir feedback visual,
- iniciar su experiencia dentro del juego.

Actualmente, el registro solo puede hacerse vía API, lo cual impide el acceso de usuarios reales.

## Objetivo
Crear una pantalla de registro funcional, clara y accesible, que permita crear un usuario y almacenarlo en el backend siguiendo las reglas del dominio.

## Alcance

### Incluye
- Formulario con campos:
  - nombre
  - email
  - contraseña
- Validaciones visuales en tiempo real.
- Llamada al endpoint de registro del backend.
- Manejo de errores del dominio (email duplicado, formato inválido, etc.).
- Redirección automática a la pantalla de login tras registro exitoso.

### Excluye
- Pantalla de login (CHG‑0102).
- Gestión de sesiones.
- Panel de usuario.

## Impacto estimado

| Área | Nivel | Notas |
|------|-------|-------|
| Frontend | alto | Nueva pantalla + validaciones |
| Backend | bajo | Solo consumo de endpoint existente |
| UX/UI | medio | Diseño de formulario |
| Seguridad | medio | Validación estricta de entrada |

## Dominio afectado
Consulte sdd/specs/domains/usuario/spec.md

## Dependencias
- Depende de CHG‑0007 (usuarios persistentes).
