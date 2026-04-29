package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** 调用记录响应 DTO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 调用记录")
public record InvocationLogResponse(
    @Schema(description = "调用记录 ID") String id,
    @Schema(description = "知识库 ID") String knowledgeBaseId,
    @Schema(description = "会话 ID，单轮问答时可能为空") String conversationId,
    @Schema(description = "关联消息 ID，单轮问答时可能为空") String messageId,
    @Schema(description = "调用的 Chat 模型名称", example = "deepseek-v4-flash") String modelName,
    @Schema(description = "提示词 Token 数", example = "120") int promptTokens,
    @Schema(description = "回答 Token 数", example = "80") int completionTokens,
    @Schema(description = "总 Token 数", example = "200") int totalTokens,
    @Schema(description = "调用耗时，单位毫秒", example = "1350") long durationMs,
    @Schema(description = "调用状态", example = "SUCCESS") String status,
    @Schema(description = "失败原因，成功时为空") String errorMessage,
    @Schema(description = "创建时间") Instant createdAt) {

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
