package com.example.ms_campuslab_notify.dto;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Envelope de eventos publicado por ms-campuslab-bookings (BOOKING_STATUS_CHANGED). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationMessageDTO implements Serializable {
    private String type;
    private String eventId;
    private String timestamp;
    private String traceId;
    private String correlationId;
    private Map<String, Object> payload; // bookingId, resourceId, studentId, status
}
