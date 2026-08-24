package com.example.football.jornadas.infrastructure.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración del job de sincronización de jornadas.
 * Vinculada a propiedades: jornadas.sync.*
 */
@Component
@ConfigurationProperties(prefix = "jornadas.sync")
public class JornadasJobConfiguration {
    private String cron = "0 0 3 * * *";  // 03:00 UTC diariamente
    private boolean enabled = true;
    private Integer currentSeason = 2024;
    private String[] leagues = {"LaLiga", "Premier League", "Serie A", "Bundesliga", "Ligue 1"};

    // Getters y Setters
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(Integer currentSeason) {
        this.currentSeason = currentSeason;
    }

    public String[] getLeaguesToSync() {
        return leagues;
    }

    public void setLeagues(String[] leagues) {
        this.leagues = leagues;
    }
}
