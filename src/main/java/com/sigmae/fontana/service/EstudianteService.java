package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.estudiante.EstudianteRequest;
import com.sigmae.fontana.dto.estudiante.EstudianteResponse;
import java.util.List;

public interface EstudianteService {

    EstudianteResponse crear(EstudianteRequest request);

    EstudianteResponse obtener(Long id);

    List<EstudianteResponse> listar(Boolean activo);

    EstudianteResponse actualizar(Long id, EstudianteRequest request);

    void desactivar(Long id);

    List<EstudianteResponse> presentes(Long gradoId, String texto);
}
