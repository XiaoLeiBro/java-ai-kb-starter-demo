package com.brolei.aikb.interfaces.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 创建会话请求 DTO. */
public record CreateConversationRequest(
    @NotBlank String knowledgeBaseId, @Size(max = 200) String title) {}
