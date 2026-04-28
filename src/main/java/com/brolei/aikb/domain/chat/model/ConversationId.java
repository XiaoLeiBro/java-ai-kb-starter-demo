package com.brolei.aikb.domain.chat.model;

import java.util.Objects;
import java.util.UUID;

/** 对话会话标识值对象. */
public record ConversationId(String value) {

  public ConversationId {
    Objects.requireNonNull(value, "ConversationId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("ConversationId value must not be blank");
    }
  }

  public static ConversationId generate() {
    return new ConversationId(UUID.randomUUID().toString());
  }

  public static ConversationId of(String value) {
    return new ConversationId(value);
  }
}
