package com.brolei.aikb.interfaces.dto.knowledge;

import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;

/** 文档响应 DTO. */
public record DocumentResponse(
    String id,
    String knowledgeBaseId,
    String originalFilename,
    String contentType,
    long fileSize,
    String status,
    int chunkCount,
    String errorMessage,
    String createdAt,
    String updatedAt) {

  /** 从领域 KnowledgeDocument 对象创建 DocumentResponse. */
  public static DocumentResponse from(KnowledgeDocument doc) {
    return new DocumentResponse(
        doc.id().value(),
        doc.knowledgeBaseId().value(),
        doc.originalFilename(),
        doc.contentType(),
        doc.fileSize(),
        doc.status().name(),
        doc.chunkCount(),
        doc.errorMessage(),
        doc.createdAt().toString(),
        doc.updatedAt().toString());
  }
}
