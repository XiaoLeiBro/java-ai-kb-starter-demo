# v0.2 RAG 主流程任务

## A. 规格与依赖

- [x] 确认 LangChain4j Chat / Embedding Starter 可用。
- [x] 使用 `pgvector/pgvector:pg16` 支撑本地与测试环境向量检索。
- [x] 增加 `ai-kb.upload`、`ai-kb.vector`、`ai-kb.llm` 配置字段。
- [x] 默认 embedding 维度设为 `1024`，匹配 `BAAI/bge-m3`。
- [x] 补充 `openspec/specs/knowledge/rag.md` 稳定规格。
- [x] 补充 `openspec/changes/v0-2-rag-main-flow/specs/knowledge/spec.md` 变更规格。

## B. 数据库与持久化

- [x] 将 `KnowledgeBase` 时间类型统一为 `Instant`。
- [x] 增加 `V2__init_rag.sql`，创建 `knowledge_bases`、`knowledge_documents`、`document_chunks`、`kb_embeddings`。
- [x] 在迁移中启用 `CREATE EXTENSION IF NOT EXISTS vector`。
- [x] 实现 `KnowledgeBasePo`、`KnowledgeBaseMapper`、`KnowledgeBasePoAssembler`、`KnowledgeBaseRepositoryImpl`。
- [x] 实现 `KnowledgeDocument`、`KnowledgeDocumentId`、`DocumentStatus`。
- [x] 实现 `DocumentChunk`、`DocumentChunkId`。
- [x] 实现文档与切片 Repository 及 MyBatis-Plus 持久化。
- [x] 保持 PO 使用 `IdType.INPUT`，聚合根 ID 不由持久层重新生成。

## C. 文件与文本处理

- [x] 定义 `FileStorage` 接口。
- [x] 实现 `LocalFileStorage`。
- [x] 文件路径按 `userId/knowledgeBaseId/documentId/originalFilename` 组织。
- [x] 上传文档存储路径使用持久化后的同一个 `documentId`。
- [x] 文件名做安全处理，避免路径穿越。
- [x] 只允许 `.md`、`.txt`。
- [x] 空文件和超过配置大小的文件返回 400。
- [x] UTF-8 读取 Markdown / TXT。
- [x] 实现固定字符数切分，默认 `chunkSize=800`、`overlap=120`。
- [x] 去除空白 chunk。

## D. Embedding、向量与 LLM

- [x] 定义 `EmbeddingProvider` 接口。
- [x] 实现 OpenAI Compatible Embedding Provider。
- [x] 定义 `VectorChunk` 与 `RetrievedChunk` 值对象。
- [x] 定义 `VectorStore` 接口。
- [x] 实现 `PgvectorStore`。
- [x] 检索时必须按 `knowledgeBaseId` 过滤。
- [x] 定义 `LlmProvider` 接口。
- [x] 实现 OpenAI Compatible Chat Provider。
- [x] 应用层不依赖 LangChain4j、pgvector 或 MyBatis-Plus 类型。

## E. 应用用例

- [x] 实现 `KnowledgeApplicationService.createKnowledgeBase`。
- [x] 实现 `KnowledgeApplicationService.listMyKnowledgeBases`。
- [x] 实现 `KnowledgeApplicationService.uploadAndIndexDocument`。
- [x] 实现 `KnowledgeApplicationService.listDocuments`。
- [x] 实现 `PromptBuilder`，限定只能基于知识库片段回答。
- [x] 实现 `ChatApplicationService`。
- [x] 问答前校验知识库归属，跨用户访问统一返回 404。
- [x] 有检索结果时调用 LLM，并返回 answer 与 references。
- [x] 无检索结果时直接返回“当前知识库中没有找到相关信息”，不调用 LLM。

## F. 接口层

- [x] 实现 `POST /api/v1/knowledge-bases`。
- [x] 实现 `GET /api/v1/knowledge-bases`。
- [x] 实现 `POST /api/v1/knowledge-bases/{id}/documents`。
- [x] 实现 `GET /api/v1/knowledge-bases/{id}/documents`。
- [x] 实现 `POST /api/v1/chat`。
- [x] `ChatController` 校验 `knowledgeBaseId`、`question`、`topK`。
- [x] health、register、login、Swagger 保持匿名访问。
- [x] 知识库、文档、问答接口默认需要认证。
- [x] 扩展知识库、文档、LLM、向量检索相关错误码。
- [x] 对外错误不泄露 SQL、API Key 或框架堆栈。

## G. 测试

- [x] `KnowledgeBaseTest` 覆盖领域规则。
- [x] `KnowledgeDocumentTest` 覆盖文档创建、状态流转和指定 ID 创建。
- [x] `KnowledgeApplicationServiceTest` 覆盖上传索引时 documentId 一致性。
- [x] `TextSplitterTest` 覆盖短文本、长文本、overlap、空白文本。
- [x] `PromptBuilderTest` 覆盖有片段、无片段、片段顺序和引用标记。
- [x] `ChatApplicationServiceTest` 覆盖无结果不调 LLM、有结果调 LLM、跨用户 404。
- [x] `AuthControllerIntegrationTest` 覆盖知识库接口 401、创建知识库、上传文档、错误文件类型、跨用户 404、ChatController 校验。
- [x] 集成测试使用 Testcontainers + pgvector，覆盖向量写入和按知识库隔离检索。

## H. 文档

- [x] README 对齐当前 `v0.2` 能力，并补充 API 体验 curl。
- [x] `docs/architecture.md` 更新为 RAG 主链路已实现。
- [x] `docs/faq.md` 更新模型接入、向量维度、离线部署和分库分表边界。
- [x] `openspec/specs/knowledge/rag.md` 沉淀稳定规格。
- [x] `openspec/changes/v0-2-rag-main-flow/proposal.md`、`design.md`、`tasks.md` 与最终代码保持一致。

## 验收

```text
./mvnw -q test
openspec validate v0-2-rag-main-flow --strict
```

所有测试和 OpenSpec 变更校验通过后，可以进入归档。
