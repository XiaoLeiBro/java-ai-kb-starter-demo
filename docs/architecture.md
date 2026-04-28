# 系统架构与 DDD 分层

## 0. 当前实现状态

当前仓库处于 `v0.1`：

```text
已实现：用户注册 / 登录 / JWT 鉴权、健康检查、DDD 分层骨架、PostgreSQL + Flyway 初始化
已预留：knowledge / chat / llm / billing 限界上下文包边界
未实现：知识库 CRUD、文件上传、文本切分、Embedding、向量检索、AI 问答、对话历史、调用记录
```

本文档描述的是免费 Demo 的目标架构。涉及 RAG 主流程的 Controller / Service / Provider / VectorStore 会从 `v0.2` 开始落地。

---

## 1. 为什么选 DDD

> “CRUD 式开发”在 AI 应用里会迅速失控。

AI 知识库系统看起来像是“上传文件 + 调模型”，但真实项目里会很快出现复杂度：

- **多层流程**：上传 → 解析 → 切分 → 向量化 → 检索 → 组装 Prompt → 调模型 → 记录调用
- **多个外部依赖**：PostgreSQL、pgvector、Redis、LLM Provider、文件存储
- **跨上下文协作**：一次问答可能同时触发对话记录、知识检索、模型调用和 Token 记录
- **多种技术选型替换**：向量库可能从 pgvector 换到 Milvus；模型供应商可能从 SiliconFlow 换到其他 OpenAI Compatible Provider

如果不做架构分层，业务逻辑会散落在 Controller 和 Service 里，最终导致：

```text
换一个模型供应商 = 改很多业务代码
加一个向量库 = 影响问答主流程
做一次计费统计 = 到处补日志和字段
```

DDD 的价值不是“时髦”，而是让变化发生在应该变化的层里。

---

## 2. 四层分层

```text
┌───────────────────────────────────────────────────────────┐
│                     interfaces 接入层                      │
│  REST Controller · DTO · 参数校验 · JWT · 全局异常处理     │
└───────────────────────┬───────────────────────────────────┘
                        │ 调用
┌───────────────────────▼───────────────────────────────────┐
│                   application 应用层                       │
│       用例编排 · 事务边界 · 跨聚合协调 · 领域事件发布       │
└───────────────────────┬───────────────────────────────────┘
                        │ 使用
┌───────────────────────▼───────────────────────────────────┐
│                     domain 领域层                          │
│  聚合根 · 实体 · 值对象 · 领域服务 · 仓储接口 · 领域事件     │
└───────────────────────▲───────────────────────────────────┘
                        │ 实现
┌───────────────────────┴───────────────────────────────────┐
│                 infrastructure 基础设施层                  │
│  MyBatis-Plus · pgvector · LangChain4j · 文件存储 · Config │
└───────────────────────────────────────────────────────────┘
```

依赖方向：

```text
interfaces → application → domain ← infrastructure
```

核心规则：

- 领域层不依赖 Spring、HTTP、MyBatis-Plus、LangChain4j
- 仓储接口写在领域层，实现类写在基础设施层
- 聚合根不提供 public setter，状态变更通过业务方法完成
- PO、Mapper、外部 API Client 都属于 infrastructure
- DTO 只用于 interfaces 层，不进入 domain

---

## 3. 限界上下文

领域层内部按业务能力划分，不按技术类型划分。

| 上下文 | 聚合根 / 核心对象 | 核心职责 |
|---|---|---|
| `domain.user` | User | 登录、身份、用户上下文 |
| `domain.knowledge` | KnowledgeBase | 知识库管理、文档解析、文本切分、向量化 |
| `domain.chat` | Conversation | 会话、消息、RAG 问答 |
| `domain.llm` | LlmProvider | 大模型供应商抽象 |
| `domain.billing` | TokenUsageRecord | Token 消耗、成本统计 |

为什么这样切：

- 每个上下文有独立生命周期
- 每个上下文有独立的技术演进可能
- 每个上下文有独立商业边界
- 免费版可以只跑主流程，商业版再补齐 billing、管理后台、审计和企业能力

---

## 4. 一次问答请求的目标链路

以“用户提问 → 返回答案”为例：

```text
POST /api/v1/chat
{
  "knowledgeBaseId": "xxx",
  "question": "xxx"
}
```

流程：

```text
interfaces/rest/ChatController
  ├─ 解析 JWT → userId
  ├─ 校验 DTO
  └─ 调用 ChatApplicationService.ask(...)

application/chat/ChatApplicationService
  ├─ 校验用户是否有权限访问知识库
  ├─ 调用 KnowledgeRetrievalService 检索 TopK 相关片段
  ├─ 调用 LlmProvider 组装 Prompt 并请求模型
  ├─ 保存 Message / Conversation
  ├─ 记录基础调用信息
  └─ 返回 AnswerResult

domain/knowledge
  ├─ KnowledgeBase
  ├─ KnowledgeDocument
  ├─ DocumentChunk
  └─ KnowledgeRetrievalService

domain/llm
  ├─ LlmProvider
  ├─ ChatRequest
  ├─ ChatResponse
  ├─ EmbeddingRequest
  └─ EmbeddingResponse

infrastructure/llm
  └─ LangChain4jOpenAiCompatibleProvider

infrastructure/vector
  └─ PgvectorStore

infrastructure/persistence
  ├─ po
  ├─ mapper
  ├─ assembler
  └─ repository impl
```

关键点：

> domain 层不感知 MyBatis-Plus、不感知 HTTP、不感知 LangChain4j。

切换技术时，只改 infrastructure。

---

## 5. 目录结构

```text
src/main/java/com/brolei/aikb/
├── AiKbApplication.java
│
├── interfaces/
│   ├── rest/
│   │   ├── HealthController.java
│   │   ├── AuthController.java
│   │   ├── KnowledgeBaseController.java      # v0.2
│   │   ├── DocumentController.java           # v0.2
│   │   └── ChatController.java               # v0.2
│   ├── dto/
│   └── exception/
│
├── application/
│   ├── user/
│   │   └── UserApplicationService.java
│   ├── knowledge/
│   │   ├── KnowledgeBaseCommandService.java  # v0.2
│   │   └── KnowledgeBaseQueryService.java    # v0.2
│   └── chat/
│       └── ChatApplicationService.java       # v0.2
│
├── domain/
│   ├── user/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── knowledge/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── chat/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── llm/
│   │   ├── LlmProvider.java
│   │   └── dto/
│   └── billing/
│       ├── model/
│       ├── repository/
│       └── service/
│
├── infrastructure/
│   ├── persistence/
│   │   ├── user/
│   │   │   ├── po/
│   │   │   ├── mapper/
│   │   │   ├── assembler/
│   │   │   └── impl/
│   │   ├── knowledge/
│   │   └── chat/
│   ├── vector/
│   │   └── PgvectorStore.java                # v0.2
│   ├── llm/
│   │   └── LangChain4jOpenAiCompatibleProvider.java  # v0.2
│   ├── file/
│   │   └── LocalFileStorage.java             # v0.2
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   └── config/
│       └── AiKbProperties.java
│
└── common/
    ├── result/
    ├── exception/
    └── util/
```

---

## 6. 为什么 PO ↔ 领域对象分离

本项目采用三层对象模型，严格分离职责：

```
domain.user.model.User          纯业务对象，无框架注解
        ↕ UserPoAssembler 显式映射
infrastructure.persistence
  .po.UserPo                    MyBatis-Plus PO，贫血模型
        ↕ BaseMapper
infrastructure.persistence
  .mapper.UserMapper            数据访问接口
```

**不做合并的原因：**

1. 数据库字段变化不应冲击业务代码 —— 加 `tenant_id` 不该改 `User` 聚合根
2. 聚合根有不变量 —— `User.register()` 必须校验，PO 是贫血数据载体，两种职责混在一起难测试
3. ORM 注解会钉死字段可见性 —— PO 要求 public setter，聚合根要求私有
4. 换存储时只换 PO 和 Mapper，领域对象保持干净

详见 `openspec/changes/archive/2026-04-28-0001-user-auth-and-stack-realignment/design.md` §1。

---

## 7. ID 策略与分库分表边界

当前免费 Demo 不启用分库分表，用户 ID 由领域层 `UserId.generate()` 生成，持久层 `UserPo` 使用 `@TableId(type = IdType.INPUT)` 原样保存。

这样设计是刻意的：

- v0.1 只有用户注册 / 登录链路，分库分表会增加部署、测试和理解成本
- DDD 分层下，聚合根应在保存前就拥有 ID，不能依赖 MyBatis-Plus 在 `insert` 时补 ID
- 免费 Demo 的目标是清楚展示工程边界，不把企业级复杂度提前塞进样例主线

如果未来商业版需要支持大用户量、多租户或企业级水平扩展，可以增加独立的 ID 生成端口：

```text
domain/application: UserIdGenerator / IdGenerator
infrastructure: SnowflakeIdGenerator
database: BIGINT 或统一字符串 ID
```

注意：即使使用雪花算法，也不建议直接使用 MyBatis-Plus `ASSIGN_ID` 作为聚合根 ID 来源。更稳妥的做法是在应用层创建聚合前生成 ID，再交给仓储保存，保持领域模型和持久化实现解耦。

---

## 8. 技术选型说明

### 为什么主线使用 Spring Boot 4.x？

本项目是新建的 Java AI 工程化模板，不是从企业老系统迁移而来，因此主线选择 Spring Boot 4.x。

这个选择的核心理由：

```text
1. 新项目没有历史兼容包袱，可以直接使用新一代 Spring 体系
2. 项目目标是面向未来的 AI 工程化模板，而不是复刻旧企业项目
3. 当前代码已经使用 Java 21，和 Spring Boot 4.x 的现代化方向一致
4. MyBatis-Plus、LangChain4j 等核心依赖已有 Boot 4 适配版本
```

但这不代表商业交付只能使用 4.x。企业真实环境里，经常会遇到：

```text
客户现有系统仍是 Spring Boot 3.x
公司内部基建、网关、监控、脚手架还没升级到 4.x
某些第三方组件或私有组件只验证过 3.x
```

所以本项目按两条线处理：

| 场景 | 推荐版本 | 原因 |
|---|---|---|
| GitHub 免费 Demo | Spring Boot 4.x | 展示新的 Java AI 工程化模板 |
| 学习 / 个人二开 | Spring Boot 4.x | 少历史包袱，直接跟新技术栈 |
| 接客户现有系统 | Spring Boot 3.x 兼容方案，默认以 3.5.x 为基线 | 降低客户集成阻力 |
| 企业私有化交付 | 按客户环境评估 | 以客户现有基建为准 |

原则：

```text
主线不为未知客户提前降级；
商业版可以为明确客户环境提供 Spring Boot 3.x 兼容分支或迁移说明，默认以 3.5.x 为基线。
```

参考：

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)

---

### 为什么用 PostgreSQL + pgvector，而不是一开始就用独立向量库？

Demo 版的首要目标是：

```text
让用户低成本跑起来
```

PostgreSQL + pgvector 的优势：

- 业务数据和向量数据放在一套数据库里
- Docker Compose 更简单
- 个人学习和小规模验证成本低
- 适合免费 Demo 和早期 MVP

商业版可以通过 `VectorStore` 抽象支持更多方案，例如：

```text
pgvector
Milvus
Elasticsearch
其他向量检索服务
```

---

### 为什么用 LangChain4j？

LangChain4j 是 Java 生态里较常见的大模型应用开发框架之一，适合做：

```text
Chat Model 调用
Embedding
向量检索
RAG 流程
Tool / Function Calling
Memory
Agent 类扩展
```

本项目选择 LangChain4j 的原因：

- 和 Java / Spring Boot 项目集成成本较低
- 适合快速搭建 RAG 应用
- 对 OpenAI Compatible 接口支持较方便
- 后续可以扩展 Tool、Memory、Agent 等能力

### 为什么不是 Spring AI？

Spring AI 也是可选方案。两者不是谁取代谁，而是不同抽象风格。

| 维度 | LangChain4j | Spring AI |
|---|---|---|
| 抽象风格 | 更接近 LangChain 概念 | 更接近 Spring 生态 |
| Spring 集成 | 支持 Spring Boot Starter | Spring 官方生态 |
| RAG 支持 | 组件比较丰富 | 与 Spring 风格一致 |
| 适合人群 | 熟悉 LangChain 概念的人 | 熟悉 Spring 体系的人 |
| 本项目选择原因 | 便于快速构建 RAG 与后续 Agent 扩展 | 可作为后续替代或商业版扩展 |

---

### 为什么第一版不做 Agent？

Agent 的复杂度高于普通 RAG：

```text
调试困难
行为不确定
Token 消耗更高
错误传播路径更长
上线前需要更多测试和限制
```

很多企业第一阶段真正需要的是：

```text
企业知识库
AI 客服
文档问答
内部助手
```

这些场景优先做好 RAG 即可。Agent 可以作为后续版本再引入。

---

## 9. 对 AI 生成代码的边界

AI 代码助手可以提高效率，但不能替代工程判断。

| 场景 | AI 容易出错 | 人工必须审查 |
|---|---|---|
| 聚合根设计 | 会加很多 public setter | ✅ |
| 仓储接口 | 会把 MyBatis-Plus 类型泄漏到领域层 | ✅ |
| 事务边界 | 会在 Controller 上加 `@Transactional` | ✅ |
| 异常处理 | 会吞掉异常或乱抛 RuntimeException | ✅ |
| 多模型 Provider | 会把某个供应商字段硬编码到领域对象 | ✅ |
| Token 统计 | 会只记日志，不考虑成本维度和用户维度 | ✅ |
| 安全配置 | 容易把 `/auth/**` 全部放行 | ✅ |

真正的工程价值在于：

> 知道哪些 AI 生成的代码不能进入真实项目。

---

## 10. OpenSpec 工作流集成

本项目使用 OpenSpec 做规格驱动开发。

典型变更流程：

```text
1. /opsx:explore      先探索问题、理清需求
2. /opsx:propose      生成变更提案
3. /opsx:apply        按 tasks.md 实施代码
4. /opsx:archive      归档已完成变更，转入 specs/ 作为正式规格
```

每个重要功能都建议走一次完整 OpenSpec 流程，例如：

```text
用户注册登录
多模型 Provider 抽象
知识库上传与切分
Token 统计
调用日志
管理后台
```

这样可以让规格、代码、文档一起演进。
