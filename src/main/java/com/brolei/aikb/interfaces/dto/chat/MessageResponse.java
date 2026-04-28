package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.Message;
import java.time.Instant;

/** 消息响应 DTO. */
public record MessageResponse(String id, String role, String content, Instant createdAt) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.id().value(), message.role().name(), message.content(), message.createdAt());
  }
}
