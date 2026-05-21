package com.sigmae.fontana.dto.novedad;

import com.sigmae.fontana.entity.enums.EstadoNovedad;
import jakarta.validation.constraints.NotNull;

public record EstadoNovedadRequest(
        @NotNull EstadoNovedad estado
) {
}
