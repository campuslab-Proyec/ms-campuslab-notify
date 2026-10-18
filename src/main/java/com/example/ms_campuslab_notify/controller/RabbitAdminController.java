package com.example.ms_campuslab_notify.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_campuslab_notify.dto.admin.BindingRequest;
import com.example.ms_campuslab_notify.dto.admin.CreateExchangeRequest;
import com.example.ms_campuslab_notify.dto.admin.CreateQueueRequest;
import com.example.ms_campuslab_notify.dto.admin.BindingSummary;
import com.example.ms_campuslab_notify.dto.admin.ExchangeSummary;
import com.example.ms_campuslab_notify.dto.admin.PublishMessageRequest;
import com.example.ms_campuslab_notify.dto.admin.QueueInfoResponse;
import com.example.ms_campuslab_notify.dto.admin.QueueSummary;
import com.example.ms_campuslab_notify.service.RabbitAdminService;

import jakarta.validation.Valid;

/**
 * API de administración de RabbitMQ. Solo traduce HTTP <-> servicio;
 * la lógica y las reglas están en RabbitAdminService.
 */
@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

    private final RabbitAdminService adminService;

    public RabbitAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // ================= COLAS =================

    @GetMapping("/queues")
    public List<QueueSummary> listQueues() {
        return adminService.listQueues();
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse getQueue(@PathVariable String name) {
        return adminService.getQueue(name);
    }

    @PostMapping("/queues")
    public ResponseEntity<QueueInfoResponse> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createQueue(request));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name,
                                            @RequestParam(defaultValue = "false") boolean ifUnused,
                                            @RequestParam(defaultValue = "false") boolean ifEmpty) {
        adminService.deleteQueue(name, ifUnused, ifEmpty);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/queues/{name}/messages")
    public Map<String, Object> purgeQueue(@PathVariable String name) {
        return Map.of("queue", name, "purged", adminService.purgeQueue(name));
    }

    // ================= EXCHANGES =================

    @GetMapping("/exchanges")
    public List<ExchangeSummary> listExchanges() {
        return adminService.listExchanges();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        adminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("exchange", request.name(), "type", request.type()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        adminService.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // ================= BINDINGS =================

    @GetMapping("/bindings")
    public List<BindingSummary> listBindings() {
        return adminService.listBindings();
    }

    @PostMapping("/bindings")
    public ResponseEntity<BindingRequest> createBinding(@Valid @RequestBody BindingRequest request) {
        adminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest request) {
        adminService.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }

    // ================= MENSAJES =================

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> publish(@Valid @RequestBody PublishMessageRequest request) {
        adminService.publish(request);
        return ResponseEntity.accepted().body(Map.of(
                "exchange", request.exchange(),
                "routingKey", request.routingKey() == null ? "" : request.routingKey(),
                "published", true));
    }
}
