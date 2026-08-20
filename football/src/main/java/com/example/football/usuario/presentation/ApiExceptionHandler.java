package com.example.football.usuario.presentation;

import com.example.football.usuario.application.EmailDuplicadoException;
import com.example.football.usuario.application.UsuarioNotFoundException;
import com.example.football.usuario.presentation.UnauthorizedException;
import com.example.football.usuario.presentation.ForbiddenException;
import com.example.football.sesiones.application.SesionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<?> emailDuplicado(EmailDuplicadoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("code", "usuario.email-duplicado"));
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<?> usuarioNoEncontrado(UsuarioNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", "usuario.progreso-no-encontrado"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> noAutorizado(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("code", "sesion.no-autenticado"));
    }

    @ExceptionHandler(SesionException.class)
    public ResponseEntity<?> sesion(SesionException exception) {

        if ("sesion.datos-invalidos".equals(exception.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", exception.code()));
        }

        if ("usuario.no-existe".equals(exception.code())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", exception.code()));
        }

        return ResponseEntity.status(exception.status())
                .body(Map.of("code", exception.code()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> prohibido(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("code", "usuario.no-autorizado"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> argumentoInvalido(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", "usuario.datos-invalidos"));
    }
}


