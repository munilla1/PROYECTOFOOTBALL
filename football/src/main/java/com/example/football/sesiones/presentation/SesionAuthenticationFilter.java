package com.example.football.sesiones.presentation;

import com.example.football.sesiones.application.SesionException;
import com.example.football.sesiones.application.SesionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Component
public class SesionAuthenticationFilter extends OncePerRequestFilter {
    private final SesionService service;

    public SesionAuthenticationFilter(SesionService service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = SesionController.tokenFrom(request);

        if ((token == null || token.isBlank()) && request.getUserPrincipal() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token == null || token.isBlank()) {
            writeError(response, "sesion.no-autenticado", "Se requiere una sesion autenticada");
            return;
        }

        try {
            UUID userId = service.autenticar(token);

            request = new PrincipalRequestWrapper(request, userId);
            filterChain.doFilter(request, response);

        } catch (SesionException exception) {

            // 🔥 CAMBIO CRÍTICO:
            // SOLO 401 si el token es inválido o la sesión está expirada.
            if (exception.code().equals("sesion.token-invalido")
                    || exception.code().equals("sesion.expirada")) {

                writeError(response, exception.code(), exception.getMessage());
                return;
            }

            // 🔥 CAMBIO CRÍTICO:
            // Para cualquier otro error → dejar que el controlador responda.
            throw exception;
        }
    }

    private void writeError(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"timestamp\":\"" + java.time.Instant.now()
                + "\",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "/api/sesiones/login".equals(path)
                || "/api/sesiones/logout".equals(path)   // 🔥 vuelve a poner esto
                || ("POST".equals(request.getMethod()) && "/api/usuarios".equals(path))
                || !path.startsWith("/api/");
    }


    private static final class PrincipalRequestWrapper extends HttpServletRequestWrapper {
        private final Principal principal;

        private PrincipalRequestWrapper(HttpServletRequest request, UUID userId) {
            super(request);
            this.principal = () -> userId.toString();
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }
    }
}
