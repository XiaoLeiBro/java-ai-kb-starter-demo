package com.brolei.aikb.domain.knowledge.model;

import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * 知识文档聚合根.
 *
 * <p>代表上传到知识库中的一份文档，管理从上传到索引完成的完整生命周期。
 */
public final class KnowledgeDocument {

  private final KnowledgeDocumentId id;
  private final KnowledgeBaseId knowledgeBaseId;
  private final UserId ownerId;
  private final String originalFilename;
  private final String storagePath;
  private final String contentType;
  private final long fileSize;
  private DocumentStatus status;
  private int chunkCount;
  private String errorMessage;
  private final Instant createdAt;
  private Instant updatedAt;

  private KnowledgeDocument(
      KnowledgeDocumentId id,
      KnowledgeBaseId knowledgeBaseId,
      UserId ownerId,
      String originalFilename,
      String storagePath,
      String contentType,
      long fileSize,
      DocumentStatus status,
      int chunkCount,
      String errorMessage,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.knowledgeBaseId = knowledgeBaseId;
    this.ownerId = ownerId;
    this.originalFilename = originalFilename;
    this.storagePath = storagePath;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.status = status;
    this.chunkCount = chunkCount;
    this.errorMessage = errorMessage;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** 新建知识文档的工厂方法. */
  public static KnowledgeDocument create(
      KnowledgeBaseId knowledgeBaseId,
      UserId ownerId,
      String originalFilename,
      String storagePath,
      String contentType,
      long fileSize) {
    return create(
        KnowledgeDocumentId.generate(),
        knowledgeBaseId,
        ownerId,
        originalFilename,
        storagePath,
        contentType,
        fileSize);
  }

  /** 使用指定文档 ID 新建知识文档的工厂方法. */
  public static KnowledgeDocument create(
      KnowledgeDocumentId id,
      KnowledgeBaseId knowledgeBaseId,
      UserId ownerId,
      String originalFilename,
      String storagePath,
      String contentType,
      long fileSize) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(originalFilename, "originalFilename must not be null");
    Objects.requireNonNull(storagePath, "storagePath must not be null");
    if (fileSize <= 0) {
      throw new IllegalArgumentException("fileSize must be positive");
    }
    Instant now = Instant.now();
    return new KnowledgeDocument(
        id,
        knowledgeBaseId,
        ownerId,
        originalFilename,
        storagePath,
        contentType,
        fileSize,
        DocumentStatus.UPLOADED,
        0,
        null,
        now,
        now);
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static KnowledgeDocument rehydrate(
      KnowledgeDocumentId id,
      KnowledgeBaseId knowledgeBaseId,
      UserId ownerId,
      String originalFilename,
      String storagePath,
      String contentType,
      long fileSize,
      DocumentStatus status,
      int chunkCount,
      String errorMessage,
      Instant createdAt,
      Instant updatedAt) {
    return new KnowledgeDocument(
        id,
        knowledgeBaseId,
        ownerId,
        originalFilename,
        storagePath,
        contentType,
        fileSize,
        status,
        chunkCount,
        errorMessage,
        createdAt,
        updatedAt);
  }

  /** 标记文档进入索引中状态. */
  public void markAsIndexing() {
    if (this.status == DocumentStatus.INDEXING) {
      return;
    }
    this.status = DocumentStatus.INDEXING;
    this.updatedAt = Instant.now();
  }

  /** 标记文档索引完成. */
  public void markAsReady(int chunkCount) {
    if (chunkCount < 0) {
      throw new IllegalArgumentException("chunkCount must be non-negative");
    }
    this.status = DocumentStatus.READY;
    this.chunkCount = chunkCount;
    this.updatedAt = Instant.now();
  }

  /** 标记文档索引失败. */
  public void markAsFailed(String errorMessage) {
    Objects.requireNonNull(errorMessage, "errorMessage must not be null");
    this.status = DocumentStatus.FAILED;
    this.errorMessage = errorMessage;
    this.updatedAt = Instant.now();
  }

  public KnowledgeDocumentId id() {
    return id;
  }

  public KnowledgeBaseId knowledgeBaseId() {
    return knowledgeBaseId;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public String originalFilename() {
    return originalFilename;
  }

  public String storagePath() {
    return storagePath;
  }

  public String contentType() {
    return contentType;
  }

  public long fileSize() {
    return fileSize;
  }

  public DocumentStatus status() {
    return status;
  }

  public int chunkCount() {
    return chunkCount;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
