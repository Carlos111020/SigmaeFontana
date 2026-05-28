package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.registro.RegistroAccesoRequest;
import com.sigmae.fontana.dto.registro.RegistroAccesoResponse;
import com.sigmae.fontana.dto.talanquera.TalanqueraAccesoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraAccesoResponse;
import com.sigmae.fontana.dto.talanquera.TalanqueraAcudienteResponse;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.PuntoAccesoRepository;
import com.sigmae.fontana.service.RegistroAccesoService;
import com.sigmae.fontana.service.TalanqueraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TalanqueraServiceImpl implements TalanqueraService {

    private static final String PUNTO_ACCESO_PRINCIPAL = "Porteria principal";

    private final RegistroAccesoService registroAccesoService;
    private final EstudianteRepository estudianteRepository;
    private final EstudianteAcudienteRepository estudianteAcudienteRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;

    @Override
    @Transactional
    public TalanqueraAccesoResponse registrarIngreso(TalanqueraAccesoRequest request, String correoUsuario) {
        var registroRequest = registroRequest(request, false);
        var registro = registroAccesoService.registrarIngreso(registroRequest, correoUsuario);
        return construirRespuesta(registro, request.codigoTarjeta(), TipoRegistro.INGRESO);
    }

    @Override
    @Transactional
    public TalanqueraAccesoResponse registrarSalida(TalanqueraAccesoRequest request, String correoUsuario) {
        var registroRequest = registroRequest(request, request.crearNovedadSalidaAnticipada());
        var registro = registroAccesoService.registrarSalida(registroRequest, correoUsuario);
        return construirRespuesta(registro, request.codigoTarjeta(), TipoRegistro.SALIDA);
    }

    private RegistroAccesoRequest registroRequest(TalanqueraAccesoRequest request, boolean crearNovedadSalidaAnticipada) {
        var puntoAcceso = puntoAccesoRepository.findByNombre(PUNTO_ACCESO_PRINCIPAL)
                .filter(punto -> punto.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso principal no encontrado"));
        return new RegistroAccesoRequest(
                request.codigoTarjeta(),
                puntoAcceso.getId(),
                request.observacion(),
                crearNovedadSalidaAnticipada
        );
    }

    private TalanqueraAccesoResponse construirRespuesta(
            RegistroAccesoResponse registro,
            String codigoTarjeta,
            TipoRegistro operacion
    ) {
        var estudiante = estudianteRepository.findById(registro.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        var acudientes = estudianteAcudienteRepository.findByEstudianteId(estudiante.getId())
                .stream()
                .map(relacion -> {
                    var acudiente = relacion.getAcudiente();
                    return new TalanqueraAcudienteResponse(
                            acudiente.getId(),
                            acudiente.getNombres(),
                            acudiente.getApellidos(),
                            relacion.getParentesco(),
                            acudiente.getCorreo(),
                            true
                    );
                })
                .toList();

        var movimiento = operacion == TipoRegistro.INGRESO ? "llegada" : "salida";
        return new TalanqueraAccesoResponse(
                registro.id(),
                operacion,
                registro.fechaHora(),
                codigoTarjeta,
                estudiante.getId(),
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                estudiante.getGrado().getNombre(),
                registro.estadoPermanencia(),
                registro.puntoAcceso(),
                "Correo simulado enviado al acudiente con la hora de " + movimiento + ": " + registro.fechaHora(),
                acudientes
        );
    }
}
