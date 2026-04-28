package com.brolei.aikb.interfaces.dto;

/** 标准 API 响应包装. */
public record ApiResult<T>(int code, String message, T data) {

  public static <T> ApiResult<T> ok(T data) {
    return new ApiResult<>(200, "OK", data);
  }

  public static <T> ApiResult<T> error(int code, String message) {
    return new ApiResult<>(code, message, null);
  }
}
