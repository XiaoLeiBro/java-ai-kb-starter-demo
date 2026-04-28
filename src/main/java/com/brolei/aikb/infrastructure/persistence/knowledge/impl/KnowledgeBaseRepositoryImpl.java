package com.brolei.aikb.infrastructure.persistence.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.infrastructure.persistence.knowledge.assembler.KnowledgeBasePoAssembler;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.KnowledgeBaseMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeBasePo;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link KnowledgeBaseRepository} 实现. */
@Repository
public class KnowledgeBaseRepositoryImpl implements KnowledgeBaseRepository {

  private final KnowledgeBaseMapper mapper;
  private final KnowledgeBasePoAssembler assembler;

  public KnowledgeBaseRepositoryImpl(
      KnowledgeBaseMapper mapper, KnowledgeBasePoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public KnowledgeBase save(KnowledgeBase knowledgeBase) {
    KnowledgeBasePo po = assembler.toPo(knowledgeBase);
    if (mapper.selectById(po.getId()) != null) {
      mapper.updateById(po);
    } else {
      mapper.insert(po);
    }
    return knowledgeBase;
  }

  @Override
  public Optional<KnowledgeBase> findById(KnowledgeBaseId id) {
    KnowledgeBasePo po = mapper.selectById(id.value());
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public List<KnowledgeBase> findByOwnerId(UserId ownerId) {
    List<KnowledgeBasePo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<KnowledgeBasePo>()
                .eq(KnowledgeBasePo::getOwnerId, ownerId.value()));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public void archiveById(KnowledgeBaseId id) {
    KnowledgeBasePo po = mapper.selectById(id.value());
    if (po != null) {
      KnowledgeBase kb = assembler.toDomain(po);
      kb.deactivate();
      mapper.updateById(assembler.toPo(kb));
    }
  }
}
