package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.grado.GradoRequest;
import com.sigmae.fontana.dto.grado.GradoResponse;
import com.sigmae.fontana.entity.Grado;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.GradoMapper;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.GradoRepository;
import com.sigmae.fontana.service.GradoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradoServiceImpl implements GradoService {

    private final GradoRepository gradoRepository;
    private final EstudianteRepository estudianteRepository;
    private final GradoMapper gradoMapper;

    @Override
    @Transactional
    public GradoResponse crear(GradoRequest request) {
        if (gradoRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("El grado ya existe", HttpStatus.CONFLICT);
        }
        var grado = new Grado();
        grado.setNombre(request.nombre());
        grado.setNivel(request.nivel());
        grado.setActivo(true);
        return gradoMapper.toResponse(gradoRepository.save(grado));
    }

    @Override
    @Transactional(readOnly = true)
    public GradoResponse obtener(Long id) {
        return gradoRepository.findById(id)
                .map(gradoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Grado no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradoResponse> listar(Boolean activo) {
        return gradoRepository.findAll()
                .stream()
                .filter(grado -> activo == null || grado.isActivo() == activo)
                .map(gradoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public GradoResponse actualizar(Long id, GradoRequest request) {
        var grado = gradoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grado no encontrado"));
        if (!grado.getNombre().equals(request.nombre()) && gradoRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("El grado ya existe", HttpStatus.CONFLICT);
        }
        grado.setNombre(request.nombre());
        grado.setNivel(request.nivel());
        return gradoMapper.toResponse(grado);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        var grado = gradoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grado no encontrado"));
        if (estudianteRepository.existsByGradoIdAndActivoTrue(id)) {
            throw new BusinessException("No se puede desactivar un grado con estudiantes activos", HttpStatus.CONFLICT);
        }
        grado.setActivo(false);
    }
}
