package com.example.ms_campuslab_notify.dto.admin;

/** Fila del listado de bindings (GET /api/admin/bindings). */
public record BindingSummary(String exchange, String queue, String routingKey) {
}
