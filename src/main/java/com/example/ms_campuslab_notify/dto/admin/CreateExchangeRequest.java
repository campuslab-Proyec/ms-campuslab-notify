package com.example.ms_campuslab_notify.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body de POST /api/admin/exchanges. */
public record CreateExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Size(max = 255, message = "El nombre del exchange no puede superar 255 caracteres")
        @Pattern(regexp = AdminPatterns.NAME, message = AdminPatterns.NAME_MESSAGE)
        String name,

        @NotBlank(message = "El tipo de exchange es obligatorio")
        @Pattern(regexp = AdminPatterns.EXCHANGE_TYPE, message = "El tipo debe ser direct, topic, fanout o headers")
        String type,

        /** Por defecto true. */
        Boolean durable,

        /** Por defecto false. */
        Boolean autoDelete
) {
}
