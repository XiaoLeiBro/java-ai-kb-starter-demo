/**
 * 知识库限界上下文（Knowledge Bounded Context）.
 *
 * <p>核心概念：
 *
 * <ul>
 *   <li><b>KnowledgeBase</b>（聚合根）：一个知识库实例，包含若干文档
 *   <li><b>KnowledgeDocument</b>（实体）：上传到知识库的文档
 *   <li><b>DocumentChunk</b>（实体）：文档切分后的片段
 *   <li><b>EmbeddingRecord</b>（值对象）：向量化记录，与 chunk 一一对应
 * </ul>
 *
 * <p>核心用例：
 *
 * <ul>
 *   <li>创建知识库
 *   <li>上传文档到知识库
 *   <li>文档切分 + 向量化
 *   <li>基于向量相似度检索 TopK 片段
 * </ul>
 *
 * <p>目录约定：
 *
 * <pre>
 *   knowledge/
 *     ├── model/        KnowledgeBase / KnowledgeDocument / DocumentChunk
 *     ├── repository/   KnowledgeBaseRepository 接口
 *     ├── service/      KnowledgeIndexingService（切分 + 向量化领域服务）
 *     └── event/        DocumentIndexedEvent 等
 * </pre>
 */
package com.brolei.aikb.domain.knowledge;
