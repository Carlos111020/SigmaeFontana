package com.sigmae.fontana.dto.puntoacceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PuntoAccesoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 120) String ubicacion
) {
}
