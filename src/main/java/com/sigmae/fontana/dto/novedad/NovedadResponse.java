package com.sigmae.fontana.dto.novedad;

import com.sigmae.fontana.entity.enums.EstadoNovedad;
import com.sigmae.fontana.entity.enums.TipoNovedad;
import java.time.LocalDateTime;

public record NovedadResponse(
        Long id,
        TipoNovedad tipoNovedad,
        String descripcion,
        LocalDateTime fechaHora,
        EstadoNovedad estado,
        Long estudianteId,
        String estudiante,
        Long usuarioResponsableId,
        String usuarioResponsable
) {
}
