package com.example.ms_campuslab_notify.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Body de POST /api/admin/queues. */
public record CreateQueueRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Size(max = 255, message = "El nombre de la cola no puede superar 255 caracteres")
        @Pattern(regexp = AdminPatterns.NAME, message = AdminPatterns.NAME_MESSAGE)
        String name,

        /** Por defecto true: la cola sobrevive a un reinicio del broker. */
        Boolean durable,

        /** Por defecto false: la cola se borra cuando se desconecta su último consumidor. */
        Boolean autoDelete,

        @Pattern(regexp = AdminPatterns.NAME, message = "deadLetterExchange: " + AdminPatterns.NAME_MESSAGE)
        String deadLetterExchange,

        @Size(max = 255, message = "deadLetterRoutingKey no puede superar 255 caracteres")
        @Pattern(regexp = AdminPatterns.ROUTING_KEY, message = "deadLetterRoutingKey: " + AdminPatterns.ROUTING_KEY_MESSAGE)
        String deadLetterRoutingKey,

        @Positive(message = "messageTtl debe ser mayor que 0 (milisegundos)")
        Integer messageTtl,

        @Positive(message = "maxLength debe ser mayor que 0")
        Integer maxLength
) {
}
