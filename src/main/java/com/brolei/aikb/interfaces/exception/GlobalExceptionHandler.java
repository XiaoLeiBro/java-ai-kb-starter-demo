package com.brolei.aikb.interfaces.exception;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.interfaces.dto.ApiResult;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** REST 控制器的全局异常处理. */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** 处理业务异常. */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.errorCode();
    return ResponseEntity.status(errorCode.httpStatus())
        .body(ApiResult.error(errorCode.httpStatus(), errorCode.code() + ": " + e.getMessage()));
  }

  /** 处理请求体校验产生的验证错误. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResult<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.status(400)
        .body(new ApiResult<>(400, "VALIDATION_ERROR: Validation failed", errors));
  }

  /** 处理领域层参数校验错误. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResult<Void>> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(400)
        .body(ApiResult.error(400, "VALIDATION_ERROR: " + e.getMessage()));
  }

  /** 处理未预期的异常. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResult<Void>> handleGeneral(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(500)
        .body(ApiResult.error(500, "INTERNAL_ERROR: Internal server error"));
  }
}
