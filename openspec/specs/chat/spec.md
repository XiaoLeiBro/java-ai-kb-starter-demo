# 对话规范

## Purpose

覆盖 v0.3 对话会话、消息历史和基于会话的问答能力，保证 RAG 问答可以按会话保存用户消息、助手消息和调用日志。

## Requirements
### Requirement: 会话生命周期

系统 SHALL 支持创建、查看、列出和归档与知识库绑定且属于特定用户的会话。

#### Scenario: 创建会话

- **Given** 用户已登录
- **When** 用户请求创建对话会话，提供 `knowledgeBaseId` 和可选的 `title`
- **Then** 系统校验 knowledgeBaseId 存在且属于当前用户，创建一个新的 ACTIVE 状态会话，返回会话 ID、标题、知识库 ID、创建时间

#### Scenario:创建会话，knowledgeBaseId 不存在

- **Given** 用户已登录
- **When** 用户请求创建对话会话，提供不存在的 `knowledgeBaseId`
- **Then** 系统返回 404

#### Scenario:创建会话，knowledgeBaseId 属于其他用户

- **Given** 用户已登录
- **When** 用户请求创建对话会话，提供属于其他用户的 `knowledgeBaseId`
- **Then** 系统返回 404

#### Scenario:创建会话，不提供标题

- **Given** 用户已登录
- **When** 用户请求创建对话会话，不提供 `title`
- **Then** 系统创建会话，title 默认为"新对话"

#### Scenario:创建会话，知识库已归档

- **Given** 用户已登录
- **When** 用户请求创建对话会话，提供已归档的 `knowledgeBaseId`
- **Then** 系统返回 400，提示知识库已归档

#### Scenario:创建会话，标题过长

- **Given** 用户已登录
- **When** 用户请求创建对话会话，`title` 长度超过 200 字符
- **Then** 系统返回 400 VALIDATION_ERROR

#### Scenario:查看单个会话

- **Given** 用户已登录，且拥有一个会话
- **When** 用户请求获取该会话详情
- **Then** 系统返回会话元数据（标题、状态、知识库 ID、创建时间、更新时间）

#### Scenario:列出我的会话

- **Given** 用户已登录
- **When** 用户请求获取对话会话列表，可选按 `knowledgeBaseId` 过滤
- **Then** 系统返回当前用户拥有的 ACTIVE 状态会话列表，按更新时间倒序

#### Scenario:查看不存在的会话

- **Given** 用户已登录
- **When** 用户请求获取一个不存在的会话详情
- **Then** 系统返回 404

#### Scenario:用户无会话时列出会话

- **Given** 用户已登录，且从未创建过会话
- **When** 用户请求获取对话会话列表
- **Then** 系统返回 HTTP 200，data 为空数组 `[]`

#### Scenario:按 knowledgeBaseId 过滤会话，无匹配结果

- **Given** 用户已登录，且创建过会话但没有在指定知识库下创建过会话
- **When** 用户请求获取对话会话列表，传入 `knowledgeBaseId` 过滤参数
- **Then** 系统返回 HTTP 200，data 为空数组 `[]`

#### Scenario:归档会话

- **Given** 用户已登录，且拥有一个 ACTIVE 状态的会话
- **When** 用户请求归档该会话（DELETE）
- **Then** 会话状态变为 ARCHIVED，返回成功

#### Scenario:重复归档会话（幂等）

- **Given** 用户已登录，且拥有一个已归档的会话
- **When** 用户再次请求归档该会话
- **Then** 系统返回 HTTP 200，无错误（幂等）

#### Scenario:归档不存在的会话

- **Given** 用户已登录
- **When** 用户请求归档不存在的会话
- **Then** 系统返回 404

#### Scenario:跨用户拒绝访问

- **Given** 用户 A 拥有一个会话
- **When** 用户 B 尝试访问该会话（查看详情、查询消息或归档）
- **Then** 系统返回 404，不暴露资源是否存在

### Requirement: 消息历史

系统 SHALL 支持在会话中追加和获取消息。消息的可见性由所属会话的状态控制。

#### Scenario:获取会话中的消息列表

- **Given** 用户已登录，且拥有一个 ACTIVE 状态的会话，且该会话包含若干消息
- **When** 用户请求获取该会话的消息列表
- **Then** 系统按 createdAt 升序返回该会话下的所有消息

#### Scenario:获取无消息会话的消息

- **Given** 用户已登录，且拥有一个 ACTIVE 状态的会话，但该会话尚未有任何消息
- **When** 用户请求获取该会话的消息列表
- **Then** 系统返回 HTTP 200，data 为空数组 `[]`

#### Scenario:获取已归档会话的消息

- **Given** 用户已登录，且拥有一个已归档的会话，该会话包含若干消息
- **When** 用户请求获取该会话的消息列表
- **Then** 系统返回 HTTP 410 Gone，附带 `conversationId` 和 `status: "ARCHIVED"` 信息，客户端可据此区分"已归档无消息可见"与"ACTIVE 但无消息"

#### Scenario:获取消息数量超过服务器限制

- **Given** 用户已登录，拥有一个 ACTIVE 会话，且该会话消息数超过 `ai-kb.chat.max-list-results`
- **When** 用户请求获取该会话的消息列表
- **Then** 系统返回 HTTP 200，data 包含最近的 `ai-kb.chat.max-list-results` 条消息，响应头 `X-Has-More: true`，`X-Total-Count: N`

#### Scenario:获取不存在会话的消息

- **Given** 用户已登录
- **When** 用户请求获取一个不存在会话的消息列表
- **Then** 系统返回 404

### Requirement: 基于会话的问答

问答接口 SHALL 接受可选的 `conversationId` 参数。提供该参数时，系统应校验所有权、状态和知识库一致性，然后自动持久化用户消息和助手消息，并记录调用日志。

#### Scenario:基于会话 ID 问答

- **Given** 用户已登录，拥有一个 ACTIVE 状态的会话，且该会话绑定的知识库属于该用户
- **When** 用户调用 `POST /api/v1/chat`，传入的 `conversationId` 和 `knowledgeBaseId` 与 Conversation 一致
- **Then** 系统在问答前创建一条 role=USER 消息，LLM 调用成功后创建 role=ASSISTANT 消息，并记录一条 SUCCESS 状态的 InvocationLog；ChatResponse 扩展返回 `userMessageId`、`assistantMessageId`、`invocationLogId`

#### Scenario:非法 conversationId 格式

- **Given** 用户已登录
- **When** 用户调用 `POST /api/v1/chat` 并传入非 UUID 格式的 `conversationId`（如 `"not-a-uuid"`）
- **Then** 系统返回 400 VALIDATION_ERROR

#### Scenario:不提供 conversationId 问答（向后兼容）

- **Given** 用户已登录
- **When** 用户调用 `POST /api/v1/chat` 且不传入 `conversationId`
- **Then** 系统保持 v0.2 原有行为，只返回答案不存历史，不记录 InvocationLog

#### Scenario:不存在的 conversationId 问答

- **Given** 用户已登录
- **When** 用户调用 `POST /api/v1/chat` 并传入不存在的 `conversationId`
- **Then** 系统返回 404

#### Scenario:使用他人的 conversationId 问答

- **Given** 用户 A 拥有一个会话
- **When** 用户 B 调用 `POST /api/v1/chat` 并传入用户 A 的 `conversationId`
- **Then** 系统返回 404

#### Scenario:knowledgeBaseId 不匹配

- **Given** 用户已登录，拥有一个绑定知识库 KB-A 的会话
- **When** 用户调用 `POST /api/v1/chat`，传入该会话的 `conversationId` 但 `knowledgeBaseId` 为 KB-B
- **Then** 系统返回 400，提示 conversation 与 knowledgeBase 不匹配

#### Scenario:向已归档会话发送消息

- **Given** 用户已登录，拥有一个已归档的会话
- **When** 用户调用 `POST /api/v1/chat` 并传入已归档会话的 `conversationId`
- **Then** 系统返回 409，提示无法向已归档会话发送消息

#### Scenario:用户消息保存后 LLM 调用失败

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** user Message 保存成功，但 LLM 调用失败（超时、API 错误等）
- **Then** user Message 保留在数据库中（不回滚），系统创建一条 FAILED 状态的 InvocationLog（含 errorMessage），接口返回错误

#### Scenario:向已归档知识库的会话发送消息

- **Given** 用户已登录，拥有一个绑定知识库 KB-A 的会话，但 KB-A 已被归档
- **When** 用户调用 `POST /api/v1/chat` 并传入该会话的 `conversationId`
- **Then** 系统返回 400，提示知识库已归档

#### Scenario:LLM 返回空回复

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM 调用成功但返回空字符串
- **Then** 系统正常创建 role=ASSISTANT 消息（content=""），记录 SUCCESS 状态 InvocationLog
