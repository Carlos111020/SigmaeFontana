package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.Notificacion;
import com.sigmae.fontana.entity.enums.TipoNotificacion;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.NotificacionRepository;
import com.sigmae.fontana.service.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EstudianteAcudienteRepository estudianteAcudienteRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public void notificarAcudientes(Estudiante estudiante, TipoNotificacion tipo, String mensaje) {
        estudianteAcudienteRepository.findByEstudianteId(estudiante.getId()).forEach(relacion -> {
            var notificacion = new Notificacion();
            notificacion.setEstudiante(estudiante);
            notificacion.setAcudiente(relacion.getAcudiente());
            notificacion.setTipoNotificacion(tipo);
            notificacion.setMensaje(mensaje);
            notificacion.setFechaHora(LocalDateTime.now());
            notificacion.setLeida(false);
            notificacionRepository.save(notificacion);
        });
    }
}
