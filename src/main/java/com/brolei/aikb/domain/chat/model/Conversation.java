package com.brolei.aikb.domain.chat.model;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * 对话会话聚合根.
 *
 * <p>管理会话生命周期：创建、归档。title 为空时由应用层填充默认值"新对话"。
 */
public final class Conversation {

  private final ConversationId id;
  private final UserId ownerId;
  private final KnowledgeBaseId knowledgeBaseId;
  private String title;
  private ConversationStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private Conversation(
      ConversationId id,
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      String title,
      ConversationStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.ownerId = ownerId;
    this.knowledgeBaseId = knowledgeBaseId;
    this.title = title;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** 新建对话会话的工厂方法. */
  public static Conversation create(UserId ownerId, KnowledgeBaseId knowledgeBaseId, String title) {
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
    if (title != null && title.length() > 200) {
      throw new IllegalArgumentException("title must not exceed 200 characters");
    }
    Instant now = Instant.now();
    return new Conversation(
        ConversationId.generate(),
        ownerId,
        knowledgeBaseId,
        title,
        ConversationStatus.ACTIVE,
        now,
        now);
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static Conversation rehydrate(
      ConversationId id,
      UserId ownerId,
      KnowledgeBaseId knowledgeBaseId,
      String title,
      ConversationStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new Conversation(id, ownerId, knowledgeBaseId, title, status, createdAt, updatedAt);
  }

  /** 刷新更新时间（有新消息时调用）. */
  public void touch() {
    this.updatedAt = Instant.now();
  }

  /** 归档会话，标记为 ARCHIVED 并更新 updatedAt. */
  public void archive() {
    if (this.status == ConversationStatus.ARCHIVED) {
      return;
    }
    this.status = ConversationStatus.ARCHIVED;
    this.updatedAt = Instant.now();
  }

  public boolean isActive() {
    return this.status == ConversationStatus.ACTIVE;
  }

  public boolean isArchived() {
    return this.status == ConversationStatus.ARCHIVED;
  }

  public boolean hasTitle() {
    return title != null && !title.isBlank();
  }

  public ConversationId id() {
    return id;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public KnowledgeBaseId knowledgeBaseId() {
    return knowledgeBaseId;
  }

  public String title() {
    return title;
  }

  public ConversationStatus status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
