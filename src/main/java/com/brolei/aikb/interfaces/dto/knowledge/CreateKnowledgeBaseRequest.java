package com.brolei.aikb.interfaces.dto.knowledge;

import jakarta.validation.constraints.NotBlank;

/** 创建知识库请求 DTO. */
public record CreateKnowledgeBaseRequest(@NotBlank String name, String description) {}
