package com.brolei.aikb.interfaces.rest;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接入层示例：健康检查.
 *
 * <p>保留这个类是为了让项目"启动即可响应请求"，并给后续开发者展示 接入层应该如何组织（Controller 不写业务，只做编排调用应用层）。
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  /** 返回服务状态的健康检查接口. */
  @GetMapping
  public Map<String, Object> health() {
    return Map.of(
        "status", "UP",
        "service", "ai-kb-demo-server",
        "version", "0.1.0-SNAPSHOT",
        "timestamp", OffsetDateTime.now().toString());
  }
}
