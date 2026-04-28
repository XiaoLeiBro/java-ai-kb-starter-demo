package com.brolei.aikb.domain.knowledge.model;

import java.util.Objects;
import java.util.UUID;

/** 文档切片标识值对象. */
public record DocumentChunkId(String value) {

  /** 校验文档切片 ID 值. */
  public DocumentChunkId {
    Objects.requireNonNull(value, "DocumentChunkId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("DocumentChunkId value must not be blank");
    }
  }

  public static DocumentChunkId generate() {
    return new DocumentChunkId(UUID.randomUUID().toString());
  }

  public static DocumentChunkId of(String value) {
    return new DocumentChunkId(value);
  }
}
