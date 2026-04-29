package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.Conversation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** 会话响应 DTO. */
@Schema(description = "对话会话信息")
public record ConversationResponse(
    @Schema(description = "会话 ID", example = "8f6d7b2b-0a54-40da-a6d6-5af81e04d102") String id,
    @Schema(description = "会话标题", example = "报销制度咨询") String title,
    @Schema(description = "会话状态", example = "ACTIVE") String status,
    @Schema(description = "所属知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
        String knowledgeBaseId,
    @Schema(description = "创建时间") Instant createdAt,
    @Schema(description = "更新时间") Instant updatedAt) {

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
