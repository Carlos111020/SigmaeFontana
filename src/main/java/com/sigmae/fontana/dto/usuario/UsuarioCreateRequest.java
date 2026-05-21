package com.sigmae.fontana.dto.usuario;

import com.sigmae.fontana.entity.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotNull RolUsuario rol
) {
}
