package com.example.ms_campuslab_notify.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.config.RabbitMQConfig;
import com.example.ms_campuslab_notify.dto.NotificationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void receiveEmailNotification(NotificationRequest notification) {
        log.info("📩 [EMAIL RECEIVED]");
        log.info("Para: {}", notification.getRecipient());
        log.info("Asunto: {}", notification.getSubject());
        log.info("Mensaje: {}", notification.getMessage());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PREP)
    public void receivePrepTicket(NotificationRequest notification) {
        log.info("🛠️ [TICKET DE PREPARACIÓN GENERADO]");
        log.info("Detalle para Operador: {}", notification.getMessage());
        log.info("Tipo de Evento: {}", notification.getType());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VOUCHER)
    public void receiveVoucherGeneration(NotificationRequest notification) {
        log.info("🎟️ [VOUCHER EMITIDO]");
        log.info("Comprobante para: {}", notification.getRecipient());
        log.info("Detalles: {}", notification.getMessage());
    }
}