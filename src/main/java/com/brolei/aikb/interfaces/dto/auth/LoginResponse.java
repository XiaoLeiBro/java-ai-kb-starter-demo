package com.brolei.aikb.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** 包含 JWT 令牌和用户信息的登录响应 DTO. */
@Schema(description = "用户登录响应")
public record LoginResponse(
    @Schema(description = "JWT Token，调用需要登录的接口时放到 Authorization: Bearer 后面") String token,
    @Schema(description = "登录用户信息") UserResponse user) {}
