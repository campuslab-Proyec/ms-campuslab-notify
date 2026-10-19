package com.example.ms_campuslab_notify.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.ms_campuslab_notify.config.MessagingProperties;

/**
 * Cliente de la API HTTP de management de RabbitMQ. Solo lectura: AmqpAdmin sabe crear y borrar,
 * pero no listar lo que existe en el broker. Devuelve los objetos del vhost "/".
 */
@Component
public class RabbitManagementClient {

    private static final String DEFAULT_VHOST = "/";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new ParameterizedTypeReference<>() {};

    private final RestClient rest;

    public RabbitManagementClient(MessagingProperties props) {
        MessagingProperties.Management m = props.getManagement();
        this.rest = RestClient.builder()
                .baseUrl(m.getUrl())
                .defaultHeaders(h -> h.setBasicAuth(m.getUsername(), m.getPassword()))
                .build();
    }

    public List<Map<String, Object>> queues() {
        return get("/api/queues");
    }

    public List<Map<String, Object>> exchanges() {
        return get("/api/exchanges");
    }

    public List<Map<String, Object>> bindings() {
        return get("/api/bindings");
    }

    /** Nodos del clúster (no pertenecen a un vhost, por eso no se filtran). */
    public List<Map<String, Object>> nodes() {
        List<Map<String, Object>> nodes = rest.get().uri("/api/nodes").retrieve().body(LIST_OF_MAPS);
        return nodes == null ? List.of() : nodes;
    }

    private List<Map<String, Object>> get(String path) {
        List<Map<String, Object>> all = rest.get().uri(path).retrieve().body(LIST_OF_MAPS);
        if (all == null) {
            return List.of();
        }
        return all.stream().filter(o -> DEFAULT_VHOST.equals(o.get("vhost"))).toList();
    }
}
