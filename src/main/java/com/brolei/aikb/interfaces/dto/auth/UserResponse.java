package com.brolei.aikb.interfaces.dto.auth;

import java.time.Instant;

/** 用于 API 输出的用户响应 DTO. */
public record UserResponse(
    String id, String username, String email, String status, Instant createdAt, Instant updatedAt) {

  /** 从领域 User 对象创建 UserResponse. */
  public static UserResponse from(com.brolei.aikb.domain.user.model.User user) {
    return new UserResponse(
        user.id().value(),
        user.username(),
        user.email(),
        user.status().name(),
        user.createdAt(),
        user.updatedAt());
  }
}
