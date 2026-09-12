package com.example.ms_campuslab_notify.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.dto.NotificationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationConsumer {

    @RabbitListener(queues = "${campuslab.rabbitmq.queue}")
    public void receiveNotification(NotificationRequest notification) {
        log.info("📩 [NUEVA NOTIFICACIÓN RECIBIDA DESDE RABBITMQ]");
        log.info("Para: {}", notification.getRecipient());
        log.info("Asunto: {}", notification.getSubject());
        log.info("Mensaje: {}", notification.getMessage());
        log.info("Tipo de Evento: {}", notification.getType());
        
        // En esta sección se invoca el servicio de correo (JavaMailSender) o SMS
    }
}