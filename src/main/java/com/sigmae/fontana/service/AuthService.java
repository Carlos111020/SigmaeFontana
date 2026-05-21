package com.sigmae.fontana.service;

import com.sigmae.fontana.dto.auth.AuthResponse;
import com.sigmae.fontana.dto.auth.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
