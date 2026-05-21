package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.novedad.EstadoNovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadResponse;
import com.sigmae.fontana.entity.Novedad;
import com.sigmae.fontana.entity.enums.TipoNotificacion;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.NovedadMapper;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.NovedadRepository;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.service.NotificationService;
import com.sigmae.fontana.service.NovedadService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NovedadServiceImpl implements NovedadService {

    private final NovedadRepository novedadRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NovedadMapper novedadMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public NovedadResponse crear(NovedadRequest request, String correoUsuario) {
        var estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        var usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado"));

        var novedad = new Novedad();
        novedad.setTipoNovedad(request.tipoNovedad());
        novedad.setDescripcion(request.descripcion());
        novedad.setFechaHora(LocalDateTime.now());
        novedad.setEstudiante(estudiante);
        novedad.setUsuarioResponsable(usuario);

        var guardada = novedadRepository.save(novedad);
        notificationService.notificarAcudientes(estudiante, TipoNotificacion.NOVEDAD, request.descripcion());
        return novedadMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public NovedadResponse actualizarEstado(Long id, EstadoNovedadRequest request) {
        var novedad = novedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novedad no encontrada"));
        novedad.setEstado(request.estado());
        return novedadMapper.toResponse(novedad);
    }
}
