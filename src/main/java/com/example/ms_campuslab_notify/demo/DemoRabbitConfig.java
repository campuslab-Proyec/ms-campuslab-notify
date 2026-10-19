package com.example.ms_campuslab_notify.demo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ms_campuslab_notify.config.MessagingProperties;

/**
 * Módulo de laboratorio: aplica las guías de RabbitMQ sin tocar las colas reales de CampusLab.
 *  - Guía 2: DirectExchange + 2 colas + bindings con routing keys (INFO / WARNING / ERROR)
 *  - Guía 3: cola con TTL + DLX/DLQ con retención de 24 h + max-length
 * Los nombres vienen de application.yaml (campuslab.rabbitmq.demo.*).
 */
@Configuration
public class DemoRabbitConfig {

    private final MessagingProperties.Demo demo;

    public DemoRabbitConfig(MessagingProperties props) {
        this.demo = props.getDemo();
    }

    // ===== Guía 2: exchanges, bindings y routing keys =====
    @Bean
    public DirectExchange demoLogsExchange() {
        return new DirectExchange(demo.getLogsExchange(), true, false);
    }

    @Bean
    public Queue demoLogsAllQueue() {
        return QueueBuilder.durable(demo.getLogsAllQueue()).build();
    }

    @Bean
    public Queue demoLogsErrorsQueue() {
        return QueueBuilder.durable(demo.getLogsErrorsQueue()).build();
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
    @Bean
    public DirectExchange demoOrdersExchange() {
        return new DirectExchange(demo.getOrdersExchange(), true, false);
    }

    @Bean
    public Queue demoOrdersQueue() {
        return QueueBuilder.durable(demo.getOrdersQueue())
                .withArgument("x-message-ttl", demo.getOrdersTtlMs())          // expira -> va al DLX
                .withArgument("x-dead-letter-exchange", demo.getOrdersDlx())  // rechazados/expirados van aquí
                .withArgument("x-max-length", demo.getOrdersMaxLength())
                .build();
    }

    @Bean
    public Binding demoOrdersBinding() {
        return BindingBuilder.bind(demoOrdersQueue()).to(demoOrdersExchange()).with(demo.getOrdersRoutingKey());
    }

    @Bean
    public FanoutExchange demoOrdersDlx() {
        return new FanoutExchange(demo.getOrdersDlx(), true, false);
    }

    @Bean
    public Queue demoOrdersDlq() {
        return QueueBuilder.durable(demo.getOrdersDlq())
                .withArgument("x-message-ttl", demo.getOrdersDlqRetentionMs()) // retención para auditoría
                .build();
    }

    @Bean
    public Binding demoOrdersDlqBinding() {
        return BindingBuilder.bind(demoOrdersDlq()).to(demoOrdersDlx());
    }
}
