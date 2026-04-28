package com.brolei.aikb.infrastructure.persistence.knowledge.assembler;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseStatus;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeBasePo;
import org.springframework.stereotype.Component;

/** KnowledgeBase 与 KnowledgeBasePo 之间的转换器. */
@Component
public class KnowledgeBasePoAssembler {

  /** 将持久化对象转换为领域对象. */
  public KnowledgeBase toDomain(KnowledgeBasePo po) {
    if (po == null) {
      return null;
    }
    return KnowledgeBase.rehydrate(
        KnowledgeBaseId.of(po.getId()),
        UserId.of(po.getOwnerId()),
        po.getName(),
        po.getDescription(),
        KnowledgeBaseStatus.valueOf(po.getStatus()),
        po.getCreatedAt(),
        po.getUpdatedAt());
  }

  /** 将领域对象转换为持久化对象. */
  public KnowledgeBasePo toPo(KnowledgeBase kb) {
    if (kb == null) {
      return null;
    }
    KnowledgeBasePo po = new KnowledgeBasePo();
    po.setId(kb.id().value());
    po.setOwnerId(kb.ownerId().value());
    po.setName(kb.name());
    po.setDescription(kb.description());
    po.setStatus(kb.status().name());
    po.setCreatedAt(kb.createdAt());
    po.setUpdatedAt(kb.updatedAt());
    return po;
  }
}
