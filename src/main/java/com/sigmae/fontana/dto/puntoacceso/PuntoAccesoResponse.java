package com.sigmae.fontana.dto.puntoacceso;

public record PuntoAccesoResponse(
        Long id,
        String nombre,
        String ubicacion,
        boolean activo
) {
}
