package com.example.ms_campuslab_notify.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body de POST y DELETE /api/admin/bindings. */
public record BindingRequest(

        @NotBlank(message = "La cola es obligatoria")
        @Pattern(regexp = AdminPatterns.NAME, message = "queue: " + AdminPatterns.NAME_MESSAGE)
        String queue,

        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = AdminPatterns.NAME, message = "exchange: " + AdminPatterns.NAME_MESSAGE)
        String exchange,

        /** Vacía o ausente para exchanges fanout. */
        @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
        @Pattern(regexp = AdminPatterns.ROUTING_KEY, message = "routingKey: " + AdminPatterns.ROUTING_KEY_MESSAGE)
        String routingKey
) {
}
