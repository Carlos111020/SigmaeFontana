package com.sigmae.fontana.dto.registro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroAccesoRequest(
        @NotBlank @Size(max = 30) String identificadorEstudiante,
        @NotNull @Positive Long puntoAccesoId,
        @Size(max = 250) String observacion,
        boolean crearNovedadSalidaAnticipada
) {
}
