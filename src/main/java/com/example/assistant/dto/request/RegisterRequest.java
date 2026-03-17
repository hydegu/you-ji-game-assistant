package com.example.assistant.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    /** 不传则默认 USER */
    private String role;
}
