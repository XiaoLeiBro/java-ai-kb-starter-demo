package com.brolei.aikb.domain.chat.repository;

import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.ConversationStatus;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;

/** 对话会话仓储接口. */
public interface ConversationRepository {

  Conversation save(Conversation conversation);

  Optional<Conversation> findById(ConversationId id);

  List<Conversation> findByOwnerIdAndStatus(UserId ownerId, ConversationStatus status);

  List<Conversation> findByOwnerIdAndKnowledgeBaseIdAndStatus(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId, ConversationStatus status);
}
