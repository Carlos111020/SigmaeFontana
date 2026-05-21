package com.sigmae.fontana.dto.estudiante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EstudianteRequest(
        @NotBlank @Size(max = 30) String codigoEstudiantil,
        @NotBlank @Size(max = 30) String documento,
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @NotNull @Positive Long gradoId
) {
}
