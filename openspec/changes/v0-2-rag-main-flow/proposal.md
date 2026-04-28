# v0.2 RAG 主流程

## Why

当前仓库 `v0.1` 已完成用户注册、登录、JWT 鉴权、DDD 分层骨架、PostgreSQL + Flyway 初始化。项目下一步需要从“认证 Demo”推进到“AI 知识库 Demo”，让用户能真实跑通一条最小 RAG 链路：

```text
创建知识库 → 上传 Markdown/TXT → 文本切分 → Embedding → pgvector 入库 → 检索 → LLM 问答
```

这个变更是免费 Demo 的核心体验版本，但必须控制范围，不能提前做商业版的复杂能力。

## What Changes

### 免费版是否包含

包含在免费 Demo `v0.2`：

- 单用户自己的知识库
- 创建 / 查询知识库
- 上传 Markdown / TXT 文件
- 本地文件存储
- 同步解析、切分、向量化
- PostgreSQL + pgvector 存储向量
- 按知识库检索 TopK 片段
- OpenAI Compatible Chat / Embedding 调用
- 单轮问答接口
- 基础集成测试与可手动体验 API

不包含在免费 Demo `v0.2`，留给后续或商业版：

- 多租户、组织架构、RBAC
- PDF / Word / Excel / PPT 多格式解析
- 异步任务队列、进度条、失败重试
- 文档删除后的向量清理
- 增量向量化、重复文档检测
- 对话历史、多轮上下文记忆
- Token 统计、成本报表、调用审计
- 管理后台完整页面
- 多模型供应商管理
- Agent、工具调用、工作流编排

### 限界上下文

涉及上下文：

- `domain.knowledge`：知识库、文档、切片、索引状态
- `domain.chat`：单轮问答请求与答案结果
- `domain.llm`：大模型与 Embedding 抽象
- `application.knowledge`：知识库创建、文档上传、索引编排
- `application.chat`：RAG 问答用例编排
- `infrastructure.persistence`：MyBatis-Plus 持久化
- `infrastructure.vector`：pgvector 存储与相似度检索
- `infrastructure.llm`：LangChain4j/OpenAI Compatible 适配
- `infrastructure.file`：本地文件存储

### 目标 API

#### 创建知识库

```text
POST /api/v1/knowledge-bases
Auth: Bearer JWT
```

Request:

```json
{
  "name": "公司制度知识库",
  "description": "用于测试 Markdown / TXT 文档问答"
}
```

Success: HTTP 200，返回知识库基础信息。

#### 查询我的知识库列表

```text
GET /api/v1/knowledge-bases
Auth: Bearer JWT
```

Success: HTTP 200，返回当前用户创建的知识库列表。

#### 上传文档并同步索引

```text
POST /api/v1/knowledge-bases/{knowledgeBaseId}/documents
Auth: Bearer JWT
Content-Type: multipart/form-data
```

Form:

```text
file: .md 或 .txt
```

Behavior:

```text
保存文件 → 读取文本 → 切分文本 → 调 Embedding → 写入 pgvector → 更新文档状态
```

Success: HTTP 200，返回文档状态、切片数。

#### 查询知识库文档

```text
GET /api/v1/knowledge-bases/{knowledgeBaseId}/documents
Auth: Bearer JWT
```

Success: HTTP 200，返回文档列表。

#### 单轮问答

```text
POST /api/v1/chat
Auth: Bearer JWT
```

Request:

```json
{
  "knowledgeBaseId": "xxx",
  "question": "公司的年假规则是什么？",
  "topK": 5
}
```

Behavior:

```text
校验知识库归属 → 检索 TopK 片段 → 组装 Prompt → 调 LlmProvider → 返回答案和引用片段
```

Success: HTTP 200。

Response:

```json
{
  "answer": "根据知识库内容，...",
  "references": [
    {
      "documentId": "xxx",
      "fileName": "company-policy.md",
      "chunkIndex": 3,
      "content": "引用片段摘要..."
    }
  ]
}
```

## Impact

### 关键约束

- domain 层不得依赖 Spring、HTTP、MyBatis-Plus、LangChain4j、pgvector。
- 聚合根 ID 继续由领域层生成，PO 使用 `IdType.INPUT`。
- v0.2 采用同步索引，文件过大时直接拒绝，不做后台任务。
- 上传只允许 `.md`、`.txt`。
- LLM API Key 不允许写死，必须通过环境变量或本地 ignored 配置覆盖。
- RAG 无匹配片段时返回明确提示，不编造答案。
- Prompt 必须限定“仅基于知识库内容回答”。

### 商业版边界

对应商业版能力：

- 进阶版：多格式解析、重复文档处理、基础 Token 统计。
- 专业版：多模型供应商、调用审计、知识库权限、管理后台。
- 陪跑版：二开讲解、部署排障、客户场景改造。
- 企业版：私有化部署、组织架构、SSO、内网模型、审计与合规。

本变更只完成免费 Demo 主链路，不提前实现商业版能力。

## Acceptance Criteria

- `./mvnw -q test` 通过。
- 用户可通过 API 完成：
  - 注册 / 登录
  - 创建知识库
  - 上传 `.md` 或 `.txt`
  - 文档被切分并写入 pgvector
  - 对知识库提问并得到答案
- 未登录访问知识库和问答接口返回 401。
- 访问其他用户知识库统一返回 404，不暴露资源是否存在。
- 不支持的文件类型返回 400。
- Embedding 或 Chat Model 配置缺失时返回清晰错误，不出现空指针或框架堆栈泄漏。
