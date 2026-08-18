# Entidades del dominio Usuario

## Usuario
Representa a una persona registrada en ProyectoFootball.

### Atributos
- id_usuario
- nombre
- email
- contraseña_hash
- fecha_creacion
- rol (usuario | admin)
- membresia (trial | normal | premium)
- fecha_inicio_trial
- fecha_expiracion_membresia
- estado_sesion (activa | inactiva)

## ProgresoJugador
Datos persistentes del jugador asociados al usuario.

### Atributos
- nivel
- xp
- energia
- estado (normal | lesionado)
- estadisticas_acumuladas
- historial_partidos
