package com.brolei.aikb.domain.chat.repository;

import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.Message;
import java.util.List;

/** 对话消息仓储接口. */
public interface MessageRepository {

  /** 保存消息，底层调用 insert（append-only）. */
  Message save(Message message);

  /** 查询会话下的所有消息，按 createdAt 升序返回. */
  List<Message> findByConversationId(ConversationId conversationId);
}
