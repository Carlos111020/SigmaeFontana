package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.estudiante.EstudianteResponse;
import com.sigmae.fontana.entity.Estudiante;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EstudianteMapper {

    default EstudianteResponse toResponse(Estudiante estudiante) {
        return new EstudianteResponse(
                estudiante.getId(),
                estudiante.getCodigoEstudiantil(),
                estudiante.getDocumento(),
                estudiante.getNombres(),
                estudiante.getApellidos(),
                estudiante.getEstadoPermanencia(),
                estudiante.isActivo(),
                estudiante.getGrado().getId(),
                estudiante.getGrado().getNombre()
        );
    }
}
