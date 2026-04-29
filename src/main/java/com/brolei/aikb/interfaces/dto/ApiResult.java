package com.brolei.aikb.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 标准 API 响应包装. */
@Schema(description = "统一 API 响应结构")
public record ApiResult<T>(
    @Schema(description = "业务状态码，200 表示成功", example = "200") int code,
    @Schema(description = "响应消息", example = "OK") String message,
    @Schema(description = "响应数据，具体结构取决于接口") T data) {

  public static <T> ApiResult<T> ok(T data) {
    return new ApiResult<>(200, "OK", data);
  }

  public static <T> ApiResult<T> error(int code, String message) {
    return new ApiResult<>(code, message, null);
  }
}
