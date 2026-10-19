package com.example.ms_campuslab_notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Nombres de exchanges, colas y routing keys definidos en application.yaml (campuslab.rabbitmq.*).
 * Es la única fuente de estos nombres dentro del microservicio.
 */
@Data
@ConfigurationProperties(prefix = "campuslab.rabbitmq")
public class MessagingProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private Management management = new Management();
    private Demo demo = new Demo();

    /** Topología de los demos de las guías 2 (routing keys) y 3 (TTL, DLX/DLQ, reintentos). */
    @Data
    public static class Demo {
        private String logsExchange;
        private String logsAllQueue;
        private String logsErrorsQueue;
        private String ordersExchange;
        private String ordersQueue;
        private String ordersRoutingKey;
        private String ordersDlx;
        private String ordersDlq;
        private int ordersTtlMs;
        private int ordersMaxLength;
        private int ordersDlqRetentionMs;
        /** Reintentos manuales antes de mandar una orden fallida a la DLQ (backoff 1 s, 2 s, 4 s…). */
        private int ordersMaxRetries;
    }

    /** API HTTP de management de RabbitMQ, usada para listar colas, exchanges y bindings. */
    @Data
    public static class Management {
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class Exchanges {
        private String command;
        private String deadLetter;
        /** Topic exchange de eventos de reservas (routing key booking.<estado>). */
        private String bookingEvents;
    }

    @Data
    public static class Queues {
        private QueueDefinition email = new QueueDefinition();
        private QueueDefinition prep = new QueueDefinition();
        private QueueDefinition voucher = new QueueDefinition();
        private QueueDefinition booking = new QueueDefinition();
    }

    /** Una cola de trabajo, la routing key con la que se enlaza y su DLQ. */
    @Data
    public static class QueueDefinition {
        private String name;
        private String routingKey;
        private String dlq;
    }
}
