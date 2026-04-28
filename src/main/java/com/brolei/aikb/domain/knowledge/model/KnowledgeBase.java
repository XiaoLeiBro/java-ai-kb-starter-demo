package com.brolei.aikb.domain.knowledge.model;

import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * 知识库聚合根.
 *
 * <p>示例实现：展示 DDD 聚合根的最小形态。
 *
 * <ul>
 *   <li>状态变更必须通过业务方法（{@code rename}、{@code deactivate}）
 *   <li>不提供 public setter
 *   <li>不依赖任何 Spring / MyBatis-Plus 注解（持久化映射放到 infrastructure.persistence）
 * </ul>
 *
 * <p>后续扩展：添加 documents 集合、owner、可见范围等，视业务演进。
 */
public final class KnowledgeBase {

  private final KnowledgeBaseId id;
  private final UserId ownerId;
  private String name;
  private String description;
  private KnowledgeBaseStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private KnowledgeBase(
      KnowledgeBaseId id,
      UserId ownerId,
      String name,
      String description,
      KnowledgeBaseStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.ownerId = ownerId;
    this.name = name;
    this.description = description;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** 新建知识库的工厂方法. */
  public static KnowledgeBase create(UserId ownerId, String name, String description) {
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    Instant now = Instant.now();
    return new KnowledgeBase(
        KnowledgeBaseId.generate(),
        ownerId,
        name.trim(),
        description,
        KnowledgeBaseStatus.ACTIVE,
        now,
        now);
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static KnowledgeBase rehydrate(
      KnowledgeBaseId id,
      UserId ownerId,
      String name,
      String description,
      KnowledgeBaseStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new KnowledgeBase(id, ownerId, name, description, status, createdAt, updatedAt);
  }

  /** 重命名知识库. */
  public void rename(String newName) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    this.name = newName.trim();
    this.updatedAt = Instant.now();
  }

  /** 停用（归档）知识库. */
  public void deactivate() {
    if (this.status == KnowledgeBaseStatus.ARCHIVED) {
      return;
    }
    this.status = KnowledgeBaseStatus.ARCHIVED;
    this.updatedAt = Instant.now();
  }

  public boolean isActive() {
    return this.status == KnowledgeBaseStatus.ACTIVE;
  }

  public boolean isArchived() {
    return this.status == KnowledgeBaseStatus.ARCHIVED;
  }

  public KnowledgeBaseId id() {
    return id;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public KnowledgeBaseStatus status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
