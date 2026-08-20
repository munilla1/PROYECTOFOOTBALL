package com.example.football.usuario.presentation;

import com.example.football.usuario.application.UsuarioNotFoundException;
import com.example.football.usuario.domain.RolInvalidoException;
import com.example.football.usuario.domain.NoAutorizadoParaCambiarRolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Manejador global de excepciones para la API de roles y usuarios.
 * 
 * Responsabilidades:
 * - Mapear excepciones de dominio a respuestas HTTP adecuadas
 * - Registrar errores para auditoría
 * - Proporcionar respuestas coherentes en formato JSON
 * - Proteger información sensible en mensajes de error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de rol inválido.
     * Devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(RolInvalidoException.class)
    public ResponseEntity<ErrorDetail> handleRolInvalido(
            RolInvalidoException ex,
            WebRequest request) {
        
        log.warn("Rol inválido solicitado: {}", ex.getMessage());
        
        ErrorDetail errorDetail = ErrorDetail.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Rol Inválido")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones cuando el usuario no está autorizado para cambiar roles.
     * Devuelve HTTP 403 Forbidden.
     */
    @ExceptionHandler(NoAutorizadoParaCambiarRolException.class)
    public ResponseEntity<ErrorDetail> handleNoAutorizadoParaCambiarRol(
            NoAutorizadoParaCambiarRolException ex,
            WebRequest request) {
        
        log.warn("Intento no autorizado de cambiar rol: {}", ex.getMessage());
        
        ErrorDetail errorDetail = ErrorDetail.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("No Autorizado")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorDetail, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja excepciones cuando un usuario no es encontrado.
     * Devuelve HTTP 404 Not Found.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleUsuarioNotFound(
            UsuarioNotFoundException ex,
            WebRequest request) {
        
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        
        ErrorDetail errorDetail = ErrorDetail.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Usuario No Encontrado")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones genéricas de IllegalArgumentException.
     * Devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetail> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.warn("Argumento inválido: {}", ex.getMessage());
        
        ErrorDetail errorDetail = ErrorDetail.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Argumento Inválido")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones no esperadas.
     * Devuelve HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Error inesperado: ", ex);
        
        ErrorDetail errorDetail = ErrorDetail.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno del Servidor")
                .message("Ocurrió un error inesperado. Por favor, intente más tarde.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DTO para respuestas de error estructuradas.
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorDetail {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
}
