package com.example.ms_campuslab_notify.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_campuslab_notify.dto.NotificationRequest;

@RestController
@RequestMapping("/api/notify/test")
public class TestProducerController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${campuslab.rabbitmq.exchange}")
    private String exchange;

    @Value("${campuslab.rabbitmq.routingkey}")
    private String routingKey;

    public TestProducerController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<String> sendTestNotification(@RequestBody NotificationRequest request) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
        return ResponseEntity.ok("Mensaje enviado exitosamente a la cola de RabbitMQ");
    }
}