package com.brolei.aikb.interfaces.dto.knowledge;

import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import io.swagger.v3.oas.annotations.media.Schema;

/** 文档响应 DTO. */
@Schema(description = "知识库文档信息")
public record DocumentResponse(
    @Schema(description = "文档 ID", example = "9d0e5b21-6dfb-4dc2-90c8-f221785b0001") String id,
    @Schema(description = "所属知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
        String knowledgeBaseId,
    @Schema(description = "上传时的原始文件名", example = "company-policy-demo.md") String originalFilename,
    @Schema(description = "文件类型", example = "text/markdown") String contentType,
    @Schema(description = "文件大小，单位字节", example = "2048") long fileSize,
    @Schema(description = "文档处理状态", example = "READY") String status,
    @Schema(description = "文档被切分后的片段数量", example = "8") int chunkCount,
    @Schema(description = "处理失败时的错误信息") String errorMessage,
    @Schema(description = "创建时间") String createdAt,
    @Schema(description = "更新时间") String updatedAt) {

  /** 从领域 KnowledgeDocument 对象创建 DocumentResponse. */
  public static DocumentResponse from(KnowledgeDocument doc) {
    return new DocumentResponse(
        doc.id().value(),
        doc.knowledgeBaseId().value(),
        doc.originalFilename(),
        doc.contentType(),
        doc.fileSize(),
        doc.status().name(),
        doc.chunkCount(),
        doc.errorMessage(),
        doc.createdAt().toString(),
        doc.updatedAt().toString());
  }
}
