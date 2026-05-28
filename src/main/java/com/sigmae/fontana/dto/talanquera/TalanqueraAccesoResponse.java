package com.sigmae.fontana.dto.talanquera;

import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import java.time.LocalDateTime;
import java.util.List;

public record TalanqueraAccesoResponse(
        Long registroId,
        TipoRegistro operacion,
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
