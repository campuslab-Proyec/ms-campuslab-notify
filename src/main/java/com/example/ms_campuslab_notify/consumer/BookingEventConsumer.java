package com.example.ms_campuslab_notify.consumer;

import java.io.IOException;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.config.RabbitMQConfig;
import com.example.ms_campuslab_notify.dto.NotificationMessageDTO;
import com.example.ms_campuslab_notify.exception.InvalidMessageException;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/** Dominio: cambios de estado de reservas publicados por ms-campuslab-bookings (notify.booking.queue). */
@Slf4j
@Component
public class BookingEventConsumer {

    @RabbitListener(queues = "${campuslab.rabbitmq.queues.booking.name}",
                    containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receiveBookingStatusChanged(NotificationMessageDTO event,
                                            Channel channel,
                                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                            @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            Map<String, Object> payload = event.getPayload();
            if (payload == null || payload.get("bookingId") == null || payload.get("status") == null) {
                throw new InvalidMessageException("Evento de reserva sin bookingId o status (eventId=" + event.getEventId() + ")");
            }
            log.info("📅 [RESERVA ACTUALIZADA]");
            log.info("Reserva: {} | Recurso: {} | Estudiante: {}",
                    payload.get("bookingId"), payload.get("resourceId"), payload.get("studentId"));
            log.info("Nuevo estado: {} (eventId={})", payload.get("status"), event.getEventId());

            channel.basicAck(deliveryTag, false);
        } catch (InvalidMessageException e) {
            log.warn("❌ [EVENTO DE RESERVA RECHAZADO] {} -> enviado a la DLQ", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("⚠️ [EVENTO DE RESERVA ERROR] {} (reencolar={})", e.getMessage(), !redelivered, e);
            channel.basicNack(deliveryTag, false, !redelivered);
        }
    }
}
