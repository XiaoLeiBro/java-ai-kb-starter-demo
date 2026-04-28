package com.brolei.aikb.application.user;

import com.brolei.aikb.domain.user.model.User;

/** 成功登录的结果，包含用户信息和 JWT 令牌. */
public record LoginResult(User user, String token) {}
