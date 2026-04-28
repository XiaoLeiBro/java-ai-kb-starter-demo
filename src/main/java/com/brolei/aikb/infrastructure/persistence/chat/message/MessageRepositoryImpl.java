package com.brolei.aikb.infrastructure.persistence.chat.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.chat.repository.MessageRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link MessageRepository} 实现. */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

  private final MessageMapper mapper;
  private final MessagePoAssembler assembler;

  public MessageRepositoryImpl(MessageMapper mapper, MessagePoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public Message save(Message message) {
    MessagePo po = assembler.toPo(message);
    mapper.insert(po);
    return message;
  }

  @Override
  public List<Message> findByConversationId(ConversationId conversationId) {
    List<MessagePo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<MessagePo>()
                .eq(MessagePo::getConversationId, conversationId.value())
                .orderByAsc(MessagePo::getCreatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }
}
