package com.sigmae.fontana.dto.grado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GradoRequest(
        @NotBlank @Size(max = 60) String nombre,
        @NotBlank @Size(max = 60) String nivel
) {
}
