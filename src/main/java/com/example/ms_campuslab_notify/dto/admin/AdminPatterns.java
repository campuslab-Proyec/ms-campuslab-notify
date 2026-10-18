package com.example.ms_campuslab_notify.dto.admin;

/** Expresiones regulares compartidas por los DTOs y el servicio de administración. */
public final class AdminPatterns {

    /** Nombres de colas y exchanges: letras, números, punto, guion, guion bajo y dos puntos. */
    public static final String NAME = "^[A-Za-z0-9._:-]*$"; // vacío lo cubre @NotBlank
    public static final String NAME_MESSAGE = "solo se permiten letras, números y los caracteres . _ : -";

    /** Routing keys: como NAME, más los comodines * y # de los topic exchanges. Puede ir vacía. */
    public static final String ROUTING_KEY = "^[A-Za-z0-9._:*#-]*$";
    public static final String ROUTING_KEY_MESSAGE = "solo se permiten letras, números y los caracteres . _ : - * #";

    public static final String EXCHANGE_TYPE = "^(direct|topic|fanout|headers)$";

    private AdminPatterns() {
    }
}
