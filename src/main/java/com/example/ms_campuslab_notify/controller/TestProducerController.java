package com.example.ms_campuslab_notify.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_campuslab_notify.config.MessagingProperties;
import com.example.ms_campuslab_notify.dto.NotificationRequest;

@RestController
@RequestMapping("/api/notify/test")
public class TestProducerController {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public TestProducerController(RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @PostMapping
    public ResponseEntity<String> sendTestNotification(@RequestBody NotificationRequest request) {
        rabbitTemplate.convertAndSend(props.getExchanges().getCommand(),
                props.getQueues().getEmail().getRoutingKey(), request);
        return ResponseEntity.ok("Mensaje enviado exitosamente a la cola de RabbitMQ");
    }
}
