package com.brolei.aikb.infrastructure.llm;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.llm.LlmChatResult;
import com.brolei.aikb.domain.llm.LlmProvider;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
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
      ChatResponse response = doChat(systemPrompt, userMessage);
      return response.aiMessage().text();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "LLM 调用失败: " + e.getMessage());
    }
  }

  @Override
  public LlmChatResult chatWithUsage(String systemPrompt, String userMessage) {
    try {
      ChatResponse response = doChat(systemPrompt, userMessage);
      String answer = response.aiMessage().text();
      TokenUsage tokenUsage = response.tokenUsage();
      int promptTokens =
          tokenUsage != null && tokenUsage.inputTokenCount() != null
              ? tokenUsage.inputTokenCount()
              : 0;
      int completionTokens =
          tokenUsage != null && tokenUsage.outputTokenCount() != null
              ? tokenUsage.outputTokenCount()
              : 0;
      return LlmChatResult.of(answer, promptTokens, completionTokens);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "LLM 调用失败: " + e.getMessage());
    }
  }

  private ChatResponse doChat(String systemPrompt, String userMessage) {
    SystemMessage sysMsg = SystemMessage.from(systemPrompt);
    UserMessage usrMsg = UserMessage.from(userMessage);
    return chatModel.chat(sysMsg, usrMsg);
  }
}
