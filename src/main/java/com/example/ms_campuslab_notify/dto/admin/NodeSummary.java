package com.example.ms_campuslab_notify.dto.admin;

/** Nodo del clúster RabbitMQ (GET /api/admin/nodes). */
public record NodeSummary(
        String name,
        boolean running,
        String type,
        long uptimeMs,
        long memoryUsed,
        long memoryLimit,
        int partitions
) {
}
