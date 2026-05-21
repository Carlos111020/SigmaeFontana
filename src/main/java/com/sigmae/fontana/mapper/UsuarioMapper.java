package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.auth.UserSummaryResponse;
import com.sigmae.fontana.dto.usuario.UsuarioResponse;
import com.sigmae.fontana.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    default UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }

    default UserSummaryResponse toSummary(Usuario usuario) {
        return new UserSummaryResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}
