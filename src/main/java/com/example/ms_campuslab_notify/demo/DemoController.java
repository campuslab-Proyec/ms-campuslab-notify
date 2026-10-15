package com.example.ms_campuslab_notify.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
public class DemoController {

    private static final List<String> MONITORED = List.of(
            DemoRabbitConfig.ORDERS_QUEUE, DemoRabbitConfig.ORDERS_DLQ,
            DemoRabbitConfig.LOGS_ALL_QUEUE, DemoRabbitConfig.LOGS_ERRORS_QUEUE,
            "q.cmd.email.dlq", "q.cmd.prep.dlq", "q.cmd.voucher.dlq", "notify.booking.dlq");

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    public DemoController(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    /** Guía 2: la routing key es el nivel del log (INFO, WARNING, ERROR). */
    @PostMapping("/demo/logs")
    public Map<String, Object> sendLog(@RequestBody Map<String, Object> body) {
        String level = String.valueOf(body.getOrDefault("level", "INFO")).toUpperCase();
        Map<String, Object> payload = new HashMap<>();
        payload.put("level", level);
        payload.put("message", body.getOrDefault("message", "(sin mensaje)"));
        payload.put("ts", Instant.now().toString());
        rabbitTemplate.convertAndSend(DemoRabbitConfig.LOGS_EXCHANGE, level, payload);
        return Map.of("status", "Log enviado", "routingKey", level);
    }

    /** Guía 3: con fail=true el consumidor falla, reintenta y termina en la DLQ. */
    @PostMapping("/demo/orders")
    public Map<String, Object> sendOrder(@RequestBody Map<String, Object> body) {
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", body.getOrDefault("orderId", "ORD-" + System.currentTimeMillis()));
        order.put("customer", body.getOrDefault("customer", "Desconocido"));
        order.put("fail", Boolean.TRUE.equals(body.get("fail")));
        order.put("ts", Instant.now().toString());
        rabbitTemplate.convertAndSend(DemoRabbitConfig.ORDERS_EXCHANGE, DemoRabbitConfig.ORDERS_ROUTING_KEY, order);
        return Map.of("status", "Orden enviada", "orderId", order.get("orderId"));
    }

    /** Estado de las colas y de las DLQ (para el panel de alertas). */
    @GetMapping("/dlq")
    public List<Map<String, Object>> queues() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String name : MONITORED) {
            QueueInformation info = amqpAdmin.getQueueInfo(name);
            Map<String, Object> row = new HashMap<>();
            row.put("queue", name);
            row.put("dlq", name.endsWith(".dlq"));
            row.put("messages", info == null ? 0 : info.getMessageCount());
            row.put("consumers", info == null ? 0 : info.getConsumerCount());
            rows.add(row);
        }
        return rows;
    }
}