package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.notificacion.NotificacionResponse;
import com.sigmae.fontana.entity.Notificacion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    default NotificacionResponse toResponse(Notificacion notificacion) {
        var estudiante = notificacion.getEstudiante();
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTipoNotificacion(),
                notificacion.getMensaje(),
                notificacion.getFechaHora(),
                notificacion.isLeida(),
                estudiante.getId(),
                estudiante.getNombres() + " " + estudiante.getApellidos()
        );
    }
}
