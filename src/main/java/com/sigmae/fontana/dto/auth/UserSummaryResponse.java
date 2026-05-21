package com.sigmae.fontana.dto.auth;

import com.sigmae.fontana.entity.enums.RolUsuario;

public record UserSummaryResponse(
        Long id,
        String nombres,
        String apellidos,
        String correo,
        RolUsuario rol
) {
}
