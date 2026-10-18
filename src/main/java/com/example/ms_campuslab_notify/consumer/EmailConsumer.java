package com.example.ms_campuslab_notify.consumer;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.config.RabbitMQConfig;
import com.example.ms_campuslab_notify.dto.NotificationRequest;
import com.example.ms_campuslab_notify.exception.InvalidMessageException;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/** Dominio: notificaciones por correo (q.cmd.email). */
@Slf4j
@Component
public class EmailConsumer {

    @RabbitListener(queues = "${campuslab.rabbitmq.queues.email.name}",
                    containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receiveEmailNotification(NotificationRequest notification,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                         @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
                throw new InvalidMessageException("El correo no tiene destinatario");
            }
            log.info("📩 [EMAIL RECEIVED]");
            log.info("Para: {}", notification.getRecipient());
            log.info("Asunto: {}", notification.getSubject());
            log.info("Mensaje: {}", notification.getMessage());

            channel.basicAck(deliveryTag, false);
        } catch (InvalidMessageException e) {
            // Error de datos: no se reencola -> DLX -> q.cmd.email.dlq
            log.warn("❌ [EMAIL RECHAZADO] {} -> enviado a la DLQ", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            // Error inesperado: se reintenta una vez; si ya era un reintento, va a la DLQ
            log.error("⚠️ [EMAIL ERROR] {} (reencolar={})", e.getMessage(), !redelivered, e);
            channel.basicNack(deliveryTag, false, !redelivered);
        }
    }
}
