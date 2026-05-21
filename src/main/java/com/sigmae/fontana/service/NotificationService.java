package com.sigmae.fontana.service;

import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.enums.TipoNotificacion;

public interface NotificationService {

    void notificarAcudientes(Estudiante estudiante, TipoNotificacion tipo, String mensaje);
}
