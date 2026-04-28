package com.brolei.aikb.infrastructure.vector;

import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.DocumentChunkMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.KbEmbeddingMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.mapper.KnowledgeDocumentMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.DocumentChunkPo;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KbEmbeddingPo;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeDocumentPo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** 基于 pgvector 的 {@link VectorStore} 实现. */
@Component
public class PgvectorStore implements VectorStore {

  private final KbEmbeddingMapper kbEmbeddingMapper;
  private final DocumentChunkMapper documentChunkMapper;
  private final KnowledgeDocumentMapper knowledgeDocumentMapper;

  public PgvectorStore(
      KbEmbeddingMapper kbEmbeddingMapper,
      DocumentChunkMapper documentChunkMapper,
      KnowledgeDocumentMapper knowledgeDocumentMapper) {
    this.kbEmbeddingMapper = kbEmbeddingMapper;
    this.documentChunkMapper = documentChunkMapper;
    this.knowledgeDocumentMapper = knowledgeDocumentMapper;
  }

  @Override
  public void saveAll(List<VectorChunk> chunks) {
    List<KbEmbeddingPo> poList = new ArrayList<>();
    for (VectorChunk chunk : chunks) {
      KbEmbeddingPo po = new KbEmbeddingPo();
      po.setId(UUID.randomUUID().toString());
      po.setKnowledgeBaseId(chunk.knowledgeBaseId().value());
      po.setDocumentId(chunk.documentId().value());
      po.setChunkId(chunk.chunkId().value());
      po.setEmbedding(chunk.embedding());
      po.setCreatedAt(Instant.now());
      poList.add(po);
    }
    kbEmbeddingMapper.insertBatch(poList);
  }

  @Override
  public List<RetrievedChunk> search(
      KnowledgeBaseId knowledgeBaseId, String query, float[] queryEmbedding, int topK) {
    List<KbEmbeddingPo> results =
        kbEmbeddingMapper.searchByKbId(knowledgeBaseId.value(), queryEmbedding, topK);
    if (results.isEmpty()) {
      return Collections.emptyList();
    }

    // 收集需要回查的文档 ID 和切片 ID
    List<String> docIds = results.stream().map(KbEmbeddingPo::getDocumentId).distinct().toList();
    List<String> chunkIds = results.stream().map(KbEmbeddingPo::getChunkId).distinct().toList();

    // 批量查询文档信息（获取 fileName）
    Map<String, KnowledgeDocumentPo> docMap =
        knowledgeDocumentMapper.findByDocIds(docIds).stream()
            .collect(Collectors.toMap(KnowledgeDocumentPo::getId, Function.identity()));

    // 批量查询切片信息（获取 chunkIndex、content）
    Map<String, DocumentChunkPo> chunkMap =
        documentChunkMapper.findByChunkIds(chunkIds).stream()
            .collect(Collectors.toMap(DocumentChunkPo::getId, Function.identity()));

    // 组装检索结果
    return results.stream()
        .map(
            po -> {
              KnowledgeDocumentPo docPo = docMap.get(po.getDocumentId());
              DocumentChunkPo chunkPo = chunkMap.get(po.getChunkId());
              return new RetrievedChunk(
                  KnowledgeBaseId.of(po.getKnowledgeBaseId()),
                  KnowledgeDocumentId.of(po.getDocumentId()),
                  DocumentChunkId.of(po.getChunkId()),
                  docPo != null ? docPo.getOriginalFilename() : null,
                  chunkPo != null ? chunkPo.getChunkIndex() : 0,
                  chunkPo != null ? chunkPo.getContent() : null,
                  po.getScore() != null ? po.getScore() : 0.0);
            })
        .toList();
  }
}
