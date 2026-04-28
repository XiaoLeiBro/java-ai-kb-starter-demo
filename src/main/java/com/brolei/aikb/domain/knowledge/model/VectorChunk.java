package com.brolei.aikb.domain.knowledge.model;

/**
 * 向量切片值对象.
 *
 * <p>包含切片内容和对应的向量嵌入数据，用于与 VectorStore 之间传递数据。 不依赖任何 LangChain4j 或 pgvector 类型。
 *
 * <p>注意：{@code embedding} 使用 {@code float[]} 而非 {@code List<Float>} 以避免装箱开销。 该记录仅作为数据载体，不参与相等性比较或作为
 * Map key。
 */
public record VectorChunk(
    String id,
    KnowledgeBaseId knowledgeBaseId,
    KnowledgeDocumentId documentId,
    DocumentChunkId chunkId,
    String content,
    float[] embedding) {}
