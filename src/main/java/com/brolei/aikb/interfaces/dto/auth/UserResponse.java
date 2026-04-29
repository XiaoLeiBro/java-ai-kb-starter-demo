package com.brolei.aikb.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** 用于 API 输出的用户响应 DTO. */
@Schema(description = "用户信息")
public record UserResponse(
    @Schema(description = "用户 ID", example = "7d8b0a1f-2d49-4ad2-a2cf-5e3e59f2a111") String id,
    @Schema(description = "用户名", example = "demo_user") String username,
    @Schema(description = "邮箱", example = "demo@example.com") String email,
    @Schema(description = "用户状态", example = "ACTIVE") String status,
    @Schema(description = "创建时间") Instant createdAt,
    @Schema(description = "更新时间") Instant updatedAt) {

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
