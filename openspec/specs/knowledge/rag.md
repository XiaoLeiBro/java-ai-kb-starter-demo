# 知识库 RAG 规范

## 范围

当前稳定规格覆盖 `v0.2` 免费 Demo 的最小知识库 RAG 主流程：

```text
创建知识库 → 上传 Markdown/TXT → 文本切分 → Embedding → pgvector 入库 → 检索 → LLM 问答
```

## API

### 创建知识库

- Method: `POST`
- Path: `/api/v1/knowledge-bases`
- Auth: 需要 `Authorization: Bearer <token>`
- Request:
  - `name`: 必填
  - `description`: 可选
- Success: HTTP 200，返回知识库基础信息。
- Failure:
  - 未登录：HTTP 401
  - 参数校验失败：HTTP 400

### 列出我的知识库

- Method: `GET`
- Path: `/api/v1/knowledge-bases`
- Auth: 需要 JWT
- Success: HTTP 200，只返回当前登录用户创建的知识库。
- Failure:
  - 未登录：HTTP 401

### 上传文档

- Method: `POST`
- Path: `/api/v1/knowledge-bases/{knowledgeBaseId}/documents`
- Auth: 需要 JWT，且知识库必须属于当前用户。
- Content-Type: `multipart/form-data`
- Form:
  - `file`: `.md` 或 `.txt`
- Behavior: 保存文件、读取文本、切分、Embedding、写入 pgvector、更新文档状态。
- Success: HTTP 200，返回文档 ID、文件名、状态和切片数。
- Failure:
  - 未登录：HTTP 401
  - 知识库不存在或不属于当前用户：HTTP 404
  - 文件为空、格式不支持或超过配置大小：HTTP 400
  - Embedding / 向量写入失败：HTTP 502

### 列出文档

- Method: `GET`
- Path: `/api/v1/knowledge-bases/{knowledgeBaseId}/documents`
- Auth: 需要 JWT，且知识库必须属于当前用户。
- Success: HTTP 200，返回该知识库下的文档列表。
- Failure:
  - 未登录：HTTP 401
  - 知识库不存在或不属于当前用户：HTTP 404

### 问答

- Method: `POST`
- Path: `/api/v1/chat`
- Auth: 需要 JWT，且知识库必须属于当前用户。
- Request:

```json
{
  "knowledgeBaseId": "xxx",
  "question": "公司的年假规则是什么？",
  "topK": 5
}
```

- Success: HTTP 200，返回 `answer` 和 `references`。
- Failure:
  - 未登录：HTTP 401
  - `knowledgeBaseId`、`question` 缺失或 `topK` 超出范围：HTTP 400
  - 知识库不存在或不属于当前用户：HTTP 404
  - LLM / Embedding / 向量检索失败：HTTP 502

## 领域规则

- `KnowledgeBase` 是当前用户拥有的知识库，跨用户访问统一返回 404。
- `KnowledgeDocument` 是上传文件对应的文档记录，上传索引成功后状态为 `READY`。
- 文档本地存储路径必须使用同一个持久化 `documentId`，路径格式为 `userId/knowledgeBaseId/documentId/originalFilename`。
- 文本切分默认 `chunkSize=800`、`overlap=120`，空白 chunk 必须丢弃。
- 默认 Embedding 模型按 `BAAI/bge-m3` 设计，向量维度为 `1024`。
- pgvector 检索必须按 `knowledgeBaseId` 过滤，不能跨知识库返回片段。
- 问答有检索结果时才调用 LLM。
- 问答无匹配片段时直接返回“当前知识库中没有找到相关信息”，不得调用 LLM。
- Prompt 必须限定模型只能基于知识库片段回答。
- LLM 调用不放在数据库事务中。

## 持久层规则

- 知识库、文档、切片和向量数据使用 PostgreSQL + pgvector。
- Flyway 负责创建 `knowledge_bases`、`knowledge_documents`、`document_chunks`、`kb_embeddings`。
- `kb_embeddings.embedding` 默认类型为 `vector(1024)`。
- PO 使用 `IdType.INPUT`，聚合根 ID 由领域层或应用层显式传入，持久层不得重新生成 ID。
- application 和 domain 层不得依赖 MyBatis-Plus、LangChain4j 或 pgvector 类型。

## 安全规则

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 知识库、文档和问答接口必须认证。
- 访问其他用户知识库、文档或向量片段时统一返回 404。
- 对外错误不得泄露 SQL、API Key、模型供应商原始敏感信息或框架堆栈。

## 非目标

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
