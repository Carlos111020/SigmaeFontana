package com.sigmae.fontana.dto.registro;

import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import java.time.LocalDateTime;

public record RegistroAccesoResponse(
        Long id,
        TipoRegistro tipoRegistro,
        LocalDateTime fechaHora,
        String observacion,
        Long estudianteId,
        String estudiante,
        EstadoPermanencia estadoPermanencia,
        Long puntoAccesoId,
        String puntoAcceso,
        Long usuarioResponsableId,
        String usuarioResponsable
) {
}
