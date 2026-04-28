package com.brolei.aikb.interfaces.dto.auth;

/** 包含 JWT 令牌和用户信息的登录响应 DTO. */
public record LoginResponse(String token, UserResponse user) {}
