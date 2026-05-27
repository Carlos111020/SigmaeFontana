package com.sigmae.fontana.dto.talanquera;

import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import java.time.LocalDateTime;
import java.util.List;

public record TalanqueraIngresoResponse(
        Long registroId,
        LocalDateTime fechaHora,
        String codigoTarjeta,
        Long estudianteId,
        String estudiante,
        String grado,
        EstadoPermanencia estadoPermanencia,
        String puntoAcceso,
        String mensajeCorreo,
        List<TalanqueraAcudienteResponse> acudientesNotificados
) {
}
