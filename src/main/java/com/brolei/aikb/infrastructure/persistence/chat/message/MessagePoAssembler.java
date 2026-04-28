package com.brolei.aikb.infrastructure.persistence.chat.message;

import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.chat.model.MessageId;
import com.brolei.aikb.domain.chat.model.MessageRole;
import org.springframework.stereotype.Component;

/** Message 与 MessagePo 之间的转换器. */
@Component
public class MessagePoAssembler {

  public Message toDomain(MessagePo po) {
    if (po == null) {
      return null;
    }
    return Message.rehydrate(
        MessageId.of(po.getId()),
        ConversationId.of(po.getConversationId()),
        MessageRole.valueOf(po.getRole()),
        po.getContent(),
        po.getCreatedAt());
  }

  public MessagePo toPo(Message message) {
    if (message == null) {
      return null;
    }
    MessagePo po = new MessagePo();
    po.setId(message.id().value());
    po.setConversationId(message.conversationId().value());
    po.setRole(message.role().name());
    po.setContent(message.content());
    po.setCreatedAt(message.createdAt());
    return po;
  }
}
