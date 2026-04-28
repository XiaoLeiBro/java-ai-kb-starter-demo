package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.Conversation;
import java.time.Instant;

/** 会话响应 DTO. */
public record ConversationResponse(
    String id,
    String title,
    String status,
    String knowledgeBaseId,
    Instant createdAt,
    Instant updatedAt) {

  public static ConversationResponse from(Conversation conversation) {
    return new ConversationResponse(
        conversation.id().value(),
        conversation.title(),
        conversation.status().name(),
        conversation.knowledgeBaseId().value(),
        conversation.createdAt(),
        conversation.updatedAt());
  }
}
