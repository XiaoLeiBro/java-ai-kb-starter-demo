package com.brolei.aikb.infrastructure.security;

import com.brolei.aikb.domain.user.service.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 基于 BCrypt 的密码哈希实现. */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return encoder.matches(rawPassword, encodedPassword);
  }
}
