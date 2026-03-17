package com.example.assistant.controller;

import com.example.assistant.dto.request.LoginRequest;
import com.example.assistant.dto.request.RegisterRequest;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.dto.response.LoginResponse;
import com.example.assistant.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }
}
