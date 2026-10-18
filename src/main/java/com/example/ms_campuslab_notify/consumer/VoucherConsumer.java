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

/** Dominio: emisión de comprobantes (q.cmd.voucher). */
@Slf4j
@Component
public class VoucherConsumer {

    @RabbitListener(queues = "${campuslab.rabbitmq.queues.voucher.name}",
                    containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receiveVoucherGeneration(NotificationRequest notification,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                         @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
                throw new InvalidMessageException("El comprobante no tiene destinatario");
            }
            log.info("🎟️ [VOUCHER EMITIDO]");
            log.info("Comprobante para: {}", notification.getRecipient());
            log.info("Detalles: {}", notification.getMessage());

            channel.basicAck(deliveryTag, false);
        } catch (InvalidMessageException e) {
            log.warn("❌ [VOUCHER RECHAZADO] {} -> enviado a la DLQ", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("⚠️ [VOUCHER ERROR] {} (reencolar={})", e.getMessage(), !redelivered, e);
            channel.basicNack(deliveryTag, false, !redelivered);
        }
    }
}
