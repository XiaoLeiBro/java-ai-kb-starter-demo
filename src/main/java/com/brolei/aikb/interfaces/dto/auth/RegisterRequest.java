package com.brolei.aikb.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 注册请求 DTO. */
@Schema(description = "用户注册请求")
public record RegisterRequest(
    @Schema(description = "用户名，3-20 位，只允许英文字母、数字和下划线", example = "demo_user")
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,20}$",
            message = "username must be 3-20 characters, alphanumeric and underscore only")
        String username,
    @Schema(description = "密码，至少 8 位", example = "demo_password") @NotBlank @Size(min = 8)
        String password,
    @Schema(description = "邮箱，可选", example = "demo@example.com") @Email String email) {}
