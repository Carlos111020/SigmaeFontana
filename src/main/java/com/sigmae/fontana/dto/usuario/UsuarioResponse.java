package com.sigmae.fontana.dto.usuario;

import com.sigmae.fontana.entity.enums.RolUsuario;

public record UsuarioResponse(
        Long id,
        String nombres,
        String apellidos,
        String correo,
        RolUsuario rol,
        boolean activo
) {
}
