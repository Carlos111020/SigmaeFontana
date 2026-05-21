package com.sigmae.fontana.dto.novedad;

import com.sigmae.fontana.entity.enums.TipoNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NovedadRequest(
        @NotNull TipoNovedad tipoNovedad,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @Positive Long estudianteId
) {
}
