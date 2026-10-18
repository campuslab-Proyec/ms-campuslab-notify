package com.example.ms_campuslab_notify.exception;

/** La operación choca con el estado actual de RabbitMQ: ya existe, está protegida o en uso (HTTP 409). */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
