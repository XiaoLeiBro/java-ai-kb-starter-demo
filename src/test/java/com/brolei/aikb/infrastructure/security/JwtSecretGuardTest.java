package com.brolei.aikb.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brolei.aikb.common.config.AiKbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtSecretGuardTest {

  @Test
  void runShouldRejectBlankSecret() {
    AiKbProperties props = propsWithSecret(" ");
    JwtSecretGuard guard = new JwtSecretGuard(new MockEnvironment(), props);

    assertThrows(IllegalStateException.class, () -> guard.run(null));
  }

  @Test
  void runShouldRejectShortSecret() {
    AiKbProperties props = propsWithSecret("short-secret");
    JwtSecretGuard guard = new JwtSecretGuard(new MockEnvironment(), props);

    assertThrows(IllegalStateException.class, () -> guard.run(null));
  }

  @Test
  void runShouldRejectDefaultSecretInProductionProfile() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");
    JwtSecretGuard guard = new JwtSecretGuard(env, new AiKbProperties());

    assertThrows(IllegalStateException.class, () -> guard.run(null));
  }

  @Test
  void runShouldAllowCustomSecret() {
    AiKbProperties props = propsWithSecret("custom-jwt-secret-with-at-least-32-chars");
    JwtSecretGuard guard = new JwtSecretGuard(new MockEnvironment(), props);

    assertDoesNotThrow(() -> guard.run(null));
  }

  private static AiKbProperties propsWithSecret(String secret) {
    AiKbProperties props = new AiKbProperties();
    props.getSecurity().getJwt().setSecret(secret);
    return props;
  }
}
