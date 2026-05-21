package com.sigmae.fontana.dto.dashboard;

import java.util.Map;

public record DashboardResponse(
        long estudiantesPresentes,
        long ingresosDelDia,
        long salidasDelDia,
        long novedadesPendientes,
        Map<String, Long> presentesPorGrado
) {
}
