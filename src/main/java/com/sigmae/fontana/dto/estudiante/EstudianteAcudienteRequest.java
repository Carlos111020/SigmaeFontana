package com.sigmae.fontana.dto.estudiante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EstudianteAcudienteRequest(
        @NotNull @Positive Long acudienteId,
        @NotBlank @Size(max = 40) String parentesco,
        boolean responsablePrincipal
) {
}
