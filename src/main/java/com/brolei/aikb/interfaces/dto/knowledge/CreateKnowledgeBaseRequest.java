package com.brolei.aikb.interfaces.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 创建知识库请求 DTO. */
@Schema(description = "创建知识库请求")
public record CreateKnowledgeBaseRequest(
    @Schema(description = "知识库名称", example = "公司制度知识库") @NotBlank String name,
    @Schema(description = "知识库描述，可选", example = "用于回答员工手册、报销制度、请假制度等问题") String description) {}
