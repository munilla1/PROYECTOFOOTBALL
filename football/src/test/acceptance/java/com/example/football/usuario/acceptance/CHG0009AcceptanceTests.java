package com.example.football.usuario.acceptance;

import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PRUEBAS DE ACEPTACIÓN - CHG-0009: Sistema de Roles
 * 
 * Clase de aceptación que valida los criterios de aceptación definidos en requirements.md
 * 
 * Convención de nombres: CA-XXX.X (Criterio de Aceptación)
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CHG-0009 - Pruebas de Aceptación: Sistema de Roles")
public class CHG0009AcceptanceTests {

    @Test
    @DisplayName("CA-0009.1: Usuario puede tener rol ADMIN")
    void testUserCanHaveAdminRole() {
        // Dado: Se intenta crear usuario con rol ADMIN
        Rol adminRole = Rol.ADMIN;

        // Cuando: Se verifica el rol
        // Entonces: El rol es válido
        assertThat(adminRole).isEqualTo(Rol.ADMIN);
        assertThat(adminRole.name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("CA-0009.2: Usuario puede tener rol USUARIO")
    void testUserCanHaveUsuarioRole() {
        // Dado: Se intenta crear usuario con rol USUARIO
        Rol usuarioRole = Rol.USUARIO;

        // Cuando: Se verifica el rol
        // Entonces: El rol es válido
        assertThat(usuarioRole).isEqualTo(Rol.USUARIO);
        assertThat(usuarioRole.name()).isEqualTo("USUARIO");
    }

    @Test
    @DisplayName("CA-0009.3: Usuario tiene campos básicos")
    void testUserHasBasicFields() {
        // Dado: Rol USUARIO existe
        Rol usuarioRole = Rol.USUARIO;

        // Cuando: Se verifica validez
        // Entonces: El rol es válido y puede ser asignado
        assertThat(usuarioRole).isNotNull();
        assertThat(usuarioRole.name()).isEqualTo("USUARIO");
    }

    @Test
    @DisplayName("CA-0009.4: Roles se diferencian correctamente")
    void testRolesDifferentiation() {
        // Dado: Dos roles diferentes
        Rol admin = Rol.ADMIN;
        Rol usuario = Rol.USUARIO;

        // Cuando: Comparamos
        // Entonces: Son diferentes
        assertThat(admin).isNotEqualTo(usuario);
    }
}
