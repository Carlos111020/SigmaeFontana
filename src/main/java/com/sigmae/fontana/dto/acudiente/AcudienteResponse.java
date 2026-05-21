package com.sigmae.fontana.dto.acudiente;

public record AcudienteResponse(
        Long id,
        String documento,
        String nombres,
        String apellidos,
        String telefono,
        String correo,
        boolean activo,
        Long usuarioId,
        String usuarioCorreo
) {
}
