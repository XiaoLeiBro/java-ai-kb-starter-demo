package com.brolei.aikb.application.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.ConversationStatus;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.chat.repository.ConversationRepository;
import com.brolei.aikb.domain.chat.repository.MessageRepository;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 对话会话相关用例的应用服务. */
@Service
public class ConversationApplicationService {

  private static final Logger log = LoggerFactory.getLogger(ConversationApplicationService.class);
  private static final String DEFAULT_TITLE = "新对话";

  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;
  private final KnowledgeBaseRepository knowledgeBaseRepository;

  public ConversationApplicationService(
      ConversationRepository conversationRepository,
      MessageRepository messageRepository,
      KnowledgeBaseRepository knowledgeBaseRepository) {
    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
    this.knowledgeBaseRepository = knowledgeBaseRepository;
  }

  /** 创建对话会话. */
  @Transactional
  public Conversation createConversation(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId, String title) {
    KnowledgeBase kb =
        knowledgeBaseRepository
            .findById(knowledgeBaseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!kb.ownerId().equals(ownerId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (kb.isArchived()) {
      throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ARCHIVED);
    }
    String finalTitle = (title == null || title.isBlank()) ? DEFAULT_TITLE : title;
    Conversation conversation = Conversation.create(ownerId, knowledgeBaseId, finalTitle);
    Conversation saved = conversationRepository.save(conversation);
    log.info(
        "Conversation created: id={}, ownerId={}, kbId={}",
        saved.id().value(),
        ownerId.value(),
        knowledgeBaseId.value());
    return saved;
  }

  /** 列出当前用户的 ACTIVE 会话列表. */
  @Transactional(readOnly = true)
  public List<Conversation> listMyConversations(UserId ownerId, KnowledgeBaseId knowledgeBaseId) {
    if (knowledgeBaseId != null) {
      return conversationRepository.findByOwnerIdAndKnowledgeBaseIdAndStatus(
          ownerId, knowledgeBaseId, ConversationStatus.ACTIVE);
    }
    return conversationRepository.findByOwnerIdAndStatus(ownerId, ConversationStatus.ACTIVE);
  }

  /** 查询单个会话详情. */
  @Transactional(readOnly = true)
  public Conversation getConversation(ConversationId conversationId, UserId ownerId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!conversation.ownerId().equals(ownerId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return conversation;
  }

  /** 查询会话消息列表. */
  @Transactional(readOnly = true)
  public List<Message> getMessages(ConversationId conversationId, UserId ownerId) {
    Conversation conversation = getConversation(conversationId, ownerId);
    if (conversation.isArchived()) {
      throw new BusinessException(ErrorCode.CONVERSATION_GONE);
    }
    return messageRepository.findByConversationId(conversationId);
  }

  /** 归档会话. */
  @Transactional
  public void archiveConversation(ConversationId conversationId, UserId ownerId) {
    Conversation conversation = getConversation(conversationId, ownerId);
    conversation.archive();
    conversationRepository.save(conversation);
    log.info("Conversation archived: id={}", conversationId.value());
  }
}
