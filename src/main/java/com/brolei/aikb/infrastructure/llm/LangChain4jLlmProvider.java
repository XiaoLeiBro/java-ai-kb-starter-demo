package com.brolei.aikb.infrastructure.llm;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.llm.LlmProvider;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

/** 基于 LangChain4j 的大语言模型提供者实现. */
@Component
public class LangChain4jLlmProvider implements LlmProvider {

  private final ChatModel chatModel;

  /** 注入 LangChain4j 自动配置的 ChatModel Bean. */
  public LangChain4jLlmProvider(ChatModel chatModel) {
    this.chatModel = chatModel;
  }

  /**
   * 发起一次聊天请求.
   *
   * <p>构造 SystemMessage 和 UserMessage 后调用 ChatModel， 返回 AiMessage 的文本内容。如果 API Key 缺失或调用失败，抛出
   * BusinessException。
   *
   * @param systemPrompt 系统提示词
   * @param userMessage 用户输入消息
   * @return 模型生成的回复文本
   */
  @Override
  public String chat(String systemPrompt, String userMessage) {
    try {
      SystemMessage sysMsg = SystemMessage.from(systemPrompt);
      UserMessage usrMsg = UserMessage.from(userMessage);
      AiMessage aiMessage = chatModel.chat(sysMsg, usrMsg).aiMessage();
      return aiMessage.text();
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "LLM 调用失败: " + e.getMessage());
    }
  }
}
