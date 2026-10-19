package com.example.ms_campuslab_notify.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.ms_campuslab_notify.config.MessagingProperties;
import com.example.ms_campuslab_notify.dto.admin.QueueInfoResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Lógica de los demos de las guías: publicar logs y órdenes, reintentar órdenes fallidas y
 * consultar el estado de las colas. El controller y el consumidor de demo solo llaman a estos métodos.
 */
@Slf4j
@Service
public class DemoMessagingService {

    /** Header con el número de reintentos de una orden (lo incrementa el consumidor en cada fallo). */
    public static final String RETRY_HEADER = "x-retry-count";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdminService adminService;
    private final MessagingProperties props;

    public DemoMessagingService(RabbitTemplate rabbitTemplate, RabbitAdminService adminService, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.adminService = adminService;
        this.props = props;
    }

    /** Guía 2: la routing key es el nivel del log (INFO, WARNING, ERROR). Devuelve la routing key usada. */
    public String publishLog(String level, Object message) {
        String routingKey = level == null ? "INFO" : level.toUpperCase();
        Map<String, Object> payload = new HashMap<>();
        payload.put("level", routingKey);
        payload.put("message", message == null ? "(sin mensaje)" : message);
        payload.put("ts", Instant.now().toString());
        rabbitTemplate.convertAndSend(props.getDemo().getLogsExchange(), routingKey, payload);
        return routingKey;
    }

    /** Guía 3: publica una orden; con fail=true el consumidor la reintenta y termina en la DLQ. */
    public Object publishOrder(Map<String, Object> body) {
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", body.getOrDefault("orderId", "ORD-" + System.currentTimeMillis()));
        order.put("customer", body.getOrDefault("customer", "Desconocido"));
        order.put("fail", Boolean.TRUE.equals(body.get("fail")));
        order.put("ts", Instant.now().toString());
        rabbitTemplate.convertAndSend(props.getDemo().getOrdersExchange(), props.getDemo().getOrdersRoutingKey(), order);
        return order.get("orderId");
    }

    /** Vuelve a publicar una orden fallida con el contador de reintentos actualizado. */
    public void republishOrder(Map<String, Object> order, int attempt) {
        rabbitTemplate.convertAndSend(props.getDemo().getOrdersExchange(), props.getDemo().getOrdersRoutingKey(), order, message -> {
            message.getMessageProperties().setHeader(RETRY_HEADER, attempt);
            return message;
        });
    }

    public int maxRetries() {
        return props.getDemo().getOrdersMaxRetries();
    }

    /** Estado de las colas de demo y de todas las DLQ (panel "Alertas y guías"). */
    public List<Map<String, Object>> queueStatus() {
        MessagingProperties.Queues q = props.getQueues();
        MessagingProperties.Demo d = props.getDemo();
        return Stream.of(d.getOrdersQueue(), d.getOrdersDlq(), d.getLogsAllQueue(), d.getLogsErrorsQueue(),
                        q.getEmail().getDlq(), q.getPrep().getDlq(), q.getVoucher().getDlq(), q.getBooking().getDlq())
                .map(name -> {
                    QueueInfoResponse info = adminService.findQueue(name).orElse(null);
                    Map<String, Object> row = new HashMap<>();
                    row.put("queue", name);
                    row.put("dlq", name.endsWith(".dlq"));
                    row.put("messages", info == null ? 0 : info.messageCount());
                    row.put("consumers", info == null ? 0 : info.consumerCount());
                    return row;
                })
                .toList();
    }
}
