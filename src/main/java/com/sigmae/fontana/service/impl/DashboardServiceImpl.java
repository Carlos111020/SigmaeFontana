package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.dashboard.DashboardResponse;
import com.sigmae.fontana.entity.enums.EstadoNovedad;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.NovedadRepository;
import com.sigmae.fontana.repository.RegistroAccesoRepository;
import com.sigmae.fontana.service.DashboardService;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EstudianteRepository estudianteRepository;
    private final RegistroAccesoRepository registroAccesoRepository;
    private final NovedadRepository novedadRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse metricas() {
        var inicio = LocalDate.now().atStartOfDay();
        var fin = inicio.plusDays(1);
        var presentes = estudianteRepository.findByEstadoPermanencia(EstadoPermanencia.DENTRO_DEL_PLANTEL);
        Map<String, Long> presentesPorGrado = presentes.stream()
                .collect(Collectors.groupingBy(e -> e.getGrado().getNombre(), Collectors.counting()));

        return new DashboardResponse(
                presentes.size(),
                registroAccesoRepository.countByTipoRegistroAndFechaHoraBetween(TipoRegistro.INGRESO, inicio, fin),
                registroAccesoRepository.countByTipoRegistroAndFechaHoraBetween(TipoRegistro.SALIDA, inicio, fin),
                novedadRepository.countByEstado(EstadoNovedad.PENDIENTE),
                presentesPorGrado
        );
    }
}
