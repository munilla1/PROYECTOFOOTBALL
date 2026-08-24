-- V004__add_jornada_reference_to_partidos.sql
-- 
-- Migración de base de datos para CHG-0006: Jornadas sincronizadas con partidos reales
-- 
-- Operaciones:
-- 1. Crear tabla jornadas con restricción UNIQUE(league, season, round_number)
-- 2. Crear índices para optimizar búsquedas
-- 3. Agregar FK desde partidos a jornadas
-- 4. Crear índice en partidos.jornada_id

-- Crear tabla jornadas si no existe
CREATE TABLE IF NOT EXISTS jornadas (
    id UUID PRIMARY KEY,
    league VARCHAR(50) NOT NULL,
    season INTEGER NOT NULL,
    round_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    match_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    synchronized_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_jornada_league_season_round UNIQUE (league, season, round_number)
);

-- Crear índices en tabla jornadas
CREATE INDEX IF NOT EXISTS idx_jornada_league_season ON jornadas(league, season);
CREATE INDEX IF NOT EXISTS idx_jornada_status ON jornadas(status);

-- Agregar columna FK a partidos (si no existe)
ALTER TABLE partidos 
ADD COLUMN IF NOT EXISTS jornada_id UUID;

-- Agregar constraint FK (si no existe)
ALTER TABLE partidos
ADD CONSTRAINT IF NOT EXISTS fk_partidos_jornada 
FOREIGN KEY (jornada_id) REFERENCES jornadas(id) ON DELETE RESTRICT;

-- Crear índice en la FK
CREATE INDEX IF NOT EXISTS idx_partido_jornada ON partidos(jornada_id);

-- Comentarios de documentación (PostgreSQL)
COMMENT ON TABLE jornadas IS 'Jornadas sincronizadas desde API-Football. Representa rondas de competición.';
COMMENT ON COLUMN jornadas.id IS 'UUID único de jornada';
COMMENT ON COLUMN jornadas.league IS 'Nombre de la liga (ej: LaLiga, Premier League)';
COMMENT ON COLUMN jornadas.season IS 'Temporada/año';
COMMENT ON COLUMN jornadas.round_number IS 'Número de ronda (1-38)';
COMMENT ON COLUMN jornadas.status IS 'Estado: NOT_STARTED, IN_PROGRESS, FINISHED, POSTPONED';
COMMENT ON COLUMN jornadas.match_count IS 'Cantidad total de partidos en la jornada';
COMMENT ON COLUMN jornadas.created_at IS 'Timestamp de creación';
COMMENT ON COLUMN jornadas.synchronized_at IS 'Timestamp de última sincronización desde API';
COMMENT ON COLUMN jornadas.updated_at IS 'Timestamp de último cambio';

COMMENT ON TABLE partidos IS 'Agregación de comentario existente: FK a jornadas sincronizadas';
COMMENT ON COLUMN partidos.jornada_id IS 'FK a jornada real sincronizada desde API-Football. ON DELETE RESTRICT previene eliminación de jornadas con partidos activos.';
