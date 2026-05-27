package com.sigmae.fontana.dto.talanquera;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TalanqueraIngresoRequest(
        @NotBlank @Size(max = 30) String codigoTarjeta,
        @Size(max = 250) String observacion
) {
}
