package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoRequest;
import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoResponse;
import com.sigmae.fontana.entity.PuntoAcceso;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.PuntoAccesoMapper;
import com.sigmae.fontana.repository.PuntoAccesoRepository;
import com.sigmae.fontana.service.PuntoAccesoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PuntoAccesoServiceImpl implements PuntoAccesoService {

    private final PuntoAccesoRepository puntoAccesoRepository;
    private final PuntoAccesoMapper puntoAccesoMapper;

    @Override
    @Transactional
    public PuntoAccesoResponse crear(PuntoAccesoRequest request) {
        if (puntoAccesoRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("El punto de acceso ya existe", HttpStatus.CONFLICT);
        }
        var puntoAcceso = new PuntoAcceso();
        puntoAcceso.setNombre(request.nombre());
        puntoAcceso.setUbicacion(request.ubicacion());
        puntoAcceso.setActivo(true);
        return puntoAccesoMapper.toResponse(puntoAccesoRepository.save(puntoAcceso));
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoAccesoResponse obtener(Long id) {
        return puntoAccesoRepository.findById(id)
                .map(puntoAccesoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoAccesoResponse> listar(Boolean activo) {
        return puntoAccesoRepository.findAll()
                .stream()
                .filter(puntoAcceso -> activo == null || puntoAcceso.isActivo() == activo)
                .map(puntoAccesoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PuntoAccesoResponse actualizar(Long id, PuntoAccesoRequest request) {
        var puntoAcceso = puntoAccesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso no encontrado"));
        if (!puntoAcceso.getNombre().equals(request.nombre()) && puntoAccesoRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("El punto de acceso ya existe", HttpStatus.CONFLICT);
        }
        puntoAcceso.setNombre(request.nombre());
        puntoAcceso.setUbicacion(request.ubicacion());
        return puntoAccesoMapper.toResponse(puntoAcceso);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        var puntoAcceso = puntoAccesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso no encontrado"));
        puntoAcceso.setActivo(false);
    }
}
