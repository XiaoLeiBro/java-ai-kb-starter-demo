package com.brolei.aikb.domain.knowledge.model;

import java.util.Objects;
import java.util.UUID;

/** 知识库标识值对象. */
public record KnowledgeBaseId(String value) {

  /** 校验知识库 ID 值. */
  public KnowledgeBaseId {
    Objects.requireNonNull(value, "KnowledgeBaseId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("KnowledgeBaseId value must not be blank");
    }
  }

  public static KnowledgeBaseId generate() {
    return new KnowledgeBaseId(UUID.randomUUID().toString());
  }

  public static KnowledgeBaseId of(String value) {
    return new KnowledgeBaseId(value);
  }
}
