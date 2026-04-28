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
}
