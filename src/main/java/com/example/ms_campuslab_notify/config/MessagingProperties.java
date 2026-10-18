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

    @Data
    public static class Exchanges {
        private String command;
        private String deadLetter;
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
