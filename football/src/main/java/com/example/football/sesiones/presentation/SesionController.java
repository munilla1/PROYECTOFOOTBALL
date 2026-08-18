package com.example.football.sesiones.presentation;

import com.example.football.sesiones.application.SesionService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.football.sesiones.presentation.SesionDtos.LoginRequest;
import static com.example.football.sesiones.presentation.SesionDtos.LoginResponse;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {
    private static final String COOKIE_NAME = "football_session";
    private final SesionService service;
    private final boolean secureCookie;

    public SesionController(SesionService service,
                            @Value("${app.session.secure-cookie:false}") boolean secureCookie) {
        this.service = service;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse login = LoginResponse.from(service.login(request.email(), request.password()));
        response.addHeader("Set-Cookie", sessionCookie(login.token(), -1).toString());
        return ResponseEntity.ok(login);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(tokenFrom(request));
        response.addHeader("Set-Cookie", sessionCookie("", 0).toString());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private ResponseCookie sessionCookie(String value, long maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    static String tokenFrom(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
