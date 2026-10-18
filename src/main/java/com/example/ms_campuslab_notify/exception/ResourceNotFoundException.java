package com.example.ms_campuslab_notify.exception;

/** La cola o el exchange solicitado no existe en RabbitMQ (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
