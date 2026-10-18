package com.example.ms_campuslab_notify.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ms_campuslab_notify.config.MessagingProperties.QueueDefinition;

/**
 * Topología de RabbitMQ del dominio: exchange de comandos, DLX y, por cada caso de uso,
 * su cola, su DLQ y los bindings. Los nombres vienen de application.yaml (MessagingProperties).
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    /** Factory de los consumidores de dominio: ACK manual, sin reintentos automáticos. */
    public static final String MANUAL_ACK_FACTORY = "manualAckContainerFactory";

    private final MessagingProperties props;

    public RabbitMQConfig(MessagingProperties props) {
        this.props = props;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Los consumidores de dominio confirman cada mensaje con basicAck / basicNack.
     * Sin advice de reintentos: el propio consumidor decide si reencolar o mandar a la DLQ.
     * Los mensajes que no se pueden convertir (JSON inválido) se rechazan sin reencolar -> DLQ.
     */
    @Bean(name = MANUAL_ACK_FACTORY)
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(ConnectionFactory connectionFactory,
                                                                          MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // --- EXCHANGES ---
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(props.getExchanges().getCommand());
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.getExchanges().getDeadLetter());
    }

    // --- COLA EMAIL Y SU DLQ ---
    @Bean
    public Queue emailQueue() {
        return workQueue(props.getQueues().getEmail());
    }

    @Bean
    public Queue emailDlqQueue() {
        return QueueBuilder.durable(props.getQueues().getEmail().getDlq()).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(directExchange())
                .with(props.getQueues().getEmail().getRoutingKey());
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlqQueue()).to(deadLetterExchange())
                .with(props.getQueues().getEmail().getDlq());
    }

    // --- COLA PREPARACIÓN Y SU DLQ ---
    @Bean
    public Queue prepQueue() {
        return workQueue(props.getQueues().getPrep());
    }

    @Bean
    public Queue prepDlqQueue() {
        return QueueBuilder.durable(props.getQueues().getPrep().getDlq()).build();
    }

    @Bean
    public Binding prepBinding() {
        return BindingBuilder.bind(prepQueue()).to(directExchange())
                .with(props.getQueues().getPrep().getRoutingKey());
    }

    @Bean
    public Binding prepDlqBinding() {
        return BindingBuilder.bind(prepDlqQueue()).to(deadLetterExchange())
                .with(props.getQueues().getPrep().getDlq());
    }

    // --- COLA VOUCHER Y SU DLQ ---
    @Bean
    public Queue voucherQueue() {
        return workQueue(props.getQueues().getVoucher());
    }

    @Bean
    public Queue voucherDlqQueue() {
        return QueueBuilder.durable(props.getQueues().getVoucher().getDlq()).build();
    }

    @Bean
    public Binding voucherBinding() {
        return BindingBuilder.bind(voucherQueue()).to(directExchange())
                .with(props.getQueues().getVoucher().getRoutingKey());
    }

    @Bean
    public Binding voucherDlqBinding() {
        return BindingBuilder.bind(voucherDlqQueue()).to(deadLetterExchange())
                .with(props.getQueues().getVoucher().getDlq());
    }

    // --- COLA EVENTOS DE RESERVAS Y SU DLQ ---
    @Bean
    public Queue bookingQueue() {
        return workQueue(props.getQueues().getBooking());
    }

    @Bean
    public Queue bookingDlqQueue() {
        return QueueBuilder.durable(props.getQueues().getBooking().getDlq()).build();
    }

    @Bean
    public Binding bookingBinding() {
        return BindingBuilder.bind(bookingQueue()).to(directExchange())
                .with(props.getQueues().getBooking().getRoutingKey());
    }

    @Bean
    public Binding bookingDlqBinding() {
        return BindingBuilder.bind(bookingDlqQueue()).to(deadLetterExchange())
                .with(props.getQueues().getBooking().getDlq());
    }

    /** Cola durable cuyos mensajes rechazados van al DLX con la DLQ como routing key. */
    private Queue workQueue(QueueDefinition def) {
        return QueueBuilder.durable(def.getName())
                .withArgument("x-dead-letter-exchange", props.getExchanges().getDeadLetter())
                .withArgument("x-dead-letter-routing-key", def.getDlq())
                .build();
    }
}
