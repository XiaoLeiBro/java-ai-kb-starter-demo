package com.brolei.aikb.domain.knowledge.repository;

import com.brolei.aikb.domain.knowledge.model.DocumentChunk;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import java.util.List;
import java.util.Optional;

/**
 * 文档切片仓储接口.
 *
 * <p>依赖倒置：领域层不关心存储实现细节。
 */
public interface DocumentChunkRepository {

  void saveAll(List<DocumentChunk> chunks);

  List<DocumentChunk> findByDocumentId(KnowledgeDocumentId documentId);

  List<DocumentChunk> findByKnowledgeBaseId(KnowledgeBaseId kbId);

  Optional<DocumentChunk> findById(DocumentChunkId id);
}
