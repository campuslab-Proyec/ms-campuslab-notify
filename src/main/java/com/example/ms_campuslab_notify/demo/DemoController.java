package com.example.ms_campuslab_notify.demo;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_campuslab_notify.service.DemoMessagingService;

/** Endpoints de los demos de las guías. Solo traduce HTTP; la mensajería está en DemoMessagingService. */
@RestController
@RequestMapping("/api/notify")
public class DemoController {

    private final DemoMessagingService demoService;

    public DemoController(DemoMessagingService demoService) {
        this.demoService = demoService;
    }

    /** Guía 2: la routing key es el nivel del log (INFO, WARNING, ERROR). */
    @PostMapping("/demo/logs")
    public Map<String, Object> sendLog(@RequestBody Map<String, Object> body) {
        String routingKey = demoService.publishLog(String.valueOf(body.getOrDefault("level", "INFO")), body.get("message"));
        return Map.of("status", "Log enviado", "routingKey", routingKey);
    }

    /** Guía 3: con fail=true el consumidor falla, reintenta y termina en la DLQ. */
    @PostMapping("/demo/orders")
    public Map<String, Object> sendOrder(@RequestBody Map<String, Object> body) {
        return Map.of("status", "Orden enviada", "orderId", demoService.publishOrder(body));
    }

    /** Estado de las colas y de las DLQ (para el panel de alertas). */
    @GetMapping("/dlq")
    public List<Map<String, Object>> queues() {
        return demoService.queueStatus();
    }
}
