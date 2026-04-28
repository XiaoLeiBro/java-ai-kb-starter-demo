package com.brolei.aikb.application.chat;

import com.brolei.aikb.domain.knowledge.model.ChatAnswer;

/**
 * 聊天结果（应用层）.
 *
 * <p>扩展 {@link ChatAnswer}，包含会话模式下产生的消息和日志 ID。
 */
public record ChatResult(
    ChatAnswer chatAnswer,
    String userMessageId,
    String assistantMessageId,
    String invocationLogId) {

  /** v0.2 兼容构造（不传入 conversationId 时使用）. */
  public ChatResult(ChatAnswer chatAnswer) {
    this(chatAnswer, null, null, null);
  }
}
