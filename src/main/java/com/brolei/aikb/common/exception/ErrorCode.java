package com.brolei.aikb.common.exception;

/** 应用错误码枚举. */
public enum ErrorCode {
  USERNAME_EXISTS(409, "USERNAME_EXISTS", "Username already exists"),
  INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "Invalid username or password"),
  UNAUTHORIZED(401, "UNAUTHORIZED", "Authentication required"),
  VALIDATION_ERROR(400, "VALIDATION_ERROR", "Validation failed"),
  INTERNAL_ERROR(500, "INTERNAL_ERROR", "Internal server error");

  private final int httpStatus;
  private final String code;
  private final String message;

  ErrorCode(int httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public int httpStatus() {
    return httpStatus;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }
}
