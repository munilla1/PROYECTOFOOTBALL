package com.example.football;

import com.example.football.usuario.domain.RoleValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans de dominio para inyección de dependencias.
 * 
 * Responsabilidades:
 * - Exponer clases de dominio puro como beans de Spring cuando sea necesario
 * - Mantener la separación de capas (dominio sin dependencias de Spring)
 * 
 * Nota: Las clases de dominio se definen aquí sin annotations de Spring
 * para mantener la pureza del modelo de dominio.
 */
@Configuration
public class DomainBeansConfiguration {

    /**
     * Expone RoleValidator como un bean de Spring para inyección en la capa de aplicación.
     * 
     * RoleValidator es una clase de dominio puro que valida reglas de negocio para roles.
     * Se expone aquí para permitir su inyección en servicios de aplicación como CambiarRolDeUsuario.
     * 
     * @return nueva instancia de RoleValidator
     */
    @Bean
    public RoleValidator roleValidator() {
        return new RoleValidator();
    }
}
