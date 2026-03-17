package com.example.assistant.service;

import com.example.assistant.dto.request.LoginRequest;
import com.example.assistant.dto.request.RegisterRequest;
import com.example.assistant.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
