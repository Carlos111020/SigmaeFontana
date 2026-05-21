package com.sigmae.fontana.dto.notificacion;

import com.sigmae.fontana.entity.enums.TipoNotificacion;
import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        TipoNotificacion tipoNotificacion,
        String mensaje,
        LocalDateTime fechaHora,
        boolean leida,
        Long estudianteId,
        String estudiante
) {
}
