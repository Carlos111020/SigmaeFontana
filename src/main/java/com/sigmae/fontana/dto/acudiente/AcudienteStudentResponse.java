package com.sigmae.fontana.dto.acudiente;

import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import java.time.LocalDateTime;

public record AcudienteStudentResponse(
        Long estudianteId,
        String codigoEstudiantil,
        String estudiante,
        String grado,
        EstadoPermanencia estadoPermanencia,
        TipoRegistro ultimoTipoRegistro,
        LocalDateTime ultimaFechaHora
) {
}
