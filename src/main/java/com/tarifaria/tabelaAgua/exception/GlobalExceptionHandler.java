package com.tarifaria.tabelaAgua.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Limita o advice apenas aos controllers da nossa aplicação para evitar interceptar os endpoints do springdoc
@Hidden
@RestControllerAdvice(basePackages = "com.tarifaria.tabelaAgua.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Dados inválidos fornecidos na requisição";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(MethodNotAllowedException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Método HTTP não permitido para este recurso";
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", message);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Muitas requisições. Tente novamente mais tarde";
        final Map<String, Object> body = buildErrorBody(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", message);
        body.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<?> handleInternalServer(InternalServerException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Erro interno do servidor. Por favor, tente novamente mais tarde";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro inesperado no processamento da requisição";
        String details = "Tipo: " + ex.getClass().getSimpleName();
        return buildErrorResponseWithDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message, details);
    }

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String error, String message) {
        final Map<String, Object> body = buildErrorBody(status, error, message);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<?> buildErrorResponseWithDetails(HttpStatus status, String error, String message, String details) {
        final Map<String, Object> body = buildErrorBody(status, error, message);
        body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message) {
        final Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
