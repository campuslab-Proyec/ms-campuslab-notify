package com.example.ms_campuslab_notify.demo;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.ms_campuslab_notify.config.RabbitMQConfig;
import com.example.ms_campuslab_notify.service.DemoMessagingService;
import com.rabbitmq.client.Channel;

/**
 * Consumidores de los demos de las guías, con ACK manual como los de dominio.
 * Guía 3 (órdenes): los reintentos son explícitos. Si una orden falla y quedan intentos, se vuelve a
 * publicar con el contador incrementado tras un backoff exponencial (1 s, 2 s, 4 s) y se confirma la
 * original; al agotarse, basicNack sin reencolar -> DLX -> demo.orders.dlq.
 */
@Component
public class DemoConsumers {

    private static final Logger log = LoggerFactory.getLogger(DemoConsumers.class);

    private final DemoMessagingService demoService;

    public DemoConsumers(DemoMessagingService demoService) {
        this.demoService = demoService;
    }

    // ===== Guía 2: dos consumidores, uno por cola =====
    @RabbitListener(queues = "${campuslab.rabbitmq.demo.logs-all-queue}", containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receiveAllLogs(Map<String, Object> msg, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[MONITOR GENERAL] Log recibido: {}", msg);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // Estas colas no tienen DLQ: el mensaje se descarta y queda registrado en el log
            log.error("[MONITOR GENERAL] No se pudo procesar el log {} -> descartado", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = "${campuslab.rabbitmq.demo.logs-errors-queue}", containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void receiveErrorLogs(Map<String, Object> msg, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("!!!!!!!! [ALERTA CRITICA] Error detectado: {}", msg);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[ALERTA CRITICA] No se pudo procesar la alerta {} -> descartada", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    // ===== Guía 3: consumidor que puede fallar, con reintentos explícitos y DLQ =====
    @RabbitListener(queues = "${campuslab.rabbitmq.demo.orders-queue}", containerFactory = RabbitMQConfig.MANUAL_ACK_FACTORY)
    public void processOrder(Map<String, Object> order, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                             @Header(name = DemoMessagingService.RETRY_HEADER, required = false) Integer retries) throws IOException {
        int attempt = retries == null ? 0 : retries;
        try {
            log.info("[PROCESADOR] Recibida orden: {} (intento {})", order, attempt + 1);
            if (Boolean.TRUE.equals(order.get("fail"))) {
                throw new IllegalStateException("Error simulado al procesar la orden " + order.get("orderId"));
            }
            log.info("[EXITO] Orden procesada correctamente: {}", order.get("orderId"));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (attempt < demoService.maxRetries()) {
                // Error recuperable: reintento con backoff exponencial y ACK del mensaje original
                long backoffMs = 1000L << attempt;
                log.warn("[REINTENTO] {} -> reintento {}/{} en {} ms", e.getMessage(), attempt + 1, demoService.maxRetries(), backoffMs);
                sleep(backoffMs);
                demoService.republishOrder(order, attempt + 1);
                channel.basicAck(deliveryTag, false);
            } else {
                // Reintentos agotados: NACK sin reencolar -> DLX -> DLQ
                log.error("[DLQ] Orden {} enviada a la DLQ tras {} reintentos: {}", order.get("orderId"), attempt, e.getMessage());
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
