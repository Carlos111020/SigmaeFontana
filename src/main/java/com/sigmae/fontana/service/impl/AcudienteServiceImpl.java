package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.acudiente.AcudienteRequest;
import com.sigmae.fontana.dto.acudiente.AcudienteResponse;
import com.sigmae.fontana.dto.acudiente.AcudienteStudentResponse;
import com.sigmae.fontana.dto.notificacion.NotificacionResponse;
import com.sigmae.fontana.entity.Acudiente;
import com.sigmae.fontana.entity.Usuario;
import com.sigmae.fontana.entity.enums.RolUsuario;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.AcudienteMapper;
import com.sigmae.fontana.mapper.NotificacionMapper;
import com.sigmae.fontana.repository.AcudienteRepository;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.NotificacionRepository;
import com.sigmae.fontana.repository.RegistroAccesoRepository;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.service.AcudienteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcudienteServiceImpl implements AcudienteService {

    private final AcudienteRepository acudienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteAcudienteRepository estudianteAcudienteRepository;
    private final RegistroAccesoRepository registroAccesoRepository;
    private final NotificacionRepository notificacionRepository;
    private final AcudienteMapper acudienteMapper;
    private final NotificacionMapper notificacionMapper;

    @Override
    @Transactional
    public AcudienteResponse crear(AcudienteRequest request) {
        validarUnicos(request.documento(), request.correo());
        var acudiente = new Acudiente();
        aplicarDatos(acudiente, request);
        acudiente.setActivo(true);
        return acudienteMapper.toResponse(acudienteRepository.save(acudiente));
    }

    @Override
    @Transactional(readOnly = true)
    public AcudienteResponse obtener(Long id) {
        return acudienteRepository.findById(id)
                .map(acudienteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Acudiente no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcudienteResponse> listar(Boolean activo) {
        return acudienteRepository.findAll()
                .stream()
                .filter(acudiente -> activo == null || acudiente.isActivo() == activo)
                .map(acudienteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AcudienteResponse actualizar(Long id, AcudienteRequest request) {
        var acudiente = acudienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acudiente no encontrado"));
        if (!acudiente.getDocumento().equals(request.documento())
                && acudienteRepository.existsByDocumento(request.documento())) {
            throw new BusinessException("El documento del acudiente ya existe", HttpStatus.CONFLICT);
        }
        if (!acudiente.getCorreo().equals(request.correo())
                && acudienteRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("El correo del acudiente ya existe", HttpStatus.CONFLICT);
        }
        aplicarDatos(acudiente, request);
        return acudienteMapper.toResponse(acudiente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        var acudiente = acudienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acudiente no encontrado"));
        acudiente.setActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcudienteStudentResponse> misEstudiantes(String correoUsuario) {
        return estudianteAcudienteRepository.findMisRelacionesDeAcudiente(correoUsuario)
                .stream()
                .map(relacion -> {
                    var estudiante = relacion.getEstudiante();
                    var ultimoRegistro = registroAccesoRepository
                            .findFirstByEstudianteIdOrderByFechaHoraDesc(estudiante.getId());
                    return new AcudienteStudentResponse(
                            estudiante.getId(),
                            estudiante.getCodigoEstudiantil(),
                            estudiante.getNombres() + " " + estudiante.getApellidos(),
                            estudiante.getGrado().getNombre(),
                            estudiante.getEstadoPermanencia(),
                            ultimoRegistro.map(r -> r.getTipoRegistro()).orElse(null),
                            ultimoRegistro.map(r -> r.getFechaHora()).orElse(null)
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones(String correoUsuario) {
        return notificacionRepository.findMisNotificacionesDeAcudiente(correoUsuario)
                .stream()
                .map(notificacionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificacionResponse marcarNotificacionLeida(Long notificacionId, String correoUsuario) {
        var notificacion = notificacionRepository.findMiNotificacionDeAcudiente(notificacionId, correoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada"));
        notificacion.setLeida(true);
        return notificacionMapper.toResponse(notificacion);
    }

    private void validarUnicos(String documento, String correo) {
        if (acudienteRepository.existsByDocumento(documento)) {
            throw new BusinessException("El documento del acudiente ya existe", HttpStatus.CONFLICT);
        }
        if (acudienteRepository.existsByCorreo(correo)) {
            throw new BusinessException("El correo del acudiente ya existe", HttpStatus.CONFLICT);
        }
    }

    private void aplicarDatos(Acudiente acudiente, AcudienteRequest request) {
        acudiente.setDocumento(request.documento());
        acudiente.setNombres(request.nombres());
        acudiente.setApellidos(request.apellidos());
        acudiente.setTelefono(request.telefono());
        acudiente.setCorreo(request.correo());
        acudiente.setUsuario(resolverUsuario(request.usuarioId(), acudiente.getId()));
    }

    private Usuario resolverUsuario(Long usuarioId, Long acudienteId) {
        if (usuarioId == null) {
            return null;
        }
        var usuarioAsignado = acudienteRepository.findByUsuarioId(usuarioId);
        if (usuarioAsignado.isPresent() && !usuarioAsignado.get().getId().equals(acudienteId)) {
            throw new BusinessException("El usuario ya esta asociado a otro acudiente", HttpStatus.CONFLICT);
        }
        return usuarioRepository.findById(usuarioId)
                .filter(usuario -> usuario.isActivo() && usuario.getRol() == RolUsuario.ACUDIENTE)
                .orElseThrow(() -> new BusinessException("El usuario asociado debe existir, estar activo y tener rol ACUDIENTE", HttpStatus.CONFLICT));
    }
}
