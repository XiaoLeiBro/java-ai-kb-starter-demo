package com.brolei.aikb.domain.knowledge.repository;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;

/**
 * 知识库仓储接口（在领域层定义，实现放到 infrastructure.persistence）.
 *
 * <p>依赖倒置：领域层不关心是 MyBatis-Plus、JPA 还是其他存储实现。
 */
public interface KnowledgeBaseRepository {

  KnowledgeBase save(KnowledgeBase knowledgeBase);

  Optional<KnowledgeBase> findById(KnowledgeBaseId id);

  List<KnowledgeBase> findByOwnerId(UserId ownerId);

  void archiveById(KnowledgeBaseId id);
}
