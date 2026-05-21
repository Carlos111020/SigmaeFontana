package com.sigmae.fontana.service.impl;

import com.sigmae.fontana.dto.auth.AuthResponse;
import com.sigmae.fontana.dto.auth.LoginRequest;
import com.sigmae.fontana.mapper.UsuarioMapper;
import com.sigmae.fontana.repository.UsuarioRepository;
import com.sigmae.fontana.security.JwtService;
import com.sigmae.fontana.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.password())
        );
        var usuario = usuarioRepository.findByCorreo(request.correo()).orElseThrow();
        var userDetails = User.withUsername(usuario.getCorreo())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
        return new AuthResponse(
                jwtService.generateToken(userDetails),
                "Bearer",
                jwtService.expirationDate(),
                usuarioMapper.toSummary(usuario)
        );
    }
}
