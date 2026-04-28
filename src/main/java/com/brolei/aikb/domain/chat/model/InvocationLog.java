package com.brolei.aikb.domain.chat.model;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * LLM 调用记录（不可变）.
 *
 * <p>messageId 具有双语义：status=SUCCESS 时指向 assistant Message，status=FAILED 时指向 user Message。
 */
public final class InvocationLog {

  private final InvocationLogId id;
  private final UserId ownerId;
  private final KnowledgeBaseId knowledgeBaseId;
  private final ConversationId conversationId;
  private final MessageId messageId;
  private final String modelName;
  private final int promptTokens;
  private final int completionTokens;
  private final int totalTokens;
  private final long durationMs;
  private final InvocationStatus status;
  private final String errorMessage;
  private final Instant createdAt;

  private InvocationLog(
      InvocationLogId id,
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      ConversationId conversationId,
      MessageId messageId,
      String modelName,
      int promptTokens,
      int completionTokens,
      int totalTokens,
      long durationMs,
      InvocationStatus status,
      String errorMessage,
      Instant createdAt) {
    this.id = id;
    this.ownerId = ownerId;
    this.knowledgeBaseId = knowledgeBaseId;
    this.conversationId = conversationId;
    this.messageId = messageId;
    this.modelName = modelName;
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
    this.durationMs = durationMs;
    this.status = status;
    this.errorMessage = errorMessage;
    this.createdAt = createdAt;
  }

  /** 记录成功调用. */
  public static InvocationLog recordSuccess(
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      ConversationId conversationId,
      MessageId messageId,
      String modelName,
      int promptTokens,
      int completionTokens,
      long durationMs) {
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(conversationId, "conversationId must not be null");
    Objects.requireNonNull(messageId, "messageId must not be null");
    Objects.requireNonNull(modelName, "modelName must not be null");
    if (promptTokens < 0) {
      throw new IllegalArgumentException("promptTokens must be >= 0");
    }
    if (completionTokens < 0) {
      throw new IllegalArgumentException("completionTokens must be >= 0");
    }
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must be >= 0");
    }
    return new InvocationLog(
        InvocationLogId.generate(),
        ownerId,
        knowledgeBaseId,
        conversationId,
        messageId,
        modelName,
        promptTokens,
        completionTokens,
        promptTokens + completionTokens,
        durationMs,
        InvocationStatus.SUCCESS,
        null,
        Instant.now());
  }

  /** 记录失败调用. */
  public static InvocationLog recordFailure(
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      ConversationId conversationId,
      MessageId messageId,
      String modelName,
      long durationMs,
      String errorMessage) {
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(conversationId, "conversationId must not be null");
    Objects.requireNonNull(messageId, "messageId must not be null");
    Objects.requireNonNull(modelName, "modelName must not be null");
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must be >= 0");
    }
    if (errorMessage == null || errorMessage.isBlank()) {
      throw new IllegalArgumentException(
          "errorMessage must not be null or blank for FAILED invocation");
    }
    return new InvocationLog(
        InvocationLogId.generate(),
        ownerId,
        knowledgeBaseId,
        conversationId,
        messageId,
        modelName,
        0,
        0,
        0,
        durationMs,
        InvocationStatus.FAILED,
        errorMessage,
        Instant.now());
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static InvocationLog rehydrate(
      InvocationLogId id,
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      ConversationId conversationId,
      MessageId messageId,
      String modelName,
      int promptTokens,
      int completionTokens,
      int totalTokens,
      long durationMs,
      InvocationStatus status,
      String errorMessage,
      Instant createdAt) {
    return new InvocationLog(
        id,
        ownerId,
        knowledgeBaseId,
        conversationId,
        messageId,
        modelName,
        promptTokens,
        completionTokens,
        totalTokens,
        durationMs,
        status,
        errorMessage,
        createdAt);
  }

  public InvocationLogId id() {
    return id;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public KnowledgeBaseId knowledgeBaseId() {
    return knowledgeBaseId;
  }

  public ConversationId conversationId() {
    return conversationId;
  }

  public MessageId messageId() {
    return messageId;
  }

  public String modelName() {
    return modelName;
  }

  public int promptTokens() {
    return promptTokens;
  }

  public int completionTokens() {
    return completionTokens;
  }

  public int totalTokens() {
    return totalTokens;
  }

  public long durationMs() {
    return durationMs;
  }

  public InvocationStatus status() {
    return status;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
