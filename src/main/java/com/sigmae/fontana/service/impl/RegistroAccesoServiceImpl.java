package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.registro.RegistroAccesoRequest;
import com.sigmae.fontana.dto.registro.RegistroAccesoResponse;
import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.Jornada;
import com.sigmae.fontana.entity.Novedad;
import com.sigmae.fontana.entity.RegistroAcceso;
import com.sigmae.fontana.entity.enums.EstadoJornada;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.TipoNotificacion;
import com.sigmae.fontana.entity.enums.TipoNovedad;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.RegistroAccesoMapper;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.JornadaRepository;
import com.sigmae.fontana.repository.NovedadRepository;
import com.sigmae.fontana.repository.PuntoAccesoRepository;
import com.sigmae.fontana.repository.RegistroAccesoRepository;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.service.NotificationService;
import com.sigmae.fontana.service.RegistroAccesoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroAccesoServiceImpl implements RegistroAccesoService {

    private final RegistroAccesoRepository registroAccesoRepository;
    private final EstudianteRepository estudianteRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JornadaRepository jornadaRepository;
    private final NovedadRepository novedadRepository;
    private final RegistroAccesoMapper registroAccesoMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RegistroAccesoResponse registrarIngreso(RegistroAccesoRequest request, String correoUsuario) {
        var estudiante = buscarEstudiante(request.identificadorEstudiante());
        validarEstudianteActivo(estudiante);
        if (estudiante.getEstadoPermanencia() == EstadoPermanencia.DENTRO_DEL_PLANTEL) {
            throw new BusinessException("El estudiante ya se encuentra dentro del plantel", HttpStatus.CONFLICT);
        }

        var registro = crearRegistro(request, correoUsuario, estudiante, TipoRegistro.INGRESO);
        estudiante.setEstadoPermanencia(EstadoPermanencia.DENTRO_DEL_PLANTEL);
        var guardado = registroAccesoRepository.save(registro);
        notificationService.notificarAcudientes(
                estudiante,
                TipoNotificacion.INGRESO,
                "Ingreso registrado para " + estudiante.getNombres() + " " + estudiante.getApellidos()
        );
        return registroAccesoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public RegistroAccesoResponse registrarSalida(RegistroAccesoRequest request, String correoUsuario) {
        var estudiante = buscarEstudiante(request.identificadorEstudiante());
        validarEstudianteActivo(estudiante);
        if (estudiante.getEstadoPermanencia() != EstadoPermanencia.DENTRO_DEL_PLANTEL) {
            throw new BusinessException("El estudiante no se encuentra dentro del plantel", HttpStatus.CONFLICT);
        }

        var registro = crearRegistro(request, correoUsuario, estudiante, TipoRegistro.SALIDA);
        estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        var guardado = registroAccesoRepository.save(registro);
        if (request.crearNovedadSalidaAnticipada()) {
            crearNovedadSalidaAnticipada(estudiante, guardado);
        }
        notificationService.notificarAcudientes(
                estudiante,
                TipoNotificacion.SALIDA,
                "Salida registrada para " + estudiante.getNombres() + " " + estudiante.getApellidos()
        );
        return registroAccesoMapper.toResponse(guardado);
    }

    private void crearNovedadSalidaAnticipada(Estudiante estudiante, RegistroAcceso registro) {
        var novedad = new Novedad();
        novedad.setTipoNovedad(TipoNovedad.SALIDA_ANTICIPADA);
        novedad.setDescripcion("Salida anticipada registrada desde " + registro.getPuntoAcceso().getNombre());
        novedad.setFechaHora(LocalDateTime.now());
        novedad.setEstudiante(estudiante);
        novedad.setUsuarioResponsable(registro.getUsuarioResponsable());
        novedadRepository.save(novedad);
        notificationService.notificarAcudientes(estudiante, TipoNotificacion.NOVEDAD, novedad.getDescripcion());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAccesoResponse> historial(Long estudianteId, Pageable pageable) {
        return registroAccesoRepository.findByEstudianteIdOrderByFechaHoraDesc(estudianteId, pageable)
                .map(registroAccesoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAccesoResponse> filtrar(
            Long estudianteId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            TipoRegistro tipoRegistro,
            Long puntoAccesoId,
            Pageable pageable
    ) {
        Specification<RegistroAcceso> spec = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (estudianteId != null) {
                predicates.add(builder.equal(root.get("estudiante").get("id"), estudianteId));
            }
            if (fechaDesde != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("fechaHora"), fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                predicates.add(builder.lessThan(root.get("fechaHora"), fechaHasta.plusDays(1).atStartOfDay()));
            }
            if (tipoRegistro != null) {
                predicates.add(builder.equal(root.get("tipoRegistro"), tipoRegistro));
            }
            if (puntoAccesoId != null) {
                predicates.add(builder.equal(root.get("puntoAcceso").get("id"), puntoAccesoId));
            }
            query.orderBy(builder.desc(root.get("fechaHora")));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return registroAccesoRepository.findAll(spec, pageable).map(registroAccesoMapper::toResponse);
    }

    private RegistroAcceso crearRegistro(
            RegistroAccesoRequest request,
            String correoUsuario,
            Estudiante estudiante,
            TipoRegistro tipoRegistro
    ) {
        var puntoAcceso = puntoAccesoRepository.findById(request.puntoAccesoId())
                .filter(p -> p.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso no encontrado o inactivo"));
        var usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado"));

        var registro = new RegistroAcceso();
        registro.setTipoRegistro(tipoRegistro);
        registro.setFechaHora(LocalDateTime.now());
        registro.setObservacion(request.observacion());
        registro.setEstudiante(estudiante);
        registro.setPuntoAcceso(puntoAcceso);
        registro.setJornada(jornadaActual());
        registro.setUsuarioResponsable(usuario);
        return registro;
    }

    private Jornada jornadaActual() {
        return jornadaRepository.findByFecha(LocalDate.now()).orElseGet(() -> {
            var jornada = new Jornada();
            jornada.setFecha(LocalDate.now());
            jornada.setHoraInicio(LocalTime.of(6, 0));
            jornada.setHoraFin(LocalTime.of(15, 0));
            jornada.setEstado(EstadoJornada.ABIERTA);
            return jornadaRepository.save(jornada);
        });
    }

    private Estudiante buscarEstudiante(String identificador) {
        return estudianteRepository.findByCodigoEstudiantil(identificador)
                .or(() -> estudianteRepository.findByDocumento(identificador))
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
    }

    private void validarEstudianteActivo(Estudiante estudiante) {
        if (!estudiante.isActivo()) {
            throw new BusinessException("El estudiante esta desactivado", HttpStatus.CONFLICT);
        }
    }
}
