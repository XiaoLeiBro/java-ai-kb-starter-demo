package com.brolei.aikb.domain.knowledge.model;

import com.brolei.aikb.domain.llm.LlmChatResult;
import java.util.List;

/**
 * 聊天回答值对象.
 *
 * <p>包含 LLM 生成的回答文本以及作为参考依据的检索切片列表。
 */
public record ChatAnswer(String answer, List<RetrievedChunk> references, LlmChatResult llmResult) {

  /** v0.2 兼容构造（不含 token 统计）. */
  public ChatAnswer(String answer, List<RetrievedChunk> references) {
    this(answer, references, null);
  }
}
