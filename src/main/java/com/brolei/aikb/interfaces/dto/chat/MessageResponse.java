package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** 消息响应 DTO. */
@Schema(description = "会话消息")
public record MessageResponse(
    @Schema(description = "消息 ID") String id,
    @Schema(description = "消息角色，USER 表示用户，ASSISTANT 表示 AI", example = "USER") String role,
    @Schema(description = "消息内容") String content,
    @Schema(description = "创建时间") Instant createdAt) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.id().value(), message.role().name(), message.content(), message.createdAt());
  }
}
