package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.usuario.UsuarioCreateRequest;
import com.sigmae.fontana.dto.usuario.UsuarioResponse;
import com.sigmae.fontana.dto.usuario.UsuarioUpdateRequest;
import com.sigmae.fontana.entity.Usuario;
import com.sigmae.fontana.exception.BusinessException;
import com.sigmae.fontana.exception.ResourceNotFoundException;
import com.sigmae.fontana.mapper.UsuarioMapper;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.service.UsuarioService;
import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Boolean activo) {
        return usuarioRepository.findAll()
                .stream()
                .filter(usuario -> activo == null || usuario.isActivo() == activo)
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!usuario.getCorreo().equals(request.correo()) && usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("El correo ya se encuentra registrado", HttpStatus.CONFLICT);
        }

        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setCorreo(request.correo());
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo());
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setActivo(false);
    }
}
