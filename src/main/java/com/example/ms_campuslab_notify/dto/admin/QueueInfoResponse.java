package com.example.ms_campuslab_notify.dto.admin;

/** Estado de una cola. "managed" indica que la declara la aplicación y no se puede borrar por la API. */
public record QueueInfoResponse(String name, long messageCount, int consumerCount, boolean managed) {
}
