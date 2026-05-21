package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.registro.RegistroAccesoResponse;
import com.sigmae.fontana.entity.RegistroAcceso;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegistroAccesoMapper {

    default RegistroAccesoResponse toResponse(RegistroAcceso registro) {
        var estudiante = registro.getEstudiante();
        var responsable = registro.getUsuarioResponsable();
        return new RegistroAccesoResponse(
                registro.getId(),
                registro.getTipoRegistro(),
                registro.getFechaHora(),
                registro.getObservacion(),
                estudiante.getId(),
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                estudiante.getEstadoPermanencia(),
                registro.getPuntoAcceso().getId(),
                registro.getPuntoAcceso().getNombre(),
                responsable.getId(),
                responsable.getNombres() + " " + responsable.getApellidos()
        );
    }
}
