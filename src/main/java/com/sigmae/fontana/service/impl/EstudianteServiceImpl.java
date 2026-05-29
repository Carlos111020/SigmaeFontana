package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.estudiante.EstudianteAcudienteRequest;
import com.sigmae.fontana.dto.estudiante.EstudianteAcudienteResponse;
import com.sigmae.fontana.dto.estudiante.EstudianteRequest;
import com.sigmae.fontana.dto.estudiante.EstudianteResponse;
import com.sigmae.fontana.entity.EstudianteAcudiente;
import com.sigmae.fontana.entity.EstudianteAcudienteId;
import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.AcudienteMapper;
import com.sigmae.fontana.mapper.EstudianteMapper;
import com.sigmae.fontana.repository.AcudienteRepository;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.GradoRepository;
import com.sigmae.fontana.service.EstudianteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final GradoRepository gradoRepository;
    private final AcudienteRepository acudienteRepository;
    private final EstudianteAcudienteRepository estudianteAcudienteRepository;
    private final EstudianteMapper estudianteMapper;
    private final AcudienteMapper acudienteMapper;

    @Override
    @Transactional
    public EstudianteResponse crear(EstudianteRequest request) {
        validarUnicos(request.codigoEstudiantil(), request.documento());
        var grado = gradoRepository.findById(request.gradoId())
                .filter(g -> g.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Grado no encontrado o inactivo"));

        var estudiante = new Estudiante();
        estudiante.setCodigoEstudiantil(request.codigoEstudiantil());
        estudiante.setDocumento(request.documento());
        estudiante.setNombres(request.nombres());
        estudiante.setApellidos(request.apellidos());
        estudiante.setGrado(grado);
        estudiante.setActivo(true);
        estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        return estudianteMapper.toResponse(estudianteRepository.save(estudiante));
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponse obtener(Long id) {
        return estudianteRepository.findById(id)
                .map(estudianteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar(Boolean activo) {
        return estudianteRepository.findAll()
                .stream()
                .filter(estudiante -> activo == null || estudiante.isActivo() == activo)
                .map(estudianteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EstudianteResponse actualizar(Long id, EstudianteRequest request) {
        var estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        if (!estudiante.getCodigoEstudiantil().equals(request.codigoEstudiantil())
                && estudianteRepository.existsByCodigoEstudiantil(request.codigoEstudiantil())) {
            throw new BusinessException("El codigo estudiantil ya existe", HttpStatus.CONFLICT);
        }
        if (!estudiante.getDocumento().equals(request.documento())
                && estudianteRepository.existsByDocumento(request.documento())) {
            throw new BusinessException("El documento ya existe", HttpStatus.CONFLICT);
        }
        var grado = gradoRepository.findById(request.gradoId())
                .filter(g -> g.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Grado no encontrado o inactivo"));

        estudiante.setCodigoEstudiantil(request.codigoEstudiantil());
        estudiante.setDocumento(request.documento());
        estudiante.setNombres(request.nombres());
        estudiante.setApellidos(request.apellidos());
        estudiante.setGrado(grado);
        return estudianteMapper.toResponse(estudiante);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        var estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        estudiante.setActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponse> presentes(Long gradoId, String texto) {
        return estudianteRepository.findByEstadoPermanencia(EstadoPermanencia.DENTRO_DEL_PLANTEL)
                .stream()
                .filter(estudiante -> gradoId == null || estudiante.getGrado().getId().equals(gradoId))
                .filter(estudiante -> !StringUtils.hasText(texto) || coincide(estudiante, texto))
                .map(estudianteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void asociarAcudiente(Long estudianteId, EstudianteAcudienteRequest request) {
        var estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        var acudiente = acudienteRepository.findById(request.acudienteId())
                .filter(a -> a.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Acudiente no encontrado o inactivo"));

        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudiante.getId());
        id.setAcudienteId(acudiente.getId());
        if (estudianteAcudienteRepository.existsById(id)) {
            throw new BusinessException("El acudiente ya esta asociado al estudiante", HttpStatus.CONFLICT);
        }

        var relacion = new EstudianteAcudiente();
        relacion.setId(id);
        relacion.setEstudiante(estudiante);
        relacion.setAcudiente(acudiente);
        relacion.setParentesco(request.parentesco());
        relacion.setResponsablePrincipal(request.responsablePrincipal());
        estudianteAcudienteRepository.save(relacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteAcudienteResponse> listarAcudientes(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new ResourceNotFoundException("Estudiante no encontrado");
        }
        return estudianteAcudienteRepository.findByEstudianteId(estudianteId)
                .stream()
                .map(this::toAcudienteResponse)
                .toList();
    }

    @Override
    @Transactional
    public EstudianteAcudienteResponse actualizarAcudiente(
            Long estudianteId,
            Long acudienteId,
            EstudianteAcudienteRequest request
    ) {
        if (!acudienteId.equals(request.acudienteId())) {
            throw new BusinessException(
                    "El acudiente de la ruta no coincide con el cuerpo de la solicitud",
                    HttpStatus.CONFLICT
            );
        }
        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudianteId);
        id.setAcudienteId(acudienteId);
        var relacion = estudianteAcudienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relacion estudiante-acudiente no encontrada"));

        relacion.setParentesco(request.parentesco());
        relacion.setResponsablePrincipal(request.responsablePrincipal());
        return toAcudienteResponse(relacion);
    }

    @Override
    @Transactional
    public void eliminarAcudiente(Long estudianteId, Long acudienteId) {
        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudianteId);
        id.setAcudienteId(acudienteId);
        var relacion = estudianteAcudienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relacion estudiante-acudiente no encontrada"));
        estudianteAcudienteRepository.delete(relacion);
    }

    private void validarUnicos(String codigoEstudiantil, String documento) {
        if (estudianteRepository.existsByCodigoEstudiantil(codigoEstudiantil)) {
            throw new BusinessException("El codigo estudiantil ya existe", HttpStatus.CONFLICT);
        }
        if (estudianteRepository.existsByDocumento(documento)) {
            throw new BusinessException("El documento ya existe", HttpStatus.CONFLICT);
        }
    }

    private boolean coincide(Estudiante estudiante, String texto) {
        var filtro = texto.toLowerCase();
        return estudiante.getCodigoEstudiantil().toLowerCase().contains(filtro)
                || estudiante.getDocumento().toLowerCase().contains(filtro)
                || estudiante.getNombres().toLowerCase().contains(filtro)
                || estudiante.getApellidos().toLowerCase().contains(filtro);
    }

    private EstudianteAcudienteResponse toAcudienteResponse(EstudianteAcudiente relacion) {
        return new EstudianteAcudienteResponse(
                relacion.getEstudiante().getId(),
                relacion.getAcudiente().getId(),
                relacion.getParentesco(),
                relacion.isResponsablePrincipal(),
                acudienteMapper.toResponse(relacion.getAcudiente())
        );
    }
}
