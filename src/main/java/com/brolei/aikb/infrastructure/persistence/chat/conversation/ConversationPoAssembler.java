package com.brolei.aikb.infrastructure.persistence.chat.conversation;

import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.ConversationStatus;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import org.springframework.stereotype.Component;

/** Conversation 与 ConversationPo 之间的转换器. */
@Component
public class ConversationPoAssembler {

  public Conversation toDomain(ConversationPo po) {
    if (po == null) {
      return null;
    }
    return Conversation.rehydrate(
        ConversationId.of(po.getId()),
        UserId.of(po.getOwnerId()),
        KnowledgeBaseId.of(po.getKnowledgeBaseId()),
        po.getTitle(),
        ConversationStatus.valueOf(po.getStatus()),
        po.getCreatedAt(),
        po.getUpdatedAt());
  }

  public ConversationPo toPo(Conversation conversation) {
    if (conversation == null) {
      return null;
    }
    ConversationPo po = new ConversationPo();
    po.setId(conversation.id().value());
    po.setOwnerId(conversation.ownerId().value());
    po.setKnowledgeBaseId(conversation.knowledgeBaseId().value());
    po.setTitle(conversation.title());
    po.setStatus(conversation.status().name());
    po.setCreatedAt(conversation.createdAt());
    po.setUpdatedAt(conversation.updatedAt());
    return po;
  }
}
