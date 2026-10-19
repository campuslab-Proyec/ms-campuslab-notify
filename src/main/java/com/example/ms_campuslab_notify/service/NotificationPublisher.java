package com.example.ms_campuslab_notify.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.ms_campuslab_notify.config.MessagingProperties;
import com.example.ms_campuslab_notify.dto.NotificationRequest;

import lombok.extern.slf4j.Slf4j;

/** Productor de notificaciones por correo (exchange de comandos -> q.cmd.email). */
@Slf4j
@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public NotificationPublisher(RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publishEmail(NotificationRequest request) {
        rabbitTemplate.convertAndSend(props.getExchanges().getCommand(),
                props.getQueues().getEmail().getRoutingKey(), request);
        log.info("Correo encolado para {}", request.getRecipient());
    }
}
