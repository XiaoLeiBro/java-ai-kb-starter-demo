package com.brolei.aikb.common.exception;

/** 应用错误码枚举. */
public enum ErrorCode {
  USERNAME_EXISTS(409, "USERNAME_EXISTS", "Username already exists"),
  INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "Invalid username or password"),
  UNAUTHORIZED(401, "UNAUTHORIZED", "Authentication required"),
  VALIDATION_ERROR(400, "VALIDATION_ERROR", "Validation failed"),
  UNSUPPORTED_FILE_TYPE(400, "UNSUPPORTED_FILE_TYPE", "Unsupported file type"),
  NOT_FOUND(404, "NOT_FOUND", "Resource not found"),
  LLM_PROVIDER_ERROR(502, "LLM_PROVIDER_ERROR", "LLM provider call failed"),
  CONVERSATION_ARCHIVED(
      409, "CONVERSATION_ARCHIVED", "Cannot send messages to an archived conversation"),
  CONVERSATION_GONE(410, "CONVERSATION_GONE", "Conversation is no longer available"),
  KB_MISMATCH(400, "KB_MISMATCH", "Knowledge base does not match conversation"),
  KNOWLEDGE_BASE_ARCHIVED(400, "KNOWLEDGE_BASE_ARCHIVED", "Knowledge base is archived"),
  INTERNAL_ERROR(500, "INTERNAL_ERROR", "Internal server error");

  private final int httpStatus;
  private final String code;
  private final String message;

  /** 构造错误码. */
  ErrorCode(int httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  /** 对应的 HTTP 状态码. */
  public int httpStatus() {
    return httpStatus;
  }

  /** 错误码标识. */
  public String code() {
    return code;
  }

  /** 错误码默认消息. */
  public String message() {
    return message;
  }
}
