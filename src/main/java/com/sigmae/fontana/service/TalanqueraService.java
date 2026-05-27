package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoResponse;

public interface TalanqueraService {

    TalanqueraIngresoResponse registrarIngreso(TalanqueraIngresoRequest request, String correoUsuario);
}
