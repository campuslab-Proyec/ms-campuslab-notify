package com.example.ms_campuslab_notify.dto.admin;

/** Fila del listado de colas (GET /api/admin/queues). */
public record QueueSummary(
        String name,
        long messages,
        long messagesReady,
        long messagesUnacknowledged,
        int consumers,
        boolean durable,
        boolean autoDelete,
        String deadLetterExchange,
        Long messageTtl,
        boolean managed
) {
}
