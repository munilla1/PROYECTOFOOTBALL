-- V003__create_estadisticas_tables.sql
-- Crear tablas para el módulo de estadísticas reales (CHG-0001)

-- Tabla de Jugadores
CREATE TABLE IF NOT EXISTS players (
    id VARCHAR(36) PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(10) NOT NULL,
    age INTEGER NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    team_id VARCHAR(255) NOT NULL,
    
    -- Estadísticas reales (normalizadas 0-100)
    season INTEGER NOT NULL,
    league VARCHAR(100) NOT NULL,
    appearances INTEGER NOT NULL DEFAULT 0,
    goals INTEGER NOT NULL DEFAULT 0,
    assists INTEGER NOT NULL DEFAULT 0,
    passes_accuracy INTEGER NOT NULL DEFAULT 0,
    dribbles_success INTEGER NOT NULL DEFAULT 0,
    tackles INTEGER NOT NULL DEFAULT 0,
    performance_score INTEGER NOT NULL DEFAULT 0,
    
    -- Timestamps
    last_stats_updated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para consultas frecuentes en players
CREATE INDEX IF NOT EXISTS idx_external_id ON players(external_id);
CREATE INDEX IF NOT EXISTS idx_team_id ON players(team_id);
CREATE INDEX IF NOT EXISTS idx_league_season ON players(league, season);

-- Tabla de Partidos
CREATE TABLE IF NOT EXISTS matches (
    id VARCHAR(36) PRIMARY KEY,
    fixture_id VARCHAR(255) NOT NULL UNIQUE,
    round INTEGER NOT NULL,
    league VARCHAR(100) NOT NULL,
    season INTEGER NOT NULL,
    match_date TIMESTAMP NOT NULL,
    home_team_id VARCHAR(255) NOT NULL,
    away_team_id VARCHAR(255) NOT NULL,
    home_goals INTEGER NOT NULL DEFAULT 0,
    away_goals INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    player_stats_json TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para consultas frecuentes en matches
CREATE INDEX IF NOT EXISTS idx_fixture_id ON matches(fixture_id);
CREATE INDEX IF NOT EXISTS idx_round ON matches(round);
CREATE INDEX IF NOT EXISTS idx_home_team ON matches(home_team_id);
CREATE INDEX IF NOT EXISTS idx_away_team ON matches(away_team_id);
CREATE INDEX IF NOT EXISTS idx_league_season ON matches(league, season);

-- Tabla de Logs de Sincronización
CREATE TABLE IF NOT EXISTS sync_logs (
    id VARCHAR(36) PRIMARY KEY,
    sync_timestamp TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    league VARCHAR(100) NOT NULL,
    season INTEGER NOT NULL,
    round_synced INTEGER NOT NULL,
    players_updated INTEGER NOT NULL DEFAULT 0,
    matches_updated INTEGER NOT NULL DEFAULT 0,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    errors_json TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para consultas frecuentes en sync_logs
CREATE INDEX IF NOT EXISTS idx_sync_timestamp ON sync_logs(sync_timestamp);
CREATE INDEX IF NOT EXISTS idx_sync_status ON sync_logs(status);
CREATE INDEX IF NOT EXISTS idx_sync_league_season ON sync_logs(league, season);
