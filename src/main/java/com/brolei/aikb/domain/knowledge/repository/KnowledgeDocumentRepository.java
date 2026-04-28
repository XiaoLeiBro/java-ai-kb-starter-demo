package com.brolei.aikb.domain.knowledge.repository;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;

/**
 * 知识文档仓储接口.
 *
 * <p>依赖倒置：领域层不关心存储实现细节。
 */
public interface KnowledgeDocumentRepository {

  KnowledgeDocument save(KnowledgeDocument document);

  Optional<KnowledgeDocument> findById(KnowledgeDocumentId id);

  List<KnowledgeDocument> findByKnowledgeBaseId(KnowledgeBaseId kbId);

  List<KnowledgeDocument> findByKnowledgeBaseIdAndOwnerId(KnowledgeBaseId kbId, UserId ownerId);
}
