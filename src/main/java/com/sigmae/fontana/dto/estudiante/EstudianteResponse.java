package com.sigmae.fontana.dto.estudiante;

import com.sigmae.fontana.entity.enums.EstadoPermanencia;

public record EstudianteResponse(
        Long id,
        String codigoEstudiantil,
        String documento,
        String nombres,
        String apellidos,
        EstadoPermanencia estadoPermanencia,
        boolean activo,
        Long gradoId,
        String grado
) {
}
