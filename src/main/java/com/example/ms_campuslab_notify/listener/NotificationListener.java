package com.example.ms_campuslab_notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.config.RabbitMQConfig;
import com.example.ms_campuslab_notify.dto.NotificationMessageDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void handleEmailNotification(NotificationMessageDTO message) {
        log.info("[EMAIL RECEIVED] Enviando correo a {}: {}", message.getRecipient(), message.getPayload());
        // Lógica de simulación de envío de correo
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PREP)
    public void handlePreparationTicket(NotificationMessageDTO message) {
        log.info("[PREPARATION TICKET] Generando ticket de preparación para operador. EventID: {}", message.getEventId());
        // Lógica de asignación a técnico
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VOUCHER)
    public void handleVoucherGeneration(NotificationMessageDTO message) {
        log.info("[VOUCHER GENERATED] Emitiendo comprobante/voucher para reserva. CorrelationID: {}", message.getCorrelationId());
        // Lógica de emisión de voucher
    }
}