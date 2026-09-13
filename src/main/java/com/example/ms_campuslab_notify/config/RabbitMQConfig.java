package com.example.ms_campuslab_notify.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DIRECT_EXCHANGE = "cmd.direct";
    public static final String DLX_EXCHANGE = "cmd.dead.dlx";

    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_PREP = "q.cmd.prep";
    public static final String QUEUE_VOUCHER = "q.cmd.voucher";

    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_PREP_DLQ = "q.cmd.prep.dlq";
    public static final String QUEUE_VOUCHER_DLQ = "q.cmd.voucher.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // --- COLA EMAIL Y SU DLQ ---
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDlqQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(directExchange()).with("email.send");
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlqQueue()).to(deadLetterExchange()).with(QUEUE_EMAIL_DLQ);
    }

    // --- COLA PREPARACIÓN Y SU DLQ ---
    @Bean
    public Queue prepQueue() {
        return QueueBuilder.durable(QUEUE_PREP)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_PREP_DLQ)
                .build();
    }

    @Bean
    public Queue prepDlqQueue() {
        return QueueBuilder.durable(QUEUE_PREP_DLQ).build();
    }

    @Bean
    public Binding prepBinding() {
        return BindingBuilder.bind(prepQueue()).to(directExchange()).with("prep.ticket");
    }

    @Bean
    public Binding prepDlqBinding() {
        return BindingBuilder.bind(prepDlqQueue()).to(deadLetterExchange()).with(QUEUE_PREP_DLQ);
    }

    // --- COLA VOUCHER Y SU DLQ ---
    @Bean
    public Queue voucherQueue() {
        return QueueBuilder.durable(QUEUE_VOUCHER)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_VOUCHER_DLQ)
                .build();
    }

    @Bean
    public Queue voucherDlqQueue() {
        return QueueBuilder.durable(QUEUE_VOUCHER_DLQ).build();
    }

    @Bean
    public Binding voucherBinding() {
        return BindingBuilder.bind(voucherQueue()).to(directExchange()).with("voucher.gen");
    }

    @Bean
    public Binding voucherDlqBinding() {
        return BindingBuilder.bind(voucherDlqQueue()).to(deadLetterExchange()).with(QUEUE_VOUCHER_DLQ);
    }
}
