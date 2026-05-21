package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.usuario.UsuarioCreateRequest;
import com.sigmae.fontana.dto.usuario.UsuarioResponse;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioCreateRequest request);
}
