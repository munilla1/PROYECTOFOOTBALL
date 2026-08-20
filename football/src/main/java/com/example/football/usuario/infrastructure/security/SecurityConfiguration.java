package com.example.football.usuario.infrastructure.security;

import com.example.football.sesiones.presentation.SesionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final SesionAuthenticationFilter sesionAuthenticationFilter;
    private final RoleAuthorizationFilter roleAuthorizationFilter;
    private final Environment environment;

    public SecurityConfiguration(SesionAuthenticationFilter sesionAuthenticationFilter, 
                                 RoleAuthorizationFilter roleAuthorizationFilter,
                                 Environment environment) {
        this.sesionAuthenticationFilter = sesionAuthenticationFilter;
        this.roleAuthorizationFilter = roleAuthorizationFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        
        if (isDev) {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .headers(headers -> headers.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

            return http.build();
        }

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/styles/**", "/ui/**", "/core/**", "/domain/**").permitAll()
                .requestMatchers("/ui/login.html").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/sesiones/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/ui/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/ui/panel.html", true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .httpBasic(httpBasic -> {})
            .addFilterBefore(sesionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(roleAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
