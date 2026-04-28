package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.application.chat.ChatResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** 聊天响应 DTO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
    String answer,
    List<Reference> references,
    String userMessageId,
    String assistantMessageId,
    String invocationLogId) {

  /** 检索参考来源. */
  public record Reference(
      String documentId, String fileName, int chunkIndex, String content, double score) {}

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
