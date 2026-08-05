package com.rentflow.service;

import com.rentflow.dto.request.LoginRequest;
import com.rentflow.dto.request.RefreshTokenRequest;
import com.rentflow.dto.request.RegisterRequest;
import com.rentflow.dto.response.AuthResponse;
import com.rentflow.dto.response.AuthUserDTO;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String email);
    AuthUserDTO getCurrentUser();
}
