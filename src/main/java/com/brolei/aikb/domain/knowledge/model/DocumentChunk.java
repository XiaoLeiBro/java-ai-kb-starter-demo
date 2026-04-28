package com.brolei.aikb.domain.knowledge.model;

import java.time.Instant;
import java.util.Objects;

/**
 * 文档切片实体.
 *
 * <p>代表文档被文本分割器切分后的一个文本片段，属于 KnowledgeDocument 聚合的子实体。
 */
public final class DocumentChunk {

  private final DocumentChunkId id;
  private final KnowledgeBaseId knowledgeBaseId;
  private final KnowledgeDocumentId documentId;
  private final int chunkIndex;
  private final String content;
  private final int charCount;
  private final Instant createdAt;

  private DocumentChunk(
      DocumentChunkId id,
      KnowledgeBaseId knowledgeBaseId,
      KnowledgeDocumentId documentId,
      int chunkIndex,
      String content,
      int charCount,
      Instant createdAt) {
    this.id = id;
    this.knowledgeBaseId = knowledgeBaseId;
    this.documentId = documentId;
    this.chunkIndex = chunkIndex;
    this.content = content;
    this.charCount = charCount;
    this.createdAt = createdAt;
  }

  /** 新建文档切片的工厂方法. */
  public static DocumentChunk create(
      KnowledgeBaseId knowledgeBaseId,
      KnowledgeDocumentId documentId,
      int chunkIndex,
      String content) {
    Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
    Objects.requireNonNull(documentId, "documentId must not be null");
    Objects.requireNonNull(content, "content must not be null");
    if (content.isBlank()) {
      throw new IllegalArgumentException("content must not be blank");
    }
    if (chunkIndex < 0) {
      throw new IllegalArgumentException("chunkIndex must be non-negative");
    }
    int charCount = content.length();
    if (charCount <= 0) {
      throw new IllegalArgumentException("charCount must be positive");
    }
    return new DocumentChunk(
        DocumentChunkId.generate(),
        knowledgeBaseId,
        documentId,
        chunkIndex,
        content,
        charCount,
        Instant.now());
  }

  /** 从持久化数据重建（Repository 实现用）. */
  public static DocumentChunk rehydrate(
      DocumentChunkId id,
      KnowledgeBaseId knowledgeBaseId,
      KnowledgeDocumentId documentId,
      int chunkIndex,
      String content,
      int charCount,
      Instant createdAt) {
    return new DocumentChunk(
        id, knowledgeBaseId, documentId, chunkIndex, content, charCount, createdAt);
  }

  public DocumentChunkId id() {
    return id;
  }

  public KnowledgeBaseId knowledgeBaseId() {
    return knowledgeBaseId;
  }

  public KnowledgeDocumentId documentId() {
    return documentId;
  }

  public int chunkIndex() {
    return chunkIndex;
  }

  public String content() {
    return content;
  }

  public int charCount() {
    return charCount;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
