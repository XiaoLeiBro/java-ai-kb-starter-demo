package com.brolei.aikb.domain.knowledge.service;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import java.util.List;

/**
 * 向量存储领域服务接口.
 *
 * <p>抽象向量嵌入的存储与检索操作，不依赖任何 LangChain4j 或 pgvector 类型。
 */
public interface VectorStore {

  /**
   * 批量保存向量切片.
   *
   * @param chunks 向量切片列表
   */
  void saveAll(List<VectorChunk> chunks);

  /**
   * 向量相似度检索.
   *
   * @param knowledgeBaseId 知识库 ID（用于限定检索范围）
   * @param query 查询文本
   * @param queryEmbedding 查询文本的向量嵌入
   * @param topK 返回最相似结果的个数
   * @return 检索结果列表，按相似度降序排列
   */
  List<RetrievedChunk> search(
      KnowledgeBaseId knowledgeBaseId, String query, float[] queryEmbedding, int topK);
}
