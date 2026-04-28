package com.brolei.aikb.domain.llm;

/** LLM 调用结果，包含回答文本和 token 用量统计. */
public record LlmChatResult(
    String answer, int promptTokens, int completionTokens, int totalTokens) {

  public LlmChatResult {
    if (promptTokens < 0) {
      throw new IllegalArgumentException("promptTokens must be >= 0");
    }
    if (completionTokens < 0) {
      throw new IllegalArgumentException("completionTokens must be >= 0");
    }
    if (totalTokens != promptTokens + completionTokens) {
      throw new IllegalArgumentException("totalTokens must equal promptTokens + completionTokens");
    }
  }

  public static LlmChatResult of(String answer, int promptTokens, int completionTokens) {
    return new LlmChatResult(
        answer, promptTokens, completionTokens, promptTokens + completionTokens);
  }
}
