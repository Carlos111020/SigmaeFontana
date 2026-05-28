package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.talanquera.TalanqueraAccesoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraAccesoResponse;

public interface TalanqueraService {

    TalanqueraAccesoResponse registrarIngreso(TalanqueraAccesoRequest request, String correoUsuario);

    TalanqueraAccesoResponse registrarSalida(TalanqueraAccesoRequest request, String correoUsuario);
}
