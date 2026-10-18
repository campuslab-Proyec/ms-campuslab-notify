package com.example.ms_campuslab_notify.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/** Convierte las excepciones de la API en respuestas JSON con el código HTTP adecuado. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Errores de @Valid en el body: se informa cada campo inválido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "El body no es un JSON válido o tiene tipos incorrectos", null);
    }

    @ExceptionHandler(InvalidRabbitConfigException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidConfig(InvalidRabbitConfigException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ResourceConflictException e) {
        return build(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> handleBrokerDown(AmqpConnectException e) {
        log.error("[ADMIN] No hay conexión con RabbitMQ", e);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "No hay conexión con RabbitMQ", null);
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleAmqp(AmqpException e) {
        log.error("[ADMIN] RabbitMQ rechazó la operación", e);
        return build(HttpStatus.BAD_GATEWAY, "RabbitMQ rechazó la operación: " + e.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, List<String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        if (details != null) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}
