package com.example.football.sesiones.presentation;

import com.example.football.sesiones.application.SesionException;
import com.example.football.sesiones.application.SesionService;
import com.example.football.sesiones.domain.SesionUsuario;
import com.example.football.usuario.application.UsuarioRepository;
import com.example.football.usuario.domain.Usuario;
import com.example.football.usuario.presentation.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Component
public class SesionAuthenticationFilter extends OncePerRequestFilter {

    private final SesionService sesionService;
    private final UsuarioRepository usuarioRepository;

    public SesionAuthenticationFilter(SesionService sesionService,
                                      UsuarioRepository usuarioRepository) {
        this.sesionService = sesionService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Rutas públicas: NO pasan por validación de sesión
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Si ya hay Authentication (puesta por Spring Security), no hacemos nada
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !isAnonymous(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer token de la request
        String token = SesionController.tokenFrom(request);

        if (token == null || token.isBlank()) {
            writeError(response, "sesion.no-autenticado");
            return;
        }

        try {
            UUID userId = sesionService.autenticar(token);

            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new SesionException("usuario.no-existe", HttpStatus.UNAUTHORIZED));

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(usuario.rol().valor()));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);


            // 6. Envolver request con principal = userId
            HttpServletRequest wrapped = new PrincipalRequestWrapper(request, userId);
            filterChain.doFilter(wrapped, response);

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
        return "anonymousUser".equals(auth.getPrincipal())
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
    }

    private void writeError(HttpServletResponse response, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + code + "\"}");
    }

    /**
     * Rutas que NO deben pasar por validación de sesión:
     * login, estáticos, APIs públicas, registro, health, etc.
     */
    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        // Login HTML y endpoint de login de formulario
        if (path.equals("/ui/login.html") || path.equals("/login")) {
            return true;
        }

        // Recursos estáticos y UI pública
        if (path.startsWith("/styles/")
                || path.startsWith("/core/")
                || path.startsWith("/domain/")
                || path.startsWith("/ui/")) {
            return true;
        }

        // API de login de sesiones
        if (path.equals("/api/sesiones/login")) {
            return true;
        }

        // Registro de usuarios (POST /api/usuarios)
        if (method.equals("POST") && path.equals("/api/usuarios")) {
            return true;
        }

        // APIs públicas / auth / health
        if (path.startsWith("/api/public/")
                || path.startsWith("/api/auth/")
                || path.startsWith("/api/health/")) {
            return true;
        }

        // Todo lo demás NO es público → requiere sesión
        return false;
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
