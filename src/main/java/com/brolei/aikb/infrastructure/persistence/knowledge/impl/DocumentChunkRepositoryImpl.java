package com.brolei.aikb.infrastructure.persistence.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.knowledge.model.DocumentChunk;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.repository.DocumentChunkRepository;
import com.brolei.aikb.infrastructure.persistence.knowledge.assembler.DocumentChunkPoAssembler;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.DocumentChunkMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.DocumentChunkPo;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link DocumentChunkRepository} 实现. */
@Repository
public class DocumentChunkRepositoryImpl implements DocumentChunkRepository {

  private final DocumentChunkMapper mapper;
  private final DocumentChunkPoAssembler assembler;

  public DocumentChunkRepositoryImpl(
      DocumentChunkMapper mapper, DocumentChunkPoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public void saveAll(List<DocumentChunk> chunks) {
    List<DocumentChunkPo> poList = chunks.stream().map(assembler::toPo).toList();
    for (DocumentChunkPo po : poList) {
      mapper.insert(po);
    }
  }

  @Override
  public Optional<DocumentChunk> findById(DocumentChunkId id) {
    DocumentChunkPo po = mapper.selectById(id.value());
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public List<DocumentChunk> findByDocumentId(KnowledgeDocumentId documentId) {
    List<DocumentChunkPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<DocumentChunkPo>()
                .eq(DocumentChunkPo::getDocumentId, documentId.value()));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public List<DocumentChunk> findByKnowledgeBaseId(KnowledgeBaseId kbId) {
    List<DocumentChunkPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<DocumentChunkPo>()
                .eq(DocumentChunkPo::getKnowledgeBaseId, kbId.value()));
    return poList.stream().map(assembler::toDomain).toList();
  }
}
