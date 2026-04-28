package com.brolei.aikb.infrastructure.security;

import com.brolei.aikb.common.config.AiKbProperties;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 防止在生产环境中使用默认 JWT 密钥. */
@Component
public class JwtSecretGuard implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(JwtSecretGuard.class);
  private static final String DEFAULT_SECRET =
      "change-me-in-production-default-demo-secret-do-not-use";

  private final Environment env;
  private final AiKbProperties props;

  public JwtSecretGuard(Environment env, AiKbProperties props) {
    this.env = env;
    this.props = props;
  }

  @Override
  public void run(ApplicationArguments args) {
    String secret = props.getSecurity().getJwt().getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT Secret must not be blank.");
    }
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT Secret must be at least 32 characters.");
    }

    boolean usingDefault = DEFAULT_SECRET.equals(secret);
    List<String> profiles = Arrays.asList(env.getActiveProfiles());
    if (!usingDefault) {
      return;
    }

    if (profiles.contains("prod") || profiles.contains("production")) {
      throw new IllegalStateException(
          "Default JWT Secret detected in production profile. "
              + "Please configure ai-kb.security.jwt.secret before starting.");
    }
    if (profiles.contains("dev")) {
      return;
    }

    log.warn(
        "\n"
            + "****************************************************************************\n"
            + "* 当前正在使用默认 JWT Secret，仅适用于本地体验。                               *\n"
            + "* 部署到公网前必须设置环境变量 AI_KB_JWT_SECRET（长度 ≥ 32 字符）。   *\n"
            + "****************************************************************************");
  }
}
