package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.application.chat.ChatResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 聊天响应 DTO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 问答响应")
public record ChatResponse(
    @Schema(description = "AI 生成的回答") String answer,
    @Schema(description = "本次回答引用的知识库片段") List<Reference> references,
    @Schema(description = "用户消息 ID，传入会话 ID 时返回") String userMessageId,
    @Schema(description = "AI 回复消息 ID，传入会话 ID 时返回") String assistantMessageId,
    @Schema(description = "AI 调用记录 ID") String invocationLogId) {

  /** 检索参考来源. */
  @Schema(description = "回答引用的知识片段")
  public record Reference(
      @Schema(description = "来源文档 ID") String documentId,
      @Schema(description = "来源文件名", example = "company-policy-demo.md") String fileName,
      @Schema(description = "文档切分片段序号", example = "0") int chunkIndex,
      @Schema(description = "命中的原文片段") String content,
      @Schema(description = "检索相似度分数，越高越相关", example = "0.82") double score) {}

  /** 从 ChatResult 创建 ChatResponse. */
  public static ChatResponse from(ChatResult result) {
    List<Reference> refs =
        result.chatAnswer().references().stream()
            .map(
                r ->
                    new Reference(
                        r.documentId().value(),
                        r.fileName(),
                        r.chunkIndex(),
                        r.content(),
                        r.score()))
            .toList();
    return new ChatResponse(
        result.chatAnswer().answer(),
        refs,
        result.userMessageId(),
        result.assistantMessageId(),
        result.invocationLogId());
  }
}
