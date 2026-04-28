package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/** 调用记录响应 DTO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvocationLogResponse(
    String id,
    String knowledgeBaseId,
    String conversationId,
    String messageId,
    String modelName,
    int promptTokens,
    int completionTokens,
    int totalTokens,
    long durationMs,
    String status,
    String errorMessage,
    Instant createdAt) {

  public static InvocationLogResponse from(InvocationLog invocationLog) {
    return new InvocationLogResponse(
        invocationLog.id().value(),
        invocationLog.knowledgeBaseId() != null ? invocationLog.knowledgeBaseId().value() : null,
        invocationLog.conversationId() != null ? invocationLog.conversationId().value() : null,
        invocationLog.messageId() != null ? invocationLog.messageId().value() : null,
        invocationLog.modelName(),
        invocationLog.promptTokens(),
        invocationLog.completionTokens(),
        invocationLog.totalTokens(),
        invocationLog.durationMs(),
        invocationLog.status().name(),
        invocationLog.errorMessage(),
        invocationLog.createdAt());
  }
}
