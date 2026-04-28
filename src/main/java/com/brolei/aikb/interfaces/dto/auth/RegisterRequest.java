package com.brolei.aikb.interfaces.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 注册请求 DTO. */
public record RegisterRequest(
    @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,20}$",
            message = "username must be 3-20 characters, alphanumeric and underscore only")
        String username,
    @NotBlank @Size(min = 8) String password,
    @Email String email) {}
