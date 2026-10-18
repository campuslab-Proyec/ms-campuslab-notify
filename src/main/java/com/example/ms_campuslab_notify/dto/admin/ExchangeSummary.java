package com.example.ms_campuslab_notify.dto.admin;

/** Fila del listado de exchanges (GET /api/admin/exchanges). */
public record ExchangeSummary(String name, String type, boolean durable, boolean autoDelete, boolean managed) {
}
