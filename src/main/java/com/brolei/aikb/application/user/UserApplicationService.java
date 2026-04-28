package com.brolei.aikb.application.user;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.domain.user.model.UserStatus;
import com.brolei.aikb.domain.user.repository.UserRepository;
import com.brolei.aikb.domain.user.service.PasswordHasher;
import com.brolei.aikb.domain.user.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户相关用例的应用服务. */
@Service
public class UserApplicationService {

  private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final TokenService tokenService;

  /** 构造用户应用服务. */
  public UserApplicationService(
      UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.tokenService = tokenService;
  }

  /** 使用给定凭据注册新用户. */
  @Transactional
  public User register(String username, String rawPassword, String email) {
    if (userRepository.existsByUsername(username)) {
      throw new BusinessException(ErrorCode.USERNAME_EXISTS);
    }
    User user = User.register(username, rawPassword, email, passwordHasher);
    userRepository.save(user);
    log.info("User registered: {}", user.id().value());
    return user;
  }

  /** 认证用户并返回包含 JWT 令牌的登录结果. */
  @Transactional(readOnly = true)
  public LoginResult login(String username, String rawPassword) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

    if (user.status() != UserStatus.ACTIVE) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    if (!user.verifyPassword(rawPassword, passwordHasher)) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String token = tokenService.issue(user.id(), user.username());
    return new LoginResult(user, token);
  }

  /** 通过用户 ID 查找当前用户. */
  @Transactional(readOnly = true)
  public User currentUser(UserId userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
  }
}
