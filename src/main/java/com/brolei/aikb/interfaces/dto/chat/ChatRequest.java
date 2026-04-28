package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 聊天请求 DTO. */
public record ChatRequest(
    @NotBlank String knowledgeBaseId,
    @NotBlank String question,
    Integer topK,
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "conversationId must be a valid UUID")
        String conversationId) {

  /** 紧凑构造函数，当 topK 未传入时默认设置为 5，并校验范围 [1, 20]. */
  public ChatRequest {
    if (topK == null) {
      topK = 5;
    } else if (topK < 1 || topK > 20) {
      throw new BusinessException(
          ErrorCode.VALIDATION_ERROR, "topK must be between 1 and 20, got " + topK);
    }
  }
}
