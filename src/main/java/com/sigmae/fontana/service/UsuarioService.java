package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.usuario.UsuarioCreateRequest;
import com.sigmae.fontana.dto.usuario.UsuarioResponse;
import com.sigmae.fontana.dto.usuario.UsuarioUpdateRequest;
import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioCreateRequest request);

    UsuarioResponse obtener(Long id);

    List<UsuarioResponse> listar(Boolean activo);

    UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request);

    void desactivar(Long id);
}
