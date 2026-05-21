package com.sigmae.fontana.mapper;

import com.sigmae.fontana.dto.acudiente.AcudienteResponse;
import com.sigmae.fontana.entity.Acudiente;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcudienteMapper {

    default AcudienteResponse toResponse(Acudiente acudiente) {
        var usuario = acudiente.getUsuario();
        return new AcudienteResponse(
                acudiente.getId(),
                acudiente.getDocumento(),
                acudiente.getNombres(),
                acudiente.getApellidos(),
                acudiente.getTelefono(),
                acudiente.getCorreo(),
                acudiente.isActivo(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getCorreo() : null
        );
    }
}
