package com.sigmae.fontana.dto.grado;

public record GradoResponse(
        Long id,
        String nombre,
        String nivel,
        boolean activo
) {
}
