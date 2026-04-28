# 知识库 RAG 规范

## Purpose

覆盖 v0.2 免费 Demo 的最小知识库 RAG 主流程：

```text
创建知识库 → 上传 Markdown/TXT → 文本切分 → Embedding → pgvector 入库 → 检索 → LLM 问答
```

## Requirements

### Requirement: Create Knowledge Base

The system SHALL allow authenticated users to create a knowledge base with a name and optional description.

#### Scenario: Create knowledge base with valid name

- **Given** 用户已登录
- **When** 用户请求 `POST /api/v1/knowledge-bases`，提供 `name`（必填）和可选的 `description`
- **Then** 系统创建知识库，返回 HTTP 200 和知识库基础信息

#### Scenario: Create knowledge base without authentication

- **Given** 用户未登录
- **When** 用户请求 `POST /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 401

#### Scenario: Create knowledge base with invalid parameters

- **Given** 用户已登录
- **When** 用户请求创建知识库，参数校验失败
- **Then** 系统返回 HTTP 400

### Requirement: List My Knowledge Bases

The system SHALL return only the knowledge bases created by the current authenticated user.

#### Scenario: List knowledge bases with valid authentication

- **Given** 用户已登录
- **When** 用户请求 `GET /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 200，仅包含当前用户创建的知识库

#### Scenario: List knowledge bases without authentication

- **Given** 用户未登录
- **When** 用户请求 `GET /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 401

### Requirement: Upload Document

The system SHALL allow users to upload `.md` or `.txt` files to a knowledge base they own, processing them through text chunking, embedding, and pgvector storage.

#### Scenario: Upload valid document to owned knowledge base

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `POST /api/v1/knowledge-bases/{knowledgeBaseId}/documents`，上传 `.md` 或 `.txt` 文件
- **Then** 系统保存文件、读取文本、切分、Embedding、写入 pgvector、更新文档状态，返回 HTTP 200 和文档 ID、文件名、状态、切片数

#### Scenario: Upload document without authentication

- **Given** 用户未登录
- **When** 用户请求上传文档
- **Then** 系统返回 HTTP 401

#### Scenario: Upload document to non-existent or foreign knowledge base

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库上传文档
- **Then** 系统返回 HTTP 404

#### Scenario: Upload invalid file

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户上传空文件、不支持的格式或超过配置大小的文件
- **Then** 系统返回 HTTP 400

#### Scenario: Upload document when embedding or vector storage fails

- **Given** 用户已登录，且拥有指定的知识库
- **When** Embedding 或向量写入失败
- **Then** 系统返回 HTTP 502

### Requirement: List Documents

The system SHALL allow users to list documents within a knowledge base they own.

#### Scenario: List documents in owned knowledge base

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `GET /api/v1/knowledge-bases/{knowledgeBaseId}/documents`
- **Then** 系统返回 HTTP 200 和文档列表

#### Scenario: List documents without authentication

- **Given** 用户未登录
- **When** 用户请求列出文档
- **Then** 系统返回 HTTP 401

#### Scenario: List documents in non-existent or foreign knowledge base

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库列出文档
- **Then** 系统返回 HTTP 404

### Requirement: Chat with Knowledge Base

The system SHALL answer questions based on knowledge base content using RAG retrieval followed by LLM generation.

#### Scenario: Chat with valid knowledge base

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `POST /api/v1/chat`，提供 `knowledgeBaseId` 和 `question`
- **Then** 系统检索相关片段，调用 LLM 生成答案，返回 HTTP 200 和 `answer`、`references`

#### Scenario: Chat without authentication

- **Given** 用户未登录
- **When** 用户请求问答
- **Then** 系统返回 HTTP 401

#### Scenario: Chat with missing or invalid parameters

- **Given** 用户已登录
- **When** 用户请求问答，缺少 `knowledgeBaseId`、`question` 或 `topK` 超出范围
- **Then** 系统返回 HTTP 400

#### Scenario: Chat with non-existent or foreign knowledge base

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库问答
- **Then** 系统返回 HTTP 404

#### Scenario: Chat when LLM/Embedding/vector retrieval fails

- **Given** 用户已登录，且拥有指定的知识库
- **When** LLM、Embedding 或向量检索失败
- **Then** 系统返回 HTTP 502

### Requirement: Domain Rules

- `KnowledgeBase` 是当前用户拥有的知识库，跨用户访问统一返回 404。
- `KnowledgeDocument` 是上传文件对应的文档记录，上传索引成功后状态为 `READY`。
- 文档本地存储路径必须使用同一个持久化 `documentId`，路径格式为 `userId/knowledgeBaseId/documentId/originalFilename`。
- 文本切分默认 `chunkSize=800`、`overlap=120`，空白 chunk 必须丢弃。
- 默认 Embedding 模型按 `BAAI/bge-m3` 设计，向量维度为 `1024`。
- pgvector 检索必须按 `knowledgeBaseId` 过滤，不能跨知识库返回片段。
- 问答有检索结果时才调用 LLM。
- 问答无匹配片段时直接返回"当前知识库中没有找到相关信息"，不得调用 LLM。
- Prompt 必须限定模型只能基于知识库片段回答。
- LLM 调用不放在数据库事务中。

### Requirement: Persistence Rules

- 知识库、文档、切片和向量数据使用 PostgreSQL + pgvector。
- Flyway 负责创建 `knowledge_bases`、`knowledge_documents`、`document_chunks`、`kb_embeddings`。
- `kb_embeddings.embedding` 默认类型为 `vector(1024)`。
- PO 使用 `IdType.INPUT`，聚合根 ID 由领域层或应用层显式传入，持久层不得重新生成 ID。
- application 和 domain 层不得依赖 MyBatis-Plus、LangChain4j 或 pgvector 类型。

### Requirement: Security Rules

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 知识库、文档和问答接口必须认证。
- 访问其他用户知识库、文档或向量片段时统一返回 404。
- 对外错误不得泄露 SQL、API Key、模型供应商原始敏感信息或框架堆栈。

### Requirement: Non-goals

- 不做多租户、组织架构或 RBAC。
- 不做 PDF、Word、Excel、PPT 等多格式解析。
- 不做异步任务、进度条或失败重试。
- 不做文档删除后的向量清理。
- 不做重复文档检测或增量向量化。
- 不做对话历史或多轮记忆。
- 不做 Token 统计、成本报表或调用审计。
- 不做管理后台完整页面。
- 不做多模型供应商管理。
- 不做 Agent、工具调用或工作流编排。
