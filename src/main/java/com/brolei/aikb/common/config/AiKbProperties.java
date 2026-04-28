package com.brolei.aikb.common.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** AI 知识库配置属性. */
@Getter
@Setter
@ConfigurationProperties(prefix = "ai-kb")
public class AiKbProperties {

  private Security security = new Security();
  private Upload upload = new Upload();
  private TextSplitter textSplitter = new TextSplitter();

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

  /** 上传配置. */
  @Getter
  @Setter
  public static class Upload {
    private String rootDir = "./data/uploads";
    private List<String> allowedExt = new ArrayList<>(List.of("md", "txt"));
    private long maxFileSize = 10 * 1024 * 1024; // 10 MB
  }

  /** 文本切分配置. */
  @Getter
  @Setter
  public static class TextSplitter {
    private int chunkSize = 800;
    private int overlap = 120;
  }
}
