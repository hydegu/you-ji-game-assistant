package com.example.assistant.controller;

import com.example.assistant.dto.request.LoginRequest;
import com.example.assistant.dto.request.RegisterRequest;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.dto.response.LoginResponse;
import com.example.assistant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "用户登录与注册接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }
}
