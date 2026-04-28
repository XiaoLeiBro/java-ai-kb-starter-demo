package com.brolei.aikb.domain.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.domain.user.service.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

  private PasswordHasher hasher;

  @BeforeEach
  void setUp() {
    hasher =
        new PasswordHasher() {
          @Override
          public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
          }

          @Override
          public boolean matches(String rawPassword, String encodedPassword) {
            return encodedPassword.equals("hashed:" + rawPassword);
          }
        };
  }

  @Test
  void registerShouldSucceedWithValidInput() {
    User user = User.register("alice", "password123", "alice@example.com", hasher);

    assertNotNull(user.id());
    assertNotNull(user.id().value());
    assertFalse(user.id().value().isBlank());
    assertEquals("alice", user.username());
    assertEquals("hashed:password123", user.passwordHash());
    assertEquals("alice@example.com", user.email());
    assertEquals(UserStatus.ACTIVE, user.status());
    assertNotNull(user.createdAt());
    assertNotNull(user.updatedAt());
  }

  @Test
  void registerShouldAllowNullEmail() {
    User user = User.register("bob", "password123", null, hasher);

    assertNull(user.email());
    assertEquals("bob", user.username());
  }

  @Test
  void registerShouldRejectUsernameWithSpaces() {
    assertThrows(
        IllegalArgumentException.class, () -> User.register("al ice", "password123", null, hasher));
  }

  @Test
  void registerShouldRejectShortUsername() {
    assertThrows(
        IllegalArgumentException.class, () -> User.register("ab", "password123", null, hasher));
  }

  @Test
  void registerShouldRejectLongUsername() {
    String longName = "a".repeat(21);
    assertThrows(
        IllegalArgumentException.class, () -> User.register(longName, "password123", null, hasher));
  }

  @Test
  void registerShouldRejectUsernameWithSpecialChars() {
    assertThrows(
        IllegalArgumentException.class, () -> User.register("alice@", "password123", null, hasher));
  }

  @Test
  void registerShouldRejectShortPassword() {
    assertThrows(
        IllegalArgumentException.class, () -> User.register("alice", "1234567", null, hasher));
  }

  @Test
  void registerShouldAcceptPasswordExactly8Chars() {
    User user = User.register("alice", "12345678", null, hasher);
    assertEquals("hashed:12345678", user.passwordHash());
  }

  @Test
  void registerShouldAcceptNullEmail() {
    User user = User.register("alice", "password123", null, hasher);
    assertNull(user.email());
  }

  @Test
  void verifyPasswordShouldReturnTrueForCorrectPassword() {
    User user = User.register("alice", "password123", null, hasher);

    assertTrue(user.verifyPassword("password123", hasher));
  }

  @Test
  void verifyPasswordShouldReturnFalseForWrongPassword() {
    User user = User.register("alice", "password123", null, hasher);

    assertFalse(user.verifyPassword("wrongpassword", hasher));
  }

  @Test
  void changePasswordShouldSucceedWithCorrectOldPassword() {
    User user = User.register("alice", "password123", null, hasher);

    user.changePassword("password123", "newpassword123", hasher);

    assertTrue(user.verifyPassword("newpassword123", hasher));
    assertFalse(user.verifyPassword("password123", hasher));
  }

  @Test
  void changePasswordShouldThrowWithWrongOldPassword() {
    User user = User.register("alice", "password123", null, hasher);

    assertThrows(
        IllegalArgumentException.class, () -> user.changePassword("wrong", "newpassword", hasher));
  }

  @Test
  void changePasswordShouldRejectShortNewPassword() {
    User user = User.register("alice", "password123", null, hasher);

    assertThrows(
        IllegalArgumentException.class, () -> user.changePassword("password123", "short", hasher));
  }

  @Test
  void disableShouldSetStatusToDisabled() {
    User user = User.register("alice", "password123", null, hasher);

    user.disable();

    assertEquals(UserStatus.DISABLED, user.status());
  }

  @Test
  void registerShouldGenerateUniqueIds() {
    User user1 = User.register("alice", "password123", null, hasher);
    User user2 = User.register("bob", "password123", null, hasher);

    assertNotEquals(user1.id(), user2.id());
  }
}
