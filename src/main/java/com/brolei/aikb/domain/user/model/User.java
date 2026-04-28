package com.brolei.aikb.domain.user.model;

import com.brolei.aikb.domain.user.service.PasswordHasher;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/** 用户聚合根. */
public final class User {

  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

  private final UserId id;
  private final String username;
  private String passwordHash;
  private String email;
  private UserStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private User(
      UserId id,
      String username,
      String passwordHash,
      String email,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** 注册新用户. */
  public static User register(
      String username, String rawPassword, String email, PasswordHasher hasher) {
    validateUsername(username);
    validatePasswordStrength(rawPassword);
    Instant now = Instant.now();
    return new User(
        UserId.generate(), username, hasher.hash(rawPassword), email, UserStatus.ACTIVE, now, now);
  }

  /** 从持久化数据重建用户对象. 仅供 Repository 或 Assembler 使用，不会触发业务校验、 重新生成 ID、重新哈希密码或修改时间戳. */
  public static User restore(
      UserId id,
      String username,
      String passwordHash,
      String email,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new User(id, username, passwordHash, email, status, createdAt, updatedAt);
  }

  public boolean verifyPassword(String rawPassword, PasswordHasher hasher) {
    return hasher.matches(rawPassword, this.passwordHash);
  }

  /** 修改用户密码. */
  public void changePassword(String oldRawPassword, String newRawPassword, PasswordHasher hasher) {
    if (!verifyPassword(oldRawPassword, hasher)) {
      throw new IllegalArgumentException("Old password does not match");
    }
    validatePasswordStrength(newRawPassword);
    this.passwordHash = hasher.hash(newRawPassword);
    this.updatedAt = Instant.now();
  }

  public void disable() {
    this.status = UserStatus.DISABLED;
    this.updatedAt = Instant.now();
  }

  private static void validateUsername(String username) {
    Objects.requireNonNull(username, "username must not be null");
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw new IllegalArgumentException(
          "username must be 3-20 characters, alphanumeric and underscore only");
    }
  }

  private static void validatePasswordStrength(String password) {
    Objects.requireNonNull(password, "password must not be null");
    if (password.length() < 8) {
      throw new IllegalArgumentException("password must be at least 8 characters");
    }
  }

  public UserId id() {
    return id;
  }

  public String username() {
    return username;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public String email() {
    return email;
  }

  public UserStatus status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
