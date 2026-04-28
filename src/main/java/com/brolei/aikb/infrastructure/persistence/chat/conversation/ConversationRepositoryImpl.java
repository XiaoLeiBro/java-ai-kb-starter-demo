package com.brolei.aikb.infrastructure.persistence.chat.conversation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.ConversationStatus;
import com.brolei.aikb.domain.chat.repository.ConversationRepository;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link ConversationRepository} 实现. */
@Repository
public class ConversationRepositoryImpl implements ConversationRepository {

  private final ConversationMapper mapper;
  private final ConversationPoAssembler assembler;

  public ConversationRepositoryImpl(ConversationMapper mapper, ConversationPoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public Conversation save(Conversation conversation) {
    ConversationPo po = assembler.toPo(conversation);
    if (mapper.selectById(po.getId()) != null) {
      mapper.updateById(po);
    } else {
      mapper.insert(po);
    }
    return conversation;
  }

  @Override
  public Optional<Conversation> findById(ConversationId id) {
    ConversationPo po = mapper.selectById(id.value());
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public List<Conversation> findByOwnerIdAndStatus(UserId ownerId, ConversationStatus status) {
    List<ConversationPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<ConversationPo>()
                .eq(ConversationPo::getOwnerId, ownerId.value())
                .eq(ConversationPo::getStatus, status.name())
                .orderByDesc(ConversationPo::getUpdatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public List<Conversation> findByOwnerIdAndKnowledgeBaseIdAndStatus(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId, ConversationStatus status) {
    List<ConversationPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<ConversationPo>()
                .eq(ConversationPo::getOwnerId, ownerId.value())
                .eq(ConversationPo::getKnowledgeBaseId, knowledgeBaseId.value())
                .eq(ConversationPo::getStatus, status.name())
                .orderByDesc(ConversationPo::getUpdatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }
}
