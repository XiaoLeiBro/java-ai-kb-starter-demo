package com.brolei.aikb.domain.chat.model;

import java.time.Instant;
import java.util.Objects;

/**
 * 对话消息实体（不可变，append-only）.
 *
 * <p>消息不设独立 status 字段，可见性由关联的 Conversation status 控制。
 */
public final class Message {

  private final MessageId id;
  private final ConversationId conversationId;
  private final MessageRole role;
  private final String content;
  private final Instant createdAt;

  private Message(
      MessageId id,
      ConversationId conversationId,
      MessageRole role,
      String content,
      Instant createdAt) {
    this.id = id;
    this.conversationId = conversationId;
    this.role = role;
    this.content = content;
    this.createdAt = createdAt;
  }

  /** 新建消息的工厂方法. */
  public static Message create(ConversationId conversationId, MessageRole role, String content) {
    Objects.requireNonNull(conversationId, "conversationId must not be null");
    Objects.requireNonNull(role, "role must not be null");
    if (content == null) {
      throw new IllegalArgumentException("content must not be null");
    }
    if (role == MessageRole.USER && content.isBlank()) {
      throw new IllegalArgumentException("user message content must not be blank");
    }
    if (content.length() > 50_000) {
      throw new IllegalArgumentException("content must not exceed 50,000 characters");
    }
    return new Message(MessageId.generate(), conversationId, role, content, Instant.now());
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static Message rehydrate(
      MessageId id,
      ConversationId conversationId,
      MessageRole role,
      String content,
      Instant createdAt) {
    return new Message(id, conversationId, role, content, createdAt);
  }

  public MessageId id() {
    return id;
  }

  public ConversationId conversationId() {
    return conversationId;
  }

  public MessageRole role() {
    return role;
  }

  public String content() {
    return content;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
