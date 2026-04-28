package com.brolei.aikb.interfaces.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** 登录请求 DTO. */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
