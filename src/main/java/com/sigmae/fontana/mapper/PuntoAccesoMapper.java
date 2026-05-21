package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoResponse;
import com.sigmae.fontana.entity.PuntoAcceso;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PuntoAccesoMapper {

    default PuntoAccesoResponse toResponse(PuntoAcceso puntoAcceso) {
        return new PuntoAccesoResponse(
                puntoAcceso.getId(),
                puntoAcceso.getNombre(),
                puntoAcceso.getUbicacion(),
                puntoAcceso.isActivo()
        );
    }
}
