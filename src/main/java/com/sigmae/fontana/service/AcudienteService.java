package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.acudiente.AcudienteStudentResponse;
import com.sigmae.fontana.dto.acudiente.AcudienteRequest;
import com.sigmae.fontana.dto.acudiente.AcudienteResponse;
import com.sigmae.fontana.dto.notificacion.NotificacionResponse;
import java.util.List;

public interface AcudienteService {

    AcudienteResponse crear(AcudienteRequest request);

    AcudienteResponse obtener(Long id);

    List<AcudienteResponse> listar(Boolean activo);

    AcudienteResponse actualizar(Long id, AcudienteRequest request);

    void desactivar(Long id);

    List<AcudienteStudentResponse> misEstudiantes(String correoUsuario);

    List<NotificacionResponse> misNotificaciones(String correoUsuario);

    NotificacionResponse marcarNotificacionLeida(Long notificacionId, String correoUsuario);
}
