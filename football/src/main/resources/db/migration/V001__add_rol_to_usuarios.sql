-- T-001: Añadir columna rol a la tabla usuarios
-- Descripción: Agrega la columna 'rol' a la tabla 'usuarios' con tipo VARCHAR(20)
-- no nulo y valor por defecto 'usuario'

ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS rol VARCHAR(20) NOT NULL DEFAULT 'usuario';

-- Crear un índice para búsquedas rápidas por rol
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);
