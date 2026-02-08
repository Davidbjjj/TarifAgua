package com.tarifaria.tabelaAgua.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(MethodNotAllowedException ex) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex) {
        final Map<String, Object> body = buildErrorBody(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage());
        body.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<?> handleInternalServer(InternalServerException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String error, String message) {
        final Map<String, Object> body = buildErrorBody(status, error, message);
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

