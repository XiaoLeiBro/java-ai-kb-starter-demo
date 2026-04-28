package com.brolei.aikb.interfaces.dto.knowledge;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;

/** 知识库响应 DTO. */
public record KnowledgeBaseResponse(
    String id, String name, String description, String status, String createdAt, String updatedAt) {

  /** 从领域 KnowledgeBase 对象创建 KnowledgeBaseResponse. */
  public static KnowledgeBaseResponse from(KnowledgeBase kb) {
    return new KnowledgeBaseResponse(
        kb.id().value(),
        kb.name(),
        kb.description(),
        kb.status().name(),
        kb.createdAt().toString(),
        kb.updatedAt().toString());
  }
}
