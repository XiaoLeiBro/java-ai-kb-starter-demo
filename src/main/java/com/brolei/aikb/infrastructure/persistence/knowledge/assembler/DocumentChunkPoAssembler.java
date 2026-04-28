package com.brolei.aikb.infrastructure.persistence.knowledge.assembler;

import com.brolei.aikb.domain.knowledge.model.DocumentChunk;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.DocumentChunkPo;
import org.springframework.stereotype.Component;

/** DocumentChunk 与 DocumentChunkPo 之间的转换器. */
@Component
public class DocumentChunkPoAssembler {

  /** 将持久化对象转换为领域对象. */
  public DocumentChunk toDomain(DocumentChunkPo po) {
    if (po == null) {
      return null;
    }
    return DocumentChunk.rehydrate(
        DocumentChunkId.of(po.getId()),
        KnowledgeBaseId.of(po.getKnowledgeBaseId()),
        KnowledgeDocumentId.of(po.getDocumentId()),
        po.getChunkIndex(),
        po.getContent(),
        po.getCharCount(),
        po.getCreatedAt());
  }

  /** 将领域对象转换为持久化对象. */
  public DocumentChunkPo toPo(DocumentChunk chunk) {
    if (chunk == null) {
      return null;
    }
    DocumentChunkPo po = new DocumentChunkPo();
    po.setId(chunk.id().value());
    po.setKnowledgeBaseId(chunk.knowledgeBaseId().value());
    po.setDocumentId(chunk.documentId().value());
    po.setChunkIndex(chunk.chunkIndex());
    po.setContent(chunk.content());
    po.setCharCount(chunk.charCount());
    po.setCreatedAt(chunk.createdAt());
    return po;
  }
}
