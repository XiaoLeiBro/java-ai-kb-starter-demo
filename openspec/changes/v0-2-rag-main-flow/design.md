# v0.2 RAG 主流程设计

## 1. 总体链路

```text
Auth JWT
  ↓
KnowledgeBaseController
  ↓
KnowledgeApplicationService
  ↓
KnowledgeBase / KnowledgeDocument / DocumentChunk
  ↓
LocalFileStorage + TextExtractor + TextSplitter + EmbeddingClient + VectorStore
  ↓
PostgreSQL / pgvector

ChatController
  ↓
ChatApplicationService
  ↓
VectorStore.search(...)
  ↓
LlmProvider.chat(...)
  ↓
AnswerResponse
```

## 2. 分层原则

### 2.1 domain

domain 层只表达业务概念：

- `KnowledgeBase`
- `KnowledgeDocument`
- `DocumentChunk`
- `DocumentStatus`
- `ChatAnswer`
- `RetrievedChunk`
- `LlmProvider`
- `EmbeddingProvider`
- `VectorChunk`

domain 层不得出现：

- `MultipartFile`
- MyBatis-Plus 注解
- LangChain4j 类型
- SQL / pgvector 类型
- Spring Bean 注解

### 2.2 application

application 层负责编排用例：

- 创建知识库
- 查询当前用户知识库
- 上传文档并同步索引
- 单轮 RAG 问答

事务边界放在 application 层。涉及 LLM 外部调用时要避免长事务：

```text
保存文档元数据 → 读取/切分/Embedding → 写向量 → 更新状态
```

如果同步实现中无法完全避免长事务，至少不要把 Chat Model 调用包在数据库事务里。

### 2.3 infrastructure

infrastructure 层适配具体技术：

- MyBatis-Plus：业务表 CRUD
- PostgreSQL pgvector：向量写入与相似度检索
- LangChain4j：OpenAI Compatible Chat / Embedding 调用
- 本地磁盘：原始文件保存

## 3. 领域模型

### 3.1 KnowledgeBase

已有 `KnowledgeBase` 聚合根继续保留，补齐持久化实现。

核心字段：

```text
id
ownerId
name
description
status
createdAt
updatedAt
```

当前 `KnowledgeBase` 使用 `OffsetDateTime`，而 `User` 使用 `Instant`。v0.2 持久化落地时统一为 `Instant`，保持领域聚合时间类型一致，数据库仍使用 `TIMESTAMPTZ`。

### 3.2 KnowledgeDocument

新增文档聚合或实体，用于表达一次上传文档：

```text
id
knowledgeBaseId
ownerId
originalFilename
storagePath
contentType
fileSize
status: UPLOADED / INDEXING / READY / FAILED
chunkCount
errorMessage
createdAt
updatedAt
```

v0.2 同步处理，但仍保留 `status`，方便后续演进到异步任务。

### 3.3 DocumentChunk

用于关系表记录切片元数据：

```text
id
knowledgeBaseId
documentId
chunkIndex
content
charCount
createdAt
```

向量表中也保存 `chunkId`，用于从检索结果回到文档与引用。

### 3.4 VectorChunk

`VectorChunk` 是写入向量库前的应用层值对象，用于把切片文本、Embedding 和业务归属关系打包，避免 infrastructure 直接拼装领域数据：

```text
id
knowledgeBaseId
documentId
chunkId
content
embedding
```

### 3.5 RetrievedChunk

`RetrievedChunk` 是检索结果值对象，用于返回给应用层和最终引用展示：

```text
knowledgeBaseId
documentId
chunkId
fileName
chunkIndex
content
score
```

### 3.6 ChatAnswer

`ChatAnswer` 表达一次单轮问答结果：

```text
answer
references: List<RetrievedChunk>
```

## 4. 数据库设计

新增 Flyway 脚本：

```text
V2__init_rag.sql
```

建议表：

```text
knowledge_bases
knowledge_documents
document_chunks
kb_embeddings
```

### 4.1 knowledge_bases

```text
id VARCHAR(36) PRIMARY KEY
owner_id VARCHAR(36) NOT NULL
name VARCHAR(100) NOT NULL
description VARCHAR(500)
status VARCHAR(20) NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

索引：

```text
idx_kb_owner_id(owner_id)
```

### 4.2 knowledge_documents

```text
id VARCHAR(36) PRIMARY KEY
knowledge_base_id VARCHAR(36) NOT NULL
owner_id VARCHAR(36) NOT NULL
original_filename VARCHAR(255) NOT NULL
storage_path VARCHAR(500) NOT NULL
content_type VARCHAR(100)
file_size BIGINT NOT NULL
status VARCHAR(20) NOT NULL
chunk_count INT NOT NULL DEFAULT 0
error_message VARCHAR(1000)
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

索引：

```text
idx_doc_kb_id(knowledge_base_id)
idx_doc_owner_id(owner_id)
```

### 4.3 document_chunks

```text
id VARCHAR(36) PRIMARY KEY
knowledge_base_id VARCHAR(36) NOT NULL
document_id VARCHAR(36) NOT NULL
chunk_index INT NOT NULL
content TEXT NOT NULL
char_count INT NOT NULL
created_at TIMESTAMPTZ NOT NULL
```

索引：

```text
idx_chunk_kb_id(knowledge_base_id)
idx_chunk_document_id(document_id)
unique(document_id, chunk_index)
```

### 4.4 kb_embeddings

```text
id VARCHAR(36) PRIMARY KEY
knowledge_base_id VARCHAR(36) NOT NULL
document_id VARCHAR(36) NOT NULL
chunk_id VARCHAR(36) NOT NULL
embedding vector(1024) NOT NULL
created_at TIMESTAMPTZ NOT NULL
```

向量维度默认匹配 `BAAI/bge-m3`。如果换 Embedding 模型，必须同步修改配置和迁移脚本。

索引：

```text
idx_embedding_kb_id(knowledge_base_id)
ivfflat 或 hnsw 向量索引，按 pgvector 支持情况选择
```

## 5. 文本处理

### 5.1 文件类型

v0.2 只支持：

```text
.md
.txt
```

拒绝：

```text
pdf/doc/docx/xls/xlsx/ppt/pptx/html/zip
```

### 5.2 文本读取

使用 UTF-8 读取。读取失败返回 `VALIDATION_ERROR` 或文档 `FAILED` 状态。

### 5.3 切分策略

默认策略：

```text
chunkSize: 800 字符
overlap: 120 字符
```

要求：

- 去掉空白切片。
- 保留原始顺序 `chunkIndex`。
- 每个 chunk 保存可展示的文本内容。
- 切分逻辑放在 domain service 或 application helper，不能依赖 LLM 框架。

## 6. Embedding 与向量检索

### 6.1 EmbeddingProvider

领域或应用层只依赖接口：

```java
List<float[]> embedAll(List<String> texts);
```

基础设施层用 LangChain4j 的 OpenAI Compatible Embedding Model 实现。

### 6.2 VectorStore

应用层只依赖接口：

```java
void saveAll(List<VectorChunk> chunks);
List<RetrievedChunk> search(KnowledgeBaseId knowledgeBaseId, String query, int topK);
```

基础设施层负责：

- 向 pgvector 写入 embedding。
- 按 `knowledge_base_id` 过滤。
- 按相似度排序。
- 返回 chunk 内容、文档信息、score。

v0.2 不把 LangChain4j `EmbeddingStore` 类型暴露到 application 或 domain。

## 7. LLM 调用与 Prompt

### 7.1 LlmProvider

应用层只依赖领域抽象：

```java
String chat(String systemPrompt, String userMessage);
```

基础设施层用 LangChain4j OpenAI Compatible Chat Model 实现。v0.2 不把 LangChain4j 的 `ChatLanguageModel` 暴露到 application 或 domain。

### 7.2 PromptBuilder

`PromptBuilder` 负责把检索片段和用户问题组装成稳定 Prompt。它应是可单测的普通类，不直接调用 LLM。

Chat Prompt 必须包含边界：

```text
你是企业知识库问答助手。
只能基于给定的知识库片段回答。
如果片段中没有答案，明确说“当前知识库中没有找到相关信息”。
不要编造制度、金额、日期、负责人。
```

返回答案时附带引用片段，便于用户判断可信度。

## 8. 权限与资源可见性

跨用户访问统一返回 404，不返回 403。原因是避免暴露“这个知识库或文档是否存在”。应用层查询资源时必须同时带上 `ownerId` 或在查询后校验归属并转换为 404。

`SecurityConfig` 当前通过 `anyRequest().authenticated()` 保护新端点。v0.2 实现时需要新增接口鉴权集成测试，显式验证知识库、文档和问答接口未登录均返回 401。

## 9. 错误处理

建议错误码：

```text
KNOWLEDGE_BASE_NOT_FOUND
DOCUMENT_NOT_FOUND
UNSUPPORTED_FILE_TYPE
DOCUMENT_INDEX_FAILED
LLM_PROVIDER_ERROR
VECTOR_SEARCH_ERROR
```

HTTP 映射：

```text
400: 参数错误、文件类型不支持、文件过大
401: 未登录
404: 资源不存在或不属于当前用户
409: 状态冲突
502: LLM / Embedding Provider 调用失败
500: 未预期错误
```

对外响应不能泄露 API Key、底层 SQL、完整堆栈。

## 10. 测试策略

### 10.1 单元测试

- `KnowledgeBaseTest`
- `KnowledgeDocumentTest`
- `TextSplitterTest`
- `PromptBuilderTest`
- `LlmProvider` 使用 fake 覆盖调用边界，不真实访问外部网络

### 10.2 应用层测试

使用 fake：

- FakeKnowledgeBaseRepository
- FakeDocumentRepository
- FakeEmbeddingProvider
- FakeVectorStore
- FakeLlmProvider

覆盖：

- 创建知识库
- 上传文档成功
- 不支持文件类型
- 无片段命中时不编造答案
- 有片段命中时正确组装 Prompt
- 跨用户访问统一返回 404

### 10.3 集成测试

使用 Testcontainers PostgreSQL + pgvector：

- Flyway V1 + V2 可迁移
- 保存知识库 / 文档 / chunk
- 向量写入与按知识库过滤检索

LLM Provider 不做真实外部 API 集成测试，避免测试依赖网络和真实 Key。

## 11. Docker 与本地环境

`docker-compose.yml` 必须继续使用包含 pgvector 扩展的镜像：

```text
pgvector/pgvector:pg16
```

不能替换为普通 `postgres:16-alpine`，否则 `CREATE EXTENSION vector` 会失败。

本地文件存储路径采用：

```text
userId/knowledgeBaseId/documentId/originalFilename
```

这是免费 Demo 的简化方案，便于排查和演示。商业版如需隐藏内部 ID，可改为哈希目录、租户隔离目录或对象存储 key 策略。

## 12. 文档更新

实现完成后更新：

- `README.md`
- `docs/quick-start.md`
- `docs/architecture.md`
- `docs/faq.md`
- `examples/company-policy-demo.md`
- `openspec/specs/knowledge/rag.md`

当前变更进入 archive 前，必须把稳定规格沉淀到 `openspec/specs/knowledge/rag.md`。
