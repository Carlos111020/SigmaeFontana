package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.grado.GradoResponse;
import com.sigmae.fontana.entity.Grado;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradoMapper {

    default GradoResponse toResponse(Grado grado) {
        return new GradoResponse(
                grado.getId(),
                grado.getNombre(),
                grado.getNivel(),
                grado.isActivo()
        );
    }
}
