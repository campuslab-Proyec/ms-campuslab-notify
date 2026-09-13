package com.example.ms_campuslab_notify.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO implements Serializable {
    private String type;
    private String eventId;
    private String timestamp;
    private String traceId;
    private String correlationId;
    private String recipient; // Email o destinatario
    private String payload;   // Contenido o detalle
}