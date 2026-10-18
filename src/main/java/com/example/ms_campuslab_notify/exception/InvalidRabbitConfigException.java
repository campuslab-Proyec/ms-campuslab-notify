package com.example.ms_campuslab_notify.exception;

/** Parámetros válidos por separado pero que juntos forman una configuración inválida (HTTP 400). */
public class InvalidRabbitConfigException extends RuntimeException {

    public InvalidRabbitConfigException(String message) {
        super(message);
    }
}
