package com.example.ms_campuslab_notify.demo;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DemoConsumers {

    private static final Logger log = LoggerFactory.getLogger(DemoConsumers.class);

    // ===== Guía 2: dos consumidores, uno por cola =====
    @RabbitListener(queues = DemoRabbitConfig.LOGS_ALL_QUEUE)
    public void receiveAllLogs(Map<String, Object> msg) {
        log.info("[MONITOR GENERAL] Log recibido: {}", msg);
    }

    @RabbitListener(queues = DemoRabbitConfig.LOGS_ERRORS_QUEUE)
    public void receiveErrorLogs(Map<String, Object> msg) {
        log.error("!!!!!!!! [ALERTA CRITICA] Error detectado: {}", msg);
    }

    // ===== Guía 3: consumidor que puede fallar =====
    // La excepción SE PROPAGA (no se captura): Spring reintenta 3 veces con backoff
    // y, al agotarse, rechaza el mensaje sin reencolar -> DLX -> demo.orders.dlq
    @RabbitListener(queues = DemoRabbitConfig.ORDERS_QUEUE)
    public void processOrder(Map<String, Object> order) {
        log.info("[PROCESADOR] Recibida orden: {}", order);
        if (Boolean.TRUE.equals(order.get("fail"))) {
            throw new IllegalStateException("Error simulado al procesar la orden " + order.get("orderId"));
        }
        log.info("[EXITO] Orden procesada correctamente: {}", order.get("orderId"));
    }
}