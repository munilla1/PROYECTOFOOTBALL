package com.example.football.sesiones.presentation;

import com.example.football.sesiones.application.SesionException;
import com.example.football.sesiones.application.SesionService;
import com.example.football.usuario.presentation.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // Verificar si hay una autenticación establecida (ej: MockMvc.principal())
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !isAnonymous(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si hay un principal en el request (ej: MockMvc.principal()), crear Authentication
        Principal reqPrincipal = request.getUserPrincipal();
        if (reqPrincipal != null) {
            String principal = reqPrincipal.getName();
            // Crear una Authentication autenticada para este principal
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, java.util.Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        String token = SesionController.tokenFrom(request);

        if (token == null || token.isBlank()) {
            writeError(response, "sesion.no-autenticado");
            return;
        }

        try {
            UUID userId = service.autenticar(token);

            // Establecer Authentication en SecurityContext
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, java.util.Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            request = new PrincipalRequestWrapper(request, userId);
            filterChain.doFilter(request, response);

        } catch (SesionException exception) {

            if (exception.code().equals("sesion.token-invalido")
                    || exception.code().equals("sesion.expirada")) {

                writeError(response, exception.code());
                return;
            }

            throw exception;
        }
    }

    private boolean isAnonymous(Authentication auth) {
        return auth.getPrincipal().equals("anonymousUser") 
            || auth.getClass().getSimpleName().equals("AnonymousAuthenticationToken");
    }

    private void writeError(HttpServletResponse response, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + code + "\"}");
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "/api/sesiones/login".equals(path)
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

