package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.grado.GradoRequest;
import com.sigmae.fontana.dto.grado.GradoResponse;
import java.util.List;

public interface GradoService {

    GradoResponse crear(GradoRequest request);

    GradoResponse obtener(Long id);

    List<GradoResponse> listar(Boolean activo);

    GradoResponse actualizar(Long id, GradoRequest request);

    void desactivar(Long id);
}
