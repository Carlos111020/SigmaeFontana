package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.novedad.EstadoNovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadResponse;

public interface NovedadService {

    NovedadResponse crear(NovedadRequest request, String correoUsuario);

    NovedadResponse actualizarEstado(Long id, EstadoNovedadRequest request);
}
