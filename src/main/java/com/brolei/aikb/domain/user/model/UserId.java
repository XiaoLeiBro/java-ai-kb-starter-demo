package com.brolei.aikb.domain.user.model;

import java.util.Objects;
import java.util.UUID;

/** 用户标识值对象. */
public record UserId(String value) {

  /** 校验用户 ID 值. */
  public UserId {
    Objects.requireNonNull(value, "UserId value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("UserId value must not be blank");
    }
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID().toString());
  }

  public static UserId of(String value) {
    return new UserId(value);
  }
}
