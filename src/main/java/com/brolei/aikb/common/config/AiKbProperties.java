package com.brolei.aikb.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** AI 知识库配置属性. */
@Getter
@Setter
@ConfigurationProperties(prefix = "ai-kb")
public class AiKbProperties {

  private Security security = new Security();

  /** 安全配置. */
  @Getter
  @Setter
  public static class Security {
    private Jwt jwt = new Jwt();
  }

  /** JWT 配置. */
  @Getter
  @Setter
  public static class Jwt {
    private String secret = "change-me-in-production-default-demo-secret-do-not-use";
    private int expirationMinutes = 10080;
  }
}
