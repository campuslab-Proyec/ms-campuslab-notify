package com.example.ms_campuslab_notify.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.ms_campuslab_notify.dto.admin.AdminPatterns;
import com.example.ms_campuslab_notify.dto.admin.BindingRequest;
import com.example.ms_campuslab_notify.dto.admin.CreateExchangeRequest;
import com.example.ms_campuslab_notify.dto.admin.CreateQueueRequest;
import com.example.ms_campuslab_notify.dto.admin.QueueInfoResponse;
import com.example.ms_campuslab_notify.exception.InvalidRabbitConfigException;
import com.example.ms_campuslab_notify.exception.ResourceConflictException;
import com.example.ms_campuslab_notify.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Administración de la topología de RabbitMQ en tiempo de ejecución: crear y eliminar colas,
 * exchanges y bindings. Las colas y exchanges que declara la propia aplicación (beans de
 * RabbitMQConfig / DemoRabbitConfig) quedan protegidos y no se pueden borrar desde la API.
 */
@Slf4j
@Service
public class RabbitAdminService {

    private static final Pattern NAME = Pattern.compile(AdminPatterns.NAME);
    private static final String RESERVED_PREFIX = "amq.";

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final Set<String> managedQueues;
    private final Set<String> managedExchanges;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate,
                              List<Queue> declaredQueues, List<Exchange> declaredExchanges) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.managedQueues = declaredQueues.stream().map(Queue::getName).collect(Collectors.toUnmodifiableSet());
        this.managedExchanges = declaredExchanges.stream().map(Exchange::getName).collect(Collectors.toUnmodifiableSet());
    }

    // ================= COLAS =================

    /** Estado de las colas que declara la aplicación. */
    public List<QueueInfoResponse> listManagedQueues() {
        return managedQueues.stream()
                .sorted(Comparator.naturalOrder())
                .map(this::toResponse)
                .toList();
    }

    public QueueInfoResponse getQueue(String name) {
        validateName(name, "la cola");
        return toResponse(name);
    }

    public QueueInfoResponse createQueue(CreateQueueRequest req) {
        validateName(req.name(), "la cola");
        if (req.deadLetterRoutingKey() != null && req.deadLetterExchange() == null) {
            throw new InvalidRabbitConfigException("deadLetterRoutingKey requiere indicar deadLetterExchange");
        }
        if (req.deadLetterExchange() != null && !exchangeExists(req.deadLetterExchange())) {
            throw new InvalidRabbitConfigException("El deadLetterExchange '" + req.deadLetterExchange() + "' no existe");
        }
        if (queueInfo(req.name()) != null) {
            throw new ResourceConflictException("La cola '" + req.name() + "' ya existe");
        }

        QueueBuilder builder = Boolean.FALSE.equals(req.durable())
                ? QueueBuilder.nonDurable(req.name())
                : QueueBuilder.durable(req.name());
        if (Boolean.TRUE.equals(req.autoDelete())) {
            builder.autoDelete();
        }
        if (req.deadLetterExchange() != null) {
            builder.deadLetterExchange(req.deadLetterExchange());
        }
        if (req.deadLetterRoutingKey() != null) {
            builder.deadLetterRoutingKey(req.deadLetterRoutingKey());
        }
        if (req.messageTtl() != null) {
            builder.ttl(req.messageTtl());
        }
        if (req.maxLength() != null) {
            builder.maxLength(req.maxLength());
        }

        amqpAdmin.declareQueue(builder.build());
        log.info("[ADMIN] Cola creada: {}", req.name());
        return toResponse(req.name());
    }

    /**
     * Elimina una cola. Con ifUnused / ifEmpty RabbitMQ se niega si la cola tiene consumidores
     * o mensajes, y se responde 409.
     */
    public void deleteQueue(String name, boolean ifUnused, boolean ifEmpty) {
        validateName(name, "la cola");
        if (managedQueues.contains(name)) {
            throw new ResourceConflictException("La cola '" + name + "' la declara la aplicación y no se puede eliminar");
        }
        QueueInformation info = requireQueue(name);
        if (ifUnused && info.getConsumerCount() > 0) {
            throw new ResourceConflictException("La cola '" + name + "' tiene " + info.getConsumerCount() + " consumidor(es)");
        }
        if (ifEmpty && info.getMessageCount() > 0) {
            throw new ResourceConflictException("La cola '" + name + "' tiene " + info.getMessageCount() + " mensaje(s)");
        }
        try {
            amqpAdmin.deleteQueue(name, ifUnused, ifEmpty);
        } catch (AmqpConnectException e) {
            throw e;
        } catch (AmqpException e) {
            // Cambió entre la comprobación y el borrado (llegó un consumidor o un mensaje)
            throw new ResourceConflictException("RabbitMQ rechazó eliminar la cola '" + name + "': " + e.getMessage());
        }
        log.info("[ADMIN] Cola eliminada: {}", name);
    }

    /** Vacía una cola sin eliminarla. Devuelve cuántos mensajes se descartaron. */
    public long purgeQueue(String name) {
        validateName(name, "la cola");
        requireQueue(name);
        int purged = amqpAdmin.purgeQueue(name);
        log.info("[ADMIN] Cola vaciada: {} ({} mensajes)", name, purged);
        return purged;
    }

    // ================= EXCHANGES =================

    public void createExchange(CreateExchangeRequest req) {
        validateName(req.name(), "el exchange");
        if (exchangeExists(req.name())) {
            throw new ResourceConflictException("El exchange '" + req.name() + "' ya existe");
        }
        ExchangeBuilder builder = new ExchangeBuilder(req.name(), req.type())
                .durable(!Boolean.FALSE.equals(req.durable()));
        if (Boolean.TRUE.equals(req.autoDelete())) {
            builder.autoDelete();
        }
        amqpAdmin.declareExchange(builder.build());
        log.info("[ADMIN] Exchange creado: {} ({})", req.name(), req.type());
    }

    public void deleteExchange(String name) {
        validateName(name, "el exchange");
        if (managedExchanges.contains(name)) {
            throw new ResourceConflictException("El exchange '" + name + "' lo declara la aplicación y no se puede eliminar");
        }
        if (!exchangeExists(name)) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe");
        }
        amqpAdmin.deleteExchange(name);
        log.info("[ADMIN] Exchange eliminado: {}", name);
    }

    // ================= BINDINGS =================

    public void createBinding(BindingRequest req) {
        Binding binding = toBinding(req);
        amqpAdmin.declareBinding(binding);
        log.info("[ADMIN] Binding creado: {} -> {} ({})", req.exchange(), req.queue(), binding.getRoutingKey());
    }

    public void deleteBinding(BindingRequest req) {
        Binding binding = toBinding(req);
        amqpAdmin.removeBinding(binding);
        log.info("[ADMIN] Binding eliminado: {} -> {} ({})", req.exchange(), req.queue(), binding.getRoutingKey());
    }

    // ================= APOYO =================

    private Binding toBinding(BindingRequest req) {
        validateName(req.queue(), "la cola");
        validateName(req.exchange(), "el exchange");
        requireQueue(req.queue());
        if (!exchangeExists(req.exchange())) {
            throw new ResourceNotFoundException("El exchange '" + req.exchange() + "' no existe");
        }
        String routingKey = req.routingKey() == null ? "" : req.routingKey();
        return new Binding(req.queue(), Binding.DestinationType.QUEUE, req.exchange(), routingKey, null);
    }

    /** Validación también para los nombres que llegan por la URL (no pasan por un DTO). */
    private void validateName(String name, String kind) {
        if (name == null || name.isBlank()) {
            throw new InvalidRabbitConfigException("El nombre de " + kind + " no puede estar vacío");
        }
        if (name.length() > 255) {
            throw new InvalidRabbitConfigException("El nombre de " + kind + " no puede superar 255 caracteres");
        }
        if (!NAME.matcher(name).matches()) {
            throw new InvalidRabbitConfigException("Nombre inválido para " + kind + ": " + AdminPatterns.NAME_MESSAGE);
        }
        if (name.startsWith(RESERVED_PREFIX)) {
            throw new InvalidRabbitConfigException("Los nombres que empiezan con 'amq.' están reservados por RabbitMQ");
        }
    }

    private QueueInformation queueInfo(String name) {
        return amqpAdmin.getQueueInfo(name);
    }

    private QueueInformation requireQueue(String name) {
        QueueInformation info = queueInfo(name);
        if (info == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        return info;
    }

    /** Declaración pasiva: RabbitMQ responde con error si el exchange no existe. */
    private boolean exchangeExists(String name) {
        try {
            rabbitTemplate.execute(channel -> channel.exchangeDeclarePassive(name));
            return true;
        } catch (AmqpConnectException e) {
            throw e;
        } catch (AmqpException e) {
            return false;
        }
    }

    private QueueInfoResponse toResponse(String name) {
        QueueInformation info = requireQueue(name);
        return new QueueInfoResponse(name, info.getMessageCount(), info.getConsumerCount(), managedQueues.contains(name));
    }
}
