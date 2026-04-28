package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.domain.knowledge.model.ChatAnswer;
import java.util.List;

/** 聊天响应 DTO. */
public record ChatResponse(String answer, List<Reference> references) {

  /** 检索参考来源. */
  public record Reference(
      String documentId, String fileName, int chunkIndex, String content, double score) {}

  /** 从领域 ChatAnswer 对象创建 ChatResponse. */
  public static ChatResponse from(ChatAnswer chatAnswer) {
    List<Reference> refs =
        chatAnswer.references().stream()
            .map(
                r ->
                    new Reference(
                        r.documentId().value(),
                        r.fileName(),
                        r.chunkIndex(),
                        r.content(),
                        r.score()))
            .toList();
    return new ChatResponse(chatAnswer.answer(), refs);
  }
}
