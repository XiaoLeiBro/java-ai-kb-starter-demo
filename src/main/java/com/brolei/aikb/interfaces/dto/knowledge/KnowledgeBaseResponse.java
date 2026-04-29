package com.brolei.aikb.interfaces.dto.knowledge;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import io.swagger.v3.oas.annotations.media.Schema;

/** 知识库响应 DTO. */
@Schema(description = "知识库信息")
public record KnowledgeBaseResponse(
    @Schema(description = "知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001") String id,
    @Schema(description = "知识库名称", example = "公司制度知识库") String name,
    @Schema(description = "知识库描述", example = "用于回答员工手册、报销制度、请假制度等问题") String description,
    @Schema(description = "知识库状态", example = "ACTIVE") String status,
    @Schema(description = "创建时间") String createdAt,
    @Schema(description = "更新时间") String updatedAt) {

  /** 从领域 KnowledgeBase 对象创建 KnowledgeBaseResponse. */
  public static KnowledgeBaseResponse from(KnowledgeBase kb) {
    return new KnowledgeBaseResponse(
        kb.id().value(),
        kb.name(),
        kb.description(),
        kb.status().name(),
        kb.createdAt().toString(),
        kb.updatedAt().toString());
  }
}
