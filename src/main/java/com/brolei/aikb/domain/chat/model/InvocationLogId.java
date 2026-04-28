package com.brolei.aikb.domain.chat.model;

import java.util.Objects;
import java.util.UUID;

/** 调用记录标识值对象. */
public record InvocationLogId(String value) {

  public InvocationLogId {
    Objects.requireNonNull(value, "InvocationLogId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("InvocationLogId value must not be blank");
    }
  }

  public static InvocationLogId generate() {
    return new InvocationLogId(UUID.randomUUID().toString());
  }

  public static InvocationLogId of(String value) {
    return new InvocationLogId(value);
  }
}
