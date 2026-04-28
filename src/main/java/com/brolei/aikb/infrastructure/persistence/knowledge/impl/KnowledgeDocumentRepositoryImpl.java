package com.brolei.aikb.infrastructure.persistence.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeDocumentRepository;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.infrastructure.persistence.knowledge.assembler.KnowledgeDocumentPoAssembler;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.KnowledgeDocumentMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeDocumentPo;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link KnowledgeDocumentRepository} 实现. */
@Repository
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {

  private final KnowledgeDocumentMapper mapper;
  private final KnowledgeDocumentPoAssembler assembler;

  public KnowledgeDocumentRepositoryImpl(
      KnowledgeDocumentMapper mapper, KnowledgeDocumentPoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public KnowledgeDocument save(KnowledgeDocument document) {
    KnowledgeDocumentPo po = assembler.toPo(document);
    if (mapper.selectById(po.getId()) != null) {
      mapper.updateById(po);
    } else {
      mapper.insert(po);
    }
    return document;
  }

  @Override
  public Optional<KnowledgeDocument> findById(KnowledgeDocumentId id) {
    KnowledgeDocumentPo po = mapper.selectById(id.value());
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public List<KnowledgeDocument> findByKnowledgeBaseId(KnowledgeBaseId kbId) {
    List<KnowledgeDocumentPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<KnowledgeDocumentPo>()
                .eq(KnowledgeDocumentPo::getKnowledgeBaseId, kbId.value()));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public List<KnowledgeDocument> findByKnowledgeBaseIdAndOwnerId(
      KnowledgeBaseId kbId, UserId ownerId) {
    List<KnowledgeDocumentPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<KnowledgeDocumentPo>()
                .eq(KnowledgeDocumentPo::getKnowledgeBaseId, kbId.value())
                .eq(KnowledgeDocumentPo::getOwnerId, ownerId.value()));
    return poList.stream().map(assembler::toDomain).toList();
  }
}
