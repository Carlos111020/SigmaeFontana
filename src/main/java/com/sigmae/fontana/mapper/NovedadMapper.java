package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.novedad.NovedadResponse;
import com.sigmae.fontana.entity.Novedad;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NovedadMapper {

    default NovedadResponse toResponse(Novedad novedad) {
        var estudiante = novedad.getEstudiante();
        var responsable = novedad.getUsuarioResponsable();
        return new NovedadResponse(
                novedad.getId(),
                novedad.getTipoNovedad(),
                novedad.getDescripcion(),
                novedad.getFechaHora(),
                novedad.getEstado(),
                estudiante.getId(),
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                responsable.getId(),
                responsable.getNombres() + " " + responsable.getApellidos()
        );
    }
}
