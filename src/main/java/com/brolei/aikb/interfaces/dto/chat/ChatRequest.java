package com.brolei.aikb.interfaces.dto.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 聊天请求 DTO. */
@Schema(description = "AI 问答请求")
public record ChatRequest(
    @Schema(description = "知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001") @NotBlank
        String knowledgeBaseId,
    @Schema(description = "用户问题", example = "员工报销发票有什么要求？") @NotBlank String question,
    @Schema(description = "检索返回的知识片段数量，范围 1-20，默认 5", example = "5") Integer topK,
    @Schema(description = "会话 ID，可选；传入后会把本次问答写入对应会话历史")
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
