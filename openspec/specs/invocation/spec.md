# 调用日志规范

## Purpose
TBD — 由归档变更 2026-04-29-v0-3-chat-history-and-invocation-log 创建，请归档后补充目的说明。

## Requirements
### Requirement: Automatic Invocation Logging

系统在聊天请求包含 `conversationId` 时，应自动记录 LLM 调用的元数据。日志应包含模型名称、Token 数量、耗时和状态，但不包含完整的请求/响应内容。

#### Scenario: 记录成功的调用

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM 调用成功
- **Then** 系统自动创建一条 InvocationLog，包含 modelName、promptTokens、completionTokens、totalTokens、durationMs、status=SUCCESS

#### Scenario:记录失败的调用

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM 调用失败（超时、API 错误、限流等）
- **Then** 系统自动创建一条 InvocationLog，包含 modelName、durationMs、status=FAILED、errorMessage

#### Scenario:Token 用量为零或不可用时

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM provider 返回的 token 计数为 0 或 null（部分 provider 不支持）
- **Then** 系统存储 0 作为 token 计数值，InvocationLog status 正常记录

#### Scenario:失败调用脱敏错误信息

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM 调用失败，原始异常包含 API key 或内部 URL 等敏感信息
- **Then** InvocationLog 的 `errorMessage` 仅包含脱敏后的错误描述（去除 `sk-*`、Bearer token、内部 URL 等），完整错误详情仅记录在服务端日志中

#### Scenario:无 conversationId 时不记录调用日志

- **Given** 用户调用 `POST /api/v1/chat` 且不传入 `conversationId`
- **When** LLM 调用完成（成功或失败）
- **Then** 系统不创建 InvocationLog

#### Scenario:调用日志包含 messageId 关联

- **Given** 用户调用 `POST /api/v1/chat` 并传入 `conversationId`
- **When** LLM 调用成功，assistant Message 保存完成
- **Then** InvocationLog 的 `messageId` 字段指向该 assistant Message

### Requirement: Query Invocation Logs

系统应允许用户查询自己的调用日志，支持可选过滤条件。结果仅限当前用户。

#### Scenario:列出调用日志

- **Given** 用户已登录，且有过 LLM 调用记录
- **When** 用户请求获取调用记录列表，不传过滤参数
- **Then** 系统返回当前用户的调用记录列表，按 createdAt 倒序

#### Scenario:按 knowledgeBaseId 过滤调用日志

- **Given** 用户已登录，且在不同知识库下有过 LLM 调用记录
- **When** 用户请求获取调用记录列表，传入 `knowledgeBaseId` 过滤参数
- **Then** 系统返回当前用户在该知识库下的调用记录列表

#### Scenario:按日期范围过滤调用日志

- **Given** 用户已登录，且有过 LLM 调用记录
- **When** 用户请求获取调用记录列表，传入 `dateFrom` 和 `dateTo`
- **Then** 系统返回当前用户在指定时间范围内的调用记录列表。`dateFrom` 和 `dateTo` 为 UTC 日期（格式 `yyyy-MM-dd`）。`dateFrom` 补齐到当天 00:00:00 UTC，`dateTo` 补齐到当天 23:59:59.999 UTC

#### Scenario:部分日期范围过滤调用日志

- **Given** 用户已登录，且有过 LLM 调用记录
- **When** 用户请求获取调用记录列表，只传 `dateFrom` 不传 `dateTo`
- **Then** 系统返回当前用户从 `dateFrom` 到当前 UTC 时间的调用记录列表

#### Scenario:仅传 dateTo 过滤调用日志

- **Given** 用户已登录，且有过 LLM 调用记录
- **When** 用户请求获取调用记录列表，只传 `dateTo` 不传 `dateFrom`
- **Then** 系统返回当前用户从 epoch 到 `dateTo` 的调用记录列表

#### Scenario:非法日期格式过滤调用日志

- **Given** 用户已登录
- **When** 用户请求获取调用记录列表，传入非法格式的 `dateFrom`（如 `not-a-date`）
- **Then** 系统返回 400 VALIDATION_ERROR

#### Scenario:非法日期范围过滤调用日志

- **Given** 用户已登录
- **When** 用户请求获取调用记录列表，传入 `dateFrom` 晚于 `dateTo`
- **Then** 系统返回 400 VALIDATION_ERROR

#### Scenario:调用日志数量超过服务器限制

- **Given** 用户已登录，且 LLM 调用记录数超过 `ai-kb.chat.max-list-results`
- **When** 用户请求获取调用记录列表
- **Then** 系统返回 HTTP 200，data 包含最近的 `ai-kb.chat.max-list-results` 条记录，响应头 `X-Has-More: true`，`X-Total-Count: N`

#### Scenario:无匹配记录的调用日志

- **Given** 用户已登录，且过滤条件下无匹配记录
- **When** 用户请求获取调用记录列表
- **Then** 系统返回 HTTP 200，data 为空数组 `[]`

#### Scenario:跨用户日志不可见

- **Given** 用户 A 有过 LLM 调用记录
- **When** 用户 B 请求获取调用记录列表
- **Then** 系统只返回用户 B 自己的调用记录，不包含用户 A 的记录

#### Scenario:knowledgeBaseId 过滤限定当前用户

- **Given** 用户 A 在知识库 KB-X 下有过调用记录
- **When** 用户 B 请求获取调用记录列表，传入 `knowledgeBaseId=KB-X`
- **Then** 系统返回空列表（不暴露其他用户在 KB-X 中的调用记录）

