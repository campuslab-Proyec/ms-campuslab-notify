package com.example.ms_campuslab_notify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_campuslab_notify.dto.NotificationRequest;
import com.example.ms_campuslab_notify.service.NotificationPublisher;

@RestController
@RequestMapping("/api/notify/test")
public class TestProducerController {

    private final NotificationPublisher publisher;

    public TestProducerController(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<String> sendTestNotification(@RequestBody NotificationRequest request) {
        publisher.publishEmail(request);
        return ResponseEntity.ok("Mensaje enviado exitosamente a la cola de RabbitMQ");
    }
}
