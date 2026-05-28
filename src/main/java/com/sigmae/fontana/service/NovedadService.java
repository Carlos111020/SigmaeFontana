package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.novedad.EstadoNovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadResponse;
import com.sigmae.fontana.entity.enums.EstadoNovedad;
import com.sigmae.fontana.entity.enums.TipoNovedad;
import java.util.List;

public interface NovedadService {

    NovedadResponse crear(NovedadRequest request, String correoUsuario);

    NovedadResponse actualizarEstado(Long id, EstadoNovedadRequest request);

    List<NovedadResponse> listar(EstadoNovedad estado, Long estudianteId, TipoNovedad tipoNovedad);
}
