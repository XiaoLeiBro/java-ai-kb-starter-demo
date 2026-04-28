package com.brolei.aikb.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.domain.user.model.UserStatus;
import com.brolei.aikb.domain.user.repository.UserRepository;
import com.brolei.aikb.domain.user.service.PasswordHasher;
import com.brolei.aikb.domain.user.service.TokenService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

  private FakeUserRepository userRepository;
  private FakePasswordHasher passwordHasher;
  private FakeTokenService tokenService;
  private UserApplicationService service;

  @BeforeEach
  void setUp() {
    userRepository = new FakeUserRepository();
    passwordHasher = new FakePasswordHasher();
    tokenService = new FakeTokenService();
    service = new UserApplicationService(userRepository, passwordHasher, tokenService);
  }

  @Test
  void registerShouldSucceed() {
    User result = service.register("alice", "password123", "alice@example.com");

    assertNotNull(result.id());
    assertEquals("alice", result.username());
    assertEquals("alice@example.com", result.email());
    assertEquals(1, userRepository.saveCount);
  }

  @Test
  void registerShouldThrowWhenUsernameExists() {
    userRepository.save(existingUser("alice", UserStatus.ACTIVE));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.register("alice", "password123", null));

    assertEquals(ErrorCode.USERNAME_EXISTS, ex.errorCode());
    assertEquals(1, userRepository.saveCount);
  }

  @Test
  void loginShouldReturnTokenAndUser() {
    User user = existingUser("alice", UserStatus.ACTIVE);
    userRepository.save(user);

    LoginResult result = service.login("alice", "password123");

    assertEquals("jwt-token", result.token());
    assertEquals("alice", result.user().username());
    assertEquals(1, passwordHasher.matchesCount);
    assertEquals(1, tokenService.issueCount);
  }

  @Test
  void loginShouldThrowWhenUserNotFound() {
    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.login("unknown", "password"));

    assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode());
  }

  @Test
  void loginShouldThrowWhenPasswordWrong() {
    userRepository.save(existingUser("alice", UserStatus.ACTIVE));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.login("alice", "wrong"));

    assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode());
    assertEquals(1, passwordHasher.matchesCount);
    assertEquals(0, tokenService.issueCount);
  }

  @Test
  void loginShouldThrowWhenUserDisabled() {
    userRepository.save(existingUser("alice", UserStatus.DISABLED));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.login("alice", "password123"));

    assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode());
    assertEquals(0, passwordHasher.matchesCount);
    assertEquals(0, tokenService.issueCount);
  }

  @Test
  void currentUserShouldReturnUser() {
    User user = existingUser("alice", UserStatus.ACTIVE);
    userRepository.save(user);

    User result = service.currentUser(user.id());

    assertEquals("alice", result.username());
    assertEquals(user.id(), result.id());
  }

  @Test
  void currentUserShouldThrowWhenNotFound() {
    UserId userId = UserId.of("unknown");

    BusinessException ex = assertThrows(BusinessException.class, () -> service.currentUser(userId));

    assertEquals(ErrorCode.UNAUTHORIZED, ex.errorCode());
  }

  private static User existingUser(String username, UserStatus status) {
    return User.restore(
        UserId.generate(),
        username,
        "hashed:password123",
        username + "@example.com",
        status,
        Instant.now(),
        Instant.now());
  }

  private static class FakeUserRepository implements UserRepository {
    private final Map<UserId, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private int saveCount;

    @Override
    public Optional<User> findById(UserId id) {
      return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
      return Optional.ofNullable(usersByUsername.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
      return usersByUsername.containsKey(username);
    }

    @Override
    public User save(User user) {
      usersById.put(user.id(), user);
      usersByUsername.put(user.username(), user);
      saveCount++;
      return user;
    }
  }

  private static class FakePasswordHasher implements PasswordHasher {
    private int matchesCount;

    @Override
    public String hash(String rawPassword) {
      return "hashed:" + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
      matchesCount++;
      return encodedPassword.equals(hash(rawPassword));
    }
  }

  private static class FakeTokenService implements TokenService {
    private int issueCount;

    @Override
    public String issue(UserId userId, String username) {
      issueCount++;
      return "jwt-token";
    }
  }
}
