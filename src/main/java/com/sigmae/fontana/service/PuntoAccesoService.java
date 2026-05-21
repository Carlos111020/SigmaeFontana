package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoRequest;
import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoResponse;
import java.util.List;

public interface PuntoAccesoService {

    PuntoAccesoResponse crear(PuntoAccesoRequest request);

    PuntoAccesoResponse obtener(Long id);

    List<PuntoAccesoResponse> listar(Boolean activo);

    PuntoAccesoResponse actualizar(Long id, PuntoAccesoRequest request);

    void desactivar(Long id);
}
