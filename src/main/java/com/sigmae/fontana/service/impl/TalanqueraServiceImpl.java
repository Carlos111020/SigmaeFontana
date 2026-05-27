package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.registro.RegistroAccesoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraAcudienteResponse;
import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoResponse;
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
    public TalanqueraIngresoResponse registrarIngreso(TalanqueraIngresoRequest request, String correoUsuario) {
        var puntoAcceso = puntoAccesoRepository.findByNombre(PUNTO_ACCESO_PRINCIPAL)
                .filter(punto -> punto.isActivo())
                .orElseThrow(() -> new ResourceNotFoundException("Punto de acceso principal no encontrado"));
        var registroRequest = new RegistroAccesoRequest(
                request.codigoTarjeta(),
                puntoAcceso.getId(),
                request.observacion(),
                false
        );
        var registro = registroAccesoService.registrarIngreso(registroRequest, correoUsuario);
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

        return new TalanqueraIngresoResponse(
                registro.id(),
                registro.fechaHora(),
                estudiante.getCodigoEstudiantil(),
                estudiante.getId(),
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                estudiante.getGrado().getNombre(),
                registro.estadoPermanencia(),
                registro.puntoAcceso(),
                "Correo simulado enviado al acudiente con la hora de llegada: " + registro.fechaHora(),
                acudientes
        );
    }
}
