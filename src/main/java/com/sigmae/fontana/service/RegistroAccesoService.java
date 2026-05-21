package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.registro.RegistroAccesoRequest;
import com.sigmae.fontana.dto.registro.RegistroAccesoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroAccesoService {

    RegistroAccesoResponse registrarIngreso(RegistroAccesoRequest request, String correoUsuario);

    RegistroAccesoResponse registrarSalida(RegistroAccesoRequest request, String correoUsuario);

    Page<RegistroAccesoResponse> historial(Long estudianteId, Pageable pageable);
}
