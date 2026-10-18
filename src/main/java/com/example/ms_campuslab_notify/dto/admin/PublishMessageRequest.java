package com.example.ms_campuslab_notify.dto.admin;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body de POST /api/admin/messages: publica un mensaje JSON de prueba en un exchange. */
public record PublishMessageRequest(

        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = AdminPatterns.NAME, message = "exchange: " + AdminPatterns.NAME_MESSAGE)
        String exchange,

        @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
        @Pattern(regexp = AdminPatterns.ROUTING_KEY, message = "routingKey: " + AdminPatterns.ROUTING_KEY_MESSAGE)
        String routingKey,

        @NotEmpty(message = "El mensaje (payload) no puede estar vacío")
        Map<String, Object> payload
) {
}
