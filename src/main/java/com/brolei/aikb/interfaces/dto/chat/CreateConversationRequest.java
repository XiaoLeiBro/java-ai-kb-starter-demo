package com.brolei.aikb.interfaces.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 创建会话请求 DTO. */
@Schema(description = "创建对话会话请求")
public record CreateConversationRequest(
    @Schema(description = "知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001") @NotBlank
        String knowledgeBaseId,
    @Schema(description = "会话标题，可选", example = "报销制度咨询") @Size(max = 200) String title) {}
