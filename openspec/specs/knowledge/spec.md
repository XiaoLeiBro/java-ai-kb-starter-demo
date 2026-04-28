# 知识库 RAG 规范

## Purpose

覆盖 v0.2 免费 Demo 的最小知识库 RAG 主流程：

```text
创建知识库 → 上传 Markdown/TXT → 文本切分 → Embedding → pgvector 入库 → 检索 → LLM 问答
```

## Requirements

### Requirement: 创建知识库

系统应允许已认证用户创建带有名称和可选描述的知识库。

#### Scenario: 使用有效名称创建知识库

- **Given** 用户已登录
- **When** 用户请求 `POST /api/v1/knowledge-bases`，提供 `name`（必填）和可选的 `description`
- **Then** 系统创建知识库，返回 HTTP 200 和知识库基础信息

#### Scenario: 未认证创建知识库

- **Given** 用户未登录
- **When** 用户请求 `POST /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 401

#### Scenario: 使用无效参数创建知识库

- **Given** 用户已登录
- **When** 用户请求创建知识库，参数校验失败
- **Then** 系统返回 HTTP 400

### Requirement: 列出我的知识库

系统应仅返回当前已认证用户创建的知识库。

#### Scenario: 有效认证下列出知识库

- **Given** 用户已登录
- **When** 用户请求 `GET /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 200，仅包含当前用户创建的知识库

#### Scenario: 未认证下列出知识库

- **Given** 用户未登录
- **When** 用户请求 `GET /api/v1/knowledge-bases`
- **Then** 系统返回 HTTP 401

### Requirement: 上传文档

系统应允许用户向自己拥有的知识库上传 `.md` 或 `.txt` 文件，经过文本切分、Embedding 和 pgvector 存储处理。

#### Scenario: 向拥有的知识库上传有效文档

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `POST /api/v1/knowledge-bases/{knowledgeBaseId}/documents`，上传 `.md` 或 `.txt` 文件
- **Then** 系统保存文件、读取文本、切分、Embedding、写入 pgvector、更新文档状态，返回 HTTP 200 和文档 ID、文件名、状态、切片数

#### Scenario: 未认证上传文档

- **Given** 用户未登录
- **When** 用户请求上传文档
- **Then** 系统返回 HTTP 401

#### Scenario: 向不存在或他人的知识库上传文档

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库上传文档
- **Then** 系统返回 HTTP 404

#### Scenario: 上传无效文件

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户上传空文件、不支持的格式或超过配置大小的文件
- **Then** 系统返回 HTTP 400

#### Scenario: Embedding 或向量存储失败时上传文档

- **Given** 用户已登录，且拥有指定的知识库
- **When** Embedding 或向量写入失败
- **Then** 系统返回 HTTP 502

### Requirement: 列出文档

系统应允许用户列出自己拥有的知识库中的文档。

#### Scenario: 在拥有的知识库中列出文档

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `GET /api/v1/knowledge-bases/{knowledgeBaseId}/documents`
- **Then** 系统返回 HTTP 200 和文档列表

#### Scenario: 未认证下列出文档

- **Given** 用户未登录
- **When** 用户请求列出文档
- **Then** 系统返回 HTTP 401

#### Scenario: 在不存在或他人的知识库中列出文档

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库列出文档
- **Then** 系统返回 HTTP 404

### Requirement: 知识库问答

系统应通过 RAG 检索 + LLM 生成来回答基于知识库内容的问题。

#### Scenario: 使用有效知识库问答

- **Given** 用户已登录，且拥有指定的知识库
- **When** 用户请求 `POST /api/v1/chat`，提供 `knowledgeBaseId` 和 `question`
- **Then** 系统检索相关片段，调用 LLM 生成答案，返回 HTTP 200 和 `answer`、`references`

#### Scenario: 未认证问答

- **Given** 用户未登录
- **When** 用户请求问答
- **Then** 系统返回 HTTP 401

#### Scenario: 缺少或无效参数问答

- **Given** 用户已登录
- **When** 用户请求问答，缺少 `knowledgeBaseId`、`question` 或 `topK` 超出范围
- **Then** 系统返回 HTTP 400

#### Scenario: 向不存在或他人的知识库问答

- **Given** 用户已登录
- **When** 用户请求向不存在或不属于自己的知识库问答
- **Then** 系统返回 HTTP 404

#### Scenario: LLM/Embedding/向量检索失败时问答

- **Given** 用户已登录，且拥有指定的知识库
- **When** LLM、Embedding 或向量检索失败
- **Then** 系统返回 HTTP 502

### Requirement: 领域规则

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

### Requirement: 持久层规则

- 知识库、文档、切片和向量数据使用 PostgreSQL + pgvector。
- Flyway 负责创建 `knowledge_bases`、`knowledge_documents`、`document_chunks`、`kb_embeddings`。
- `kb_embeddings.embedding` 默认类型为 `vector(1024)`。
- PO 使用 `IdType.INPUT`，聚合根 ID 由领域层或应用层显式传入，持久层不得重新生成 ID。
- application 和 domain 层不得依赖 MyBatis-Plus、LangChain4j 或 pgvector 类型。

### Requirement: 安全规则

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 知识库、文档和问答接口必须认证。
- 访问其他用户知识库、文档或向量片段时统一返回 404。
- 对外错误不得泄露 SQL、API Key、模型供应商原始敏感信息或框架堆栈。

### Requirement: 非目标

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
