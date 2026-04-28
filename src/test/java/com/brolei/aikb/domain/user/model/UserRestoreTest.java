package com.brolei.aikb.domain.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserRestoreTest {

  @Test
  void restoreShouldReturnUserWithExactFields() {
    UserId id = UserId.of("test-uuid-123");
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

    User user =
        User.restore(
            id,
            "historic_user",
            "hashed_password_value",
            "old@example.com",
            UserStatus.DISABLED,
            createdAt,
            updatedAt);

    assertEquals(id, user.id());
    assertEquals("historic_user", user.username());
    assertEquals("hashed_password_value", user.passwordHash());
    assertEquals("old@example.com", user.email());
    assertEquals(UserStatus.DISABLED, user.status());
    assertEquals(createdAt, user.createdAt());
    assertEquals(updatedAt, user.updatedAt());
  }

  @Test
  void restoreShouldNotTriggerValidationForInvalidHistoricData() {
    // A historic user with a 2-char username that would fail register() validation
    UserId id = UserId.of("uuid-short-name");
    Instant now = Instant.now();

    User user = User.restore(id, "ab", "hash", null, UserStatus.ACTIVE, now, now);

    assertEquals("ab", user.username());
    assertEquals("hash", user.passwordHash());
  }

  @Test
  void restoreShouldNotRegenerateId() {
    UserId originalId = UserId.of("specific-id");
    Instant now = Instant.now();

    User user = User.restore(originalId, "alice", "hash", null, UserStatus.ACTIVE, now, now);

    assertEquals("specific-id", user.id().value());
  }

  @Test
  void restoreShouldNotModifyTimestamps() {
    Instant createdAt = Instant.parse("2025-06-15T10:30:00Z");
    Instant updatedAt = Instant.parse("2025-12-20T14:00:00Z");

    User user =
        User.restore(
            UserId.of("id"), "alice", "hash", null, UserStatus.ACTIVE, createdAt, updatedAt);

    assertEquals(createdAt, user.createdAt());
    assertEquals(updatedAt, user.updatedAt());
  }
}
