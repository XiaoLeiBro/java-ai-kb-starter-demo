package com.brolei.aikb.common.exception;

/** 携带错误码的业务异常. */
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.message());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, String detail) {
    super(detail);
    this.errorCode = errorCode;
  }

  public ErrorCode errorCode() {
    return errorCode;
  }
}
