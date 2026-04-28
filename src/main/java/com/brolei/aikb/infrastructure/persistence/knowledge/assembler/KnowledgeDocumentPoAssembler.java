package com.brolei.aikb.infrastructure.persistence.knowledge.assembler;

import com.brolei.aikb.domain.knowledge.model.DocumentStatus;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeDocumentPo;
import org.springframework.stereotype.Component;

/** KnowledgeDocument 与 KnowledgeDocumentPo 之间的转换器. */
@Component
public class KnowledgeDocumentPoAssembler {

  /** 将持久化对象转换为领域对象. */
  public KnowledgeDocument toDomain(KnowledgeDocumentPo po) {
    if (po == null) {
      return null;
    }
    return KnowledgeDocument.rehydrate(
        KnowledgeDocumentId.of(po.getId()),
        KnowledgeBaseId.of(po.getKnowledgeBaseId()),
        UserId.of(po.getOwnerId()),
        po.getOriginalFilename(),
        po.getStoragePath(),
        po.getContentType(),
        po.getFileSize() != null ? po.getFileSize() : 0L,
        DocumentStatus.valueOf(po.getStatus()),
        po.getChunkCount() != null ? po.getChunkCount() : 0,
        po.getErrorMessage(),
        po.getCreatedAt(),
        po.getUpdatedAt());
  }

  /** 将领域对象转换为持久化对象. */
  public KnowledgeDocumentPo toPo(KnowledgeDocument document) {
    if (document == null) {
      return null;
    }
    KnowledgeDocumentPo po = new KnowledgeDocumentPo();
    po.setId(document.id().value());
    po.setKnowledgeBaseId(document.knowledgeBaseId().value());
    po.setOwnerId(document.ownerId().value());
    po.setOriginalFilename(document.originalFilename());
    po.setStoragePath(document.storagePath());
    po.setContentType(document.contentType());
    po.setFileSize(document.fileSize());
    po.setStatus(document.status().name());
    po.setChunkCount(document.chunkCount());
    po.setErrorMessage(document.errorMessage());
    po.setCreatedAt(document.createdAt());
    po.setUpdatedAt(document.updatedAt());
    return po;
  }
}
