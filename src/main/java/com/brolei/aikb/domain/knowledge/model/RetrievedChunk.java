package com.brolei.aikb.domain.knowledge.model;

/**
 * 检索结果切片值对象.
 *
 * <p>代表向量检索返回的一个匹配切片及其相关性分数。
 */
public record RetrievedChunk(
    KnowledgeBaseId knowledgeBaseId,
    KnowledgeDocumentId documentId,
    DocumentChunkId chunkId,
    String fileName,
    int chunkIndex,
    String content,
    double score) {}
