package com.example.football.usuario.presentation;

import com.example.football.usuario.application.EmailDuplicadoException;
import com.example.football.usuario.application.UsuarioNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ApiError> emailDuplicado(EmailDuplicadoException exception) {
        return response(HttpStatus.CONFLICT, "usuario.email-duplicado", exception.getMessage());
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ApiError> usuarioNoEncontrado(UsuarioNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "usuario.progreso-no-encontrado", exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> noAutorizado(UnauthorizedException exception) {
        return response(HttpStatus.UNAUTHORIZED, "sesion.no-autenticado", exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> prohibido(ForbiddenException exception) {
        return response(HttpStatus.FORBIDDEN, "usuario.no-autorizado", exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> solicitudInvalida(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, "usuario.datos-invalidos", exception.getMessage());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), code, message));
    }

    public record ApiError(Instant timestamp, String code, String message) {
    }
}
