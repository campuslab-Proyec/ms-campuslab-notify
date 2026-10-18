package com.example.ms_campuslab_notify.exception;

/**
 * Mensaje con datos inválidos: reintentarlo no lo arregla, así que el consumidor
 * lo rechaza sin reencolar y termina en la DLQ.
 */
public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String message) {
        super(message);
    }
}
