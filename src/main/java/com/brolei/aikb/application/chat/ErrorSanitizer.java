package com.brolei.aikb.application.chat;

/**
 * LLM 错误消息脱敏工具.
 *
 * <p>去除 API key、Bearer token、内部 URL 等敏感信息，防止泄露到 HTTP 响应和 InvocationLog 中。
 */
final class ErrorSanitizer {

  private ErrorSanitizer() {}

  /** 对错误消息进行脱敏处理. */
  static String sanitize(String message) {
    if (message == null) {
      return "Unknown error";
    }
    String sanitized = message;
    // 去除 sk- 开头的 API key（含 sk-proj- 等变体）
    sanitized = sanitized.replaceAll("sk-[a-zA-Z0-9\\-]{16,}", "sk-***");
    // 去除 Bearer token
    sanitized = sanitized.replaceAll("Bearer\\s+[a-zA-Z0-9._\\-]{16,}", "Bearer ***");
    // 去除 @ 连接的敏感 URL（如 https://user:pass@host）
    sanitized = sanitized.replaceAll("[a-zA-Z0-9._%+-]+:[^\\s]+@", "***:***@");
    return sanitized;
  }

  /** 脱敏并截断至指定长度. */
  static String sanitizeAndTruncate(String message, int maxLength) {
    String sanitized = sanitize(message);
    if (sanitized.length() > maxLength) {
      return sanitized.substring(0, maxLength);
    }
    return sanitized;
  }
}
