package com.example.ms_campuslab_notify.exception;

/** La API HTTP de management de RabbitMQ no responde (HTTP 503). */
public class ManagementUnavailableException extends RuntimeException {

    public ManagementUnavailableException(String message) {
        super(message);
    }
}
