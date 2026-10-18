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

/** Dominio: tickets de preparación de equipos para el equipo de operaciones (q.cmd.prep). */
@Slf4j
@Component
public class PreparationConsumer {

    @RabbitListener(queues = "${campuslab.rabbitmq.queues.prep.name}",
                    containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receivePrepTicket(NotificationRequest notification,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                  @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (notification.getMessage() == null || notification.getMessage().isBlank()) {
                throw new InvalidMessageException("El ticket de preparación no tiene detalle");
            }
            log.info("🛠️ [TICKET DE PREPARACIÓN GENERADO]");
            log.info("Detalle para Operador: {}", notification.getMessage());
            log.info("Tipo de Evento: {}", notification.getType());

            channel.basicAck(deliveryTag, false);
        } catch (InvalidMessageException e) {
            log.warn("❌ [TICKET RECHAZADO] {} -> enviado a la DLQ", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("⚠️ [TICKET ERROR] {} (reencolar={})", e.getMessage(), !redelivered, e);
            channel.basicNack(deliveryTag, false, !redelivered);
        }
    }
}
