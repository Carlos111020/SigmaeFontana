package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.usuario.UsuarioCreateRequest;
import com.sigmae.fontana.dto.usuario.UsuarioResponse;
import com.sigmae.fontana.entity.Usuario;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.mapper.UsuarioMapper;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("El correo ya se encuentra registrado", HttpStatus.CONFLICT);
        }

        var usuario = new Usuario();
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setCorreo(request.correo());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }
}
