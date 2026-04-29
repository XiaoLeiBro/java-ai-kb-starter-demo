package com.brolei.aikb.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 登录请求 DTO. */
@Schema(description = "用户登录请求")
public record LoginRequest(
    @Schema(description = "用户名", example = "demo_user") @NotBlank String username,
    @Schema(description = "密码", example = "demo_password") @NotBlank String password) {}
