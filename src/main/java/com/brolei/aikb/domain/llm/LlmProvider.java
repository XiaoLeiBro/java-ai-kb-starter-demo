package com.brolei.aikb.domain.llm;

/**
 * 大语言模型提供者接口.
 *
 * <p>领域层只表达"需要与大模型对话"的需求，不依赖任何具体 LLM SDK。
 */
public interface LlmProvider {

  /**
   * 发送对话请求并获取回复.
   *
   * @param systemPrompt 系统提示词
   * @param userMessage 用户消息
   * @return 模型回复文本
   */
  String chat(String systemPrompt, String userMessage);

  /**
   * 发送对话请求并获取回复及 token 用量.
   *
   * <p>默认实现委托给 {@link #chat}，token 计数为 0，保持向后兼容.
   *
   * @param systemPrompt 系统提示词
   * @param userMessage 用户消息
   * @return 包含回答和 token 用量的结果
   */
  default LlmChatResult chatWithUsage(String systemPrompt, String userMessage) {
    String answer = chat(systemPrompt, userMessage);
    return LlmChatResult.of(answer, 0, 0);
  }
}
