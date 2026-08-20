-- T-002: Crear tabla de auditoría para cambios de roles
-- Descripción: Crea la tabla 'role_audit_logs' para registrar todos los cambios
-- de roles de usuarios realizados en el sistema

CREATE TABLE IF NOT EXISTS role_audit_logs (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_anterior VARCHAR(20) NOT NULL,
    rol_nuevo VARCHAR(20) NOT NULL,
    cambiadoPor_usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT,
    CONSTRAINT valid_roles CHECK (
        rol_anterior IN ('usuario', 'admin') AND
        rol_nuevo IN ('usuario', 'admin')
    )
);

-- Crear índices para búsquedas comunes
CREATE INDEX IF NOT EXISTS idx_role_audit_logs_usuario_id 
    ON role_audit_logs(usuario_id);
    
CREATE INDEX IF NOT EXISTS idx_role_audit_logs_fecha_cambio 
    ON role_audit_logs(fecha_cambio DESC);
    
CREATE INDEX IF NOT EXISTS idx_role_audit_logs_cambiadoPor_usuario_id 
    ON role_audit_logs(cambiadoPor_usuario_id);

-- Crear vista para acceso rápido a cambios recientes
CREATE VIEW IF NOT EXISTS recent_role_changes AS
    SELECT 
        id,
        usuario_id,
        rol_anterior,
        rol_nuevo,
        cambiadoPor_usuario_id,
        fecha_cambio,
        motivo
    FROM role_audit_logs
    WHERE fecha_cambio >= CURRENT_TIMESTAMP - INTERVAL '30 days'
    ORDER BY fecha_cambio DESC;
