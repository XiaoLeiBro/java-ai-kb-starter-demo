package com.brolei.aikb.domain.chat.model;

import java.util.Objects;
import java.util.UUID;

/** 对话消息标识值对象. */
public record MessageId(String value) {

  public MessageId {
    Objects.requireNonNull(value, "MessageId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("MessageId value must not be blank");
    }
  }

  public static MessageId generate() {
    return new MessageId(UUID.randomUUID().toString());
  }

  public static MessageId of(String value) {
    return new MessageId(value);
  }
}
