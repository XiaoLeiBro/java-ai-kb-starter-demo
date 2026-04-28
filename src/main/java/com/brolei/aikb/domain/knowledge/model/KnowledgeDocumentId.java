package com.brolei.aikb.domain.knowledge.model;

import java.util.Objects;
import java.util.UUID;

/** 知识文档标识值对象. */
public record KnowledgeDocumentId(String value) {

  /** 校验知识文档 ID 值. */
  public KnowledgeDocumentId {
    Objects.requireNonNull(value, "KnowledgeDocumentId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("KnowledgeDocumentId value must not be blank");
    }
  }

  public static KnowledgeDocumentId generate() {
    return new KnowledgeDocumentId(UUID.randomUUID().toString());
  }

  public static KnowledgeDocumentId of(String value) {
    return new KnowledgeDocumentId(value);
  }
}
