# GitHub Profile README Draft

> 说明：这是用于 `XiaoLeiBro/XiaoLeiBro` GitHub Profile 仓库的 README 草稿。  
> 当前不要公开价格，不强调求职困境，不暴露过多私人信息，重点展示：Java 工程经验 + AI 应用工程化 + 可交付项目。

---

# Hi, I'm Leo Shi / 磊哥

> Java Backend Developer · AI Application Engineering · RAG / Agent / Spring Boot

我是一名长期从事 Java 后端与企业级系统开发的工程师，关注方向是 **Java AI 应用工程化**：如何把 RAG、Agent、Tool Calling、结构化输出、权限控制、审计追踪等大模型能力，落地到真实的 Java / Spring Boot 业务系统中。

目前我正在围绕以下方向做项目化探索：

```text
Java 企业知识库 / RAG 私有化部署
AI Agent 在业务系统中的工程化落地
病程资料管理与复诊资料整理小程序
AI Dev Workflow / OpenSpec / Codex / ClaudeCode 辅助开发
```

---

## Focus

```text
Java AI Engineering
RAG Knowledge Base
Agent / Tool Calling
Spring Boot / DDD / PostgreSQL / pgvector
AI-assisted Software Development
```

我更关注 AI 应用落地中的工程问题，而不只是调用大模型 API：

- 文档解析、切分、向量化、召回、重排、引用来源
- 用户权限、知识库归属、metadata 过滤、数据隔离
- Tool Calling 的读写隔离、参数校验、人工审批
- JSON Schema / 结构化输出 / 失败重试 / 缺字段追问
- Trace 审计、调用日志、Token 统计、成本控制
- Docker Compose / 私有化部署 / Java 老系统二开

---

## Tech Stack

### Backend

```text
Java 21
Spring Boot
Spring Security
MyBatis-Plus
PostgreSQL / pgvector
Redis
Flyway
Docker Compose
DDD / OpenSpec
```

### AI Application

```text
RAG
Embedding
Vector Search
Tool Calling
Agent Workflow
Structured Output
Prompt Engineering
LangChain4j
Spring AI / Spring AI Alibaba
OpenAI Compatible API
```

### Frontend / Mini Program

```text
Vue 3
uni-app
Pinia
TypeScript
微信小程序
```

### AI Development Tools

```text
ClaudeCode
Codex
OpenSpec
AI-assisted coding workflow
```

---

## Featured Projects

### 1. Java 企业 AI 知识库工程模板

Repository: [java-ai-kb-starter-demo](https://github.com/XiaoLeiBro/java-ai-kb-starter-demo)

一个面向 **Java AI 应用工程化、企业知识库、RAG 私有化部署、AI 客服和老系统二开** 的工程模板。

核心能力：

```text
用户注册 / 登录 / JWT 鉴权
知识库创建与归属校验
Markdown / TXT / 文本型 PDF 上传
文档解析与文本切分
Embedding 向量化
PostgreSQL + pgvector 检索
RAG 问答与引用来源返回
会话历史
基础 LLM 调用记录
Docker Compose 一键启动
```

项目目标不是再造一个通用 SaaS，而是验证：

> Java 后端如何把 AI 知识库能力接入真实企业系统，并具备可维护、可部署、可二开的工程结构。

---

### 2. 家庭病历电子化管理工具

Backend: [medrecord-server](https://github.com/XiaoLeiBro/medrecord-server)  
Frontend: [medrecord-web](https://github.com/XiaoLeiBro/medrecord-web)

一个面向患者和家属的病历资料整理工具，用于把分散的检查报告、影像报告、费用票据、副作用记录和复诊信息整理成可追溯的病程档案。

产品边界：

```text
病历资料整理
病程时间线
检查报告归档
费用台账
影像报告记录
副作用记录
复诊资料整理
```

不定位为 AI 医生，不提供诊断、处方或治疗决策。

---

### 3. 营销活动配置 Agent Demo

Repository: [marketing-agent-demo](https://github.com/XiaoLeiBro/marketing-agent-demo)

一个面向营销中台内部运营场景的 Agent / Copilot Demo。

项目重点不是让 Agent 直接操作生产系统，而是演示企业场景下更可控的 AI 工程化链路：

```text
自然语言需求解析
结构化活动草稿生成
RAG 检索历史活动样例
Tool Calling 查询人群包、库存、时间冲突
JSON Schema 等价校验
规则校验链
人工审批
Trace 审计
活动状态流转
```

核心原则：

> LLM 负责理解和草稿生成，Java 后端负责确定性规则、权限边界、事务边界和审计追踪。

---

### 4. LangChain / RAG Learning Demo

Repository: [langchain-demo](https://github.com/XiaoLeiBro/langchain-demo)

用于记录 LangChain、Embedding、RAG、ChromaDB、BGE、Reranker、PromptTemplate、Few-shot、LCEL 等基础能力的学习和实验过程。

---

## What I'm Building

我目前主要在做三类事情：

```text
1. 把 Java 企业知识库 Demo 做成可演示、可部署、可二开的工程模板
2. 把病程管理小程序做成真实产品原型
3. 沉淀 Java AI 应用工程化内容与项目经验
```

公开内容会围绕：

```text
Java 程序员如何做 RAG
Spring Boot 如何接入大模型应用
企业知识库权限过滤怎么设计
Agent 写操作为什么必须审批
pgvector / Qdrant / ES 如何选择
AI 项目如何从 Demo 走向可交付
```

---

## Engineering View

我对 AI 应用工程化的基本判断：

```text
AI 应用真正难的不是调通模型 API，
而是把模型能力放进一个可维护、可观测、可控成本、可审计、可交付的业务系统里。
```

因此，我更关注这些问题：

- 大模型输出不稳定时，业务系统如何兜底？
- RAG 召回不准时，如何调试和优化？
- Agent 调错工具时，如何限制风险？
- 企业知识库如何避免越权召回？
- AI 调用链路如何记录、审计和回放？
- 一个 Demo 如何变成真实可交付项目？

---

## Contact

- GitHub: [XiaoLeiBro](https://github.com/XiaoLeiBro)
- Personal IP: 磊哥聊 Java AI
- Focus: Java AI 工程化 / 企业知识库 / RAG / Agent / 微信小程序

---

## 中文简介

我是磊哥，一名 Java 后端开发者，正在专注于 **Java + AI 应用工程化** 方向。

我主要关注如何把 RAG、Agent、Tool Calling、结构化输出、权限控制、审计日志、成本控制等能力，真正落地到 Java / Spring Boot 业务系统中。

当前主推项目是：

```text
Java 企业 AI 知识库工程模板
家庭病历电子化管理小程序
营销活动配置 Agent Demo
```

这些项目既是技术探索，也是我对个人产品化、AI 工程化和一人公司方向的长期实践。
