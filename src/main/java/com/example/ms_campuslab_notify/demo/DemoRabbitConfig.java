package com.example.ms_campuslab_notify.demo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Módulo de laboratorio: aplica las guías de RabbitMQ sin tocar las colas reales de CampusLab.
 *  - Guía 2: DirectExchange + 2 colas + bindings con routing keys (INFO / WARNING / ERROR)
 *  - Guía 3: cola con TTL + DLX/DLQ con retención de 24 h + max-length
 */
@Configuration
public class DemoRabbitConfig {

    // ===== Guía 2: exchanges, bindings y routing keys =====
    public static final String LOGS_EXCHANGE = "demo.logs.exchange";
    public static final String LOGS_ALL_QUEUE = "demo.logs.all";
    public static final String LOGS_ERRORS_QUEUE = "demo.logs.errors";

    @Bean
    public DirectExchange demoLogsExchange() {
        return new DirectExchange(LOGS_EXCHANGE, true, false);
    }

    @Bean
    public Queue demoLogsAllQueue() {
        return QueueBuilder.durable(LOGS_ALL_QUEUE).build();
    }

    @Bean
    public Queue demoLogsErrorsQueue() {
        return QueueBuilder.durable(LOGS_ERRORS_QUEUE).build();
    }

    // all_logs recibe INFO, WARNING y ERROR (3 bindings)
    @Bean
    public Binding demoBindAllInfo() {
        return BindingBuilder.bind(demoLogsAllQueue()).to(demoLogsExchange()).with("INFO");
    }

    @Bean
    public Binding demoBindAllWarning() {
        return BindingBuilder.bind(demoLogsAllQueue()).to(demoLogsExchange()).with("WARNING");
    }

    @Bean
    public Binding demoBindAllError() {
        return BindingBuilder.bind(demoLogsAllQueue()).to(demoLogsExchange()).with("ERROR");
    }

    // errors_only recibe solo ERROR (1 binding)
    @Bean
    public Binding demoBindErrorsOnly() {
        return BindingBuilder.bind(demoLogsErrorsQueue()).to(demoLogsExchange()).with("ERROR");
    }

    // ===== Guía 3: DLX / DLQ, TTL y retención =====
    public static final String ORDERS_EXCHANGE = "demo.orders.exchange";
    public static final String ORDERS_QUEUE = "demo.orders.queue";
    public static final String ORDERS_ROUTING_KEY = "order.created";
    public static final String ORDERS_DLX = "demo.orders.dlx";
    public static final String ORDERS_DLQ = "demo.orders.dlq";

    @Bean
    public DirectExchange demoOrdersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue demoOrdersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE)
                .withArgument("x-message-ttl", 30000)                // expira a los 30 s -> va al DLX
                .withArgument("x-dead-letter-exchange", ORDERS_DLX)  // rechazados/expirados van aquí
                .withArgument("x-max-length", 1000)                  // máx. 1000 mensajes
                .build();
    }

    @Bean
    public Binding demoOrdersBinding() {
        return BindingBuilder.bind(demoOrdersQueue()).to(demoOrdersExchange()).with(ORDERS_ROUTING_KEY);
    }

    @Bean
    public FanoutExchange demoOrdersDlx() {
        return new FanoutExchange(ORDERS_DLX, true, false);
    }

    @Bean
    public Queue demoOrdersDlq() {
        return QueueBuilder.durable(ORDERS_DLQ)
                .withArgument("x-message-ttl", 86_400_000)           // retención de 24 h para auditoría
                .build();
    }

    @Bean
    public Binding demoOrdersDlqBinding() {
        return BindingBuilder.bind(demoOrdersDlq()).to(demoOrdersDlx());
    }
}