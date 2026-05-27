package com.sigmae.fontana.dto.talanquera;

public record TalanqueraAcudienteResponse(
        Long id,
        String nombres,
        String apellidos,
        String parentesco,
        String correo,
        boolean correoSimuladoEnviado
) {
}
