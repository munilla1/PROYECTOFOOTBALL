package com.example.football.usuario.infrastructure.security;

import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.Rol;
import com.example.football.usuario.infrastructure.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    public RoleAuthorizationFilter(JwtTokenProvider jwtTokenProvider, UsuarioRepository usuarioRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // 🔴 RUTA PÚBLICA: REGISTRO DE USUARIO
        if (requestPath.equals("/api/usuarios") && request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String rolRequerido = obtenerRolRequerido(requestPath);
        if (rolRequerido == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extraerTokenDelHeader(request);
        if (token == null) {
            denegarAcceso(response, "Token no encontrado", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String rolDelUsuario = jwtTokenProvider.extraerRolDelToken(token);

            if (!rolDelUsuario.equals(rolRequerido)) {
                denegarAcceso(response, "Acceso denegado: se requiere rol " + rolRequerido,
                        HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            denegarAcceso(response, "Token inválido: " + e.getMessage(),
                    HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String obtenerRolRequerido(String requestPath) {

        // rutas públicas
        if (requestPath.equals("/api/usuarios")) {
            return null;
        }

        if (requestPath.startsWith("/api/admin/")) {
            return "admin";
        }

        return null;
    }

    private String extraerTokenDelHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void denegarAcceso(HttpServletResponse response, String mensaje, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
