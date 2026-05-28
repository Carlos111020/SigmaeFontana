package com.sigmae.fontana.dto.estudiante;

import com.sigmae.fontana.dto.acudiente.AcudienteResponse;

public record EstudianteAcudienteResponse(
        Long estudianteId,
        Long acudienteId,
        String parentesco,
        boolean responsablePrincipal,
        AcudienteResponse acudiente
) {
}
