package com.sigmae.fontana.dto.acudiente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AcudienteRequest(
        @NotBlank @Size(max = 30) String documento,
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @NotBlank @Size(max = 30) String telefono,
        @NotBlank @Email @Size(max = 120) String correo,
        @Positive Long usuarioId
) {
}
