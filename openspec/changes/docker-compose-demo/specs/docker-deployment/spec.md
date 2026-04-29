## ADDED Requirements

### Requirement: Docker Compose 一键启动

系统 MUST 提供 `docker compose up` 命令，一条命令自动构建并启动全部服务（Vue 前端、Spring Boot 应用、PostgreSQL、Redis）。PostgreSQL 和 Redis 应在应用启动前通过健康检查。后端应用端口 8080 映射到宿主机 18080，前端 Web 端口 80 映射到宿主机 18081。

#### Scenario: 一键启动

- **WHEN** 用户在项目根目录执行 `docker compose up -d`
- **THEN** 应用镜像自动构建
- **AND** PostgreSQL、Redis、Spring Boot 应用和 Vue 前端自动启动
- **AND** PostgreSQL 和 Redis 通过健康检查后应用才开始启动

#### Scenario: 应用等待依赖

- **WHEN** 用户执行 `docker compose up -d` 且 PostgreSQL 仍在启动中
- **THEN** 应用服务等待 PostgreSQL 健康检查通过后才启动

#### Scenario: 应用端口可访问

- **WHEN** 所有服务启动完成且应用健康检查通过
- **THEN** 用户可通过 `http://localhost:18081` 访问前端页面
- **AND** 用户可通过 `http://localhost:18080` 访问后端应用
- **AND** Swagger UI 在 `http://localhost:18080/swagger-ui.html` 可正常打开

#### Scenario: 前端代理后端 API

- **WHEN** 用户访问 `http://localhost:18081` 并执行登录、创建知识库、上传文档、AI 问答或调用记录查询
- **THEN** 前端通过 `/api` 路径代理到后端 app service
- **AND** 浏览器不需要直接配置后端 API 地址

#### Scenario: 停止全部服务

- **WHEN** 用户执行 `docker compose down`
- **THEN** 全部容器停止，应用容器被移除，但数据卷保留

#### Scenario: 停止全部服务并清理数据

- **WHEN** 用户执行 `docker compose down -v`
- **THEN** 全部容器停止且数据卷被移除，恢复干净初始状态

#### Scenario: 端口冲突

- **WHEN** 宿主机 18080 端口已被占用
- **THEN** docker compose 启动失败并输出端口冲突错误
- **AND** 用户可在 `.env` 中修改 `APP_PORT` 变量覆盖默认端口

### Requirement: 应用容器镜像

系统 MUST 通过 Docker Compose `build` 配置为 Spring Boot 应用构建轻量级 OCI 镜像。镜像 MUST 基于 JRE 运行时（非完整 JDK）运行，且构建过程不得要求用户本机安装 JDK 或 Maven。

#### Scenario: 通过 Docker Compose 构建应用镜像

- **WHEN** 用户执行 `docker compose up -d`
- **THEN** Docker Compose 自动构建 `ai-kb-demo:latest`
- **AND** 构建完成后应用容器自动启动

#### Scenario: 构建失败

- **WHEN** 用户执行 `docker compose up -d` 且构建过程中出现错误（如依赖下载失败、Docker daemon 不可用）
- **THEN** Docker Compose 以非零退出码结束，输出可读的错误信息
- **AND** 用户修复问题后重新执行相同命令即可重试

### Requirement: 数据持久化

PostgreSQL 和 Redis 数据 MUST 通过 Docker 命名卷在容器重启后持久保存。

#### Scenario: 数据在容器重启后保留

- **WHEN** 用户执行 `docker compose down` 停止服务后再执行 `docker compose up -d`
- **THEN** 之前创建的知识库和文档仍然可用

#### Scenario: 清理数据重置

- **WHEN** 用户执行 `docker compose down -v` 后再执行 `docker compose up -d`
- **THEN** 全部数据重置为初始状态，数据库在首次启动时执行 Flyway 迁移

### Requirement: 通过 .env 文件管理环境配置

敏感配置（LLM API Key、数据库密码等） MUST 从 `.env` 文件加载，该文件不得提交到版本控制。项目 MUST 提供 `.env.example` 模板文件。

`.env.example` 应包含以下变量及其默认值：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `AI_KB_LLM_API_KEY` | `your-chat-api-key-here` | Chat 模型 API Key |
| `AI_KB_LLM_BASE_URL` | `https://api.deepseek.com` | Chat 模型 OpenAI Compatible Base URL |
| `AI_KB_CHAT_MODEL` | `deepseek-v4-flash` | Chat 模型名称 |
| `AI_KB_EMBEDDING_API_KEY` | `your-embedding-api-key-here` | Embedding 模型 API Key |
| `AI_KB_EMBEDDING_BASE_URL` | `https://api.siliconflow.cn/v1` | Embedding 模型 OpenAI Compatible Base URL |
| `AI_KB_EMBEDDING_MODEL` | `BAAI/bge-m3` | Embedding 模型名称 |
| `SPRING_DATASOURCE_PASSWORD` | `ai_kb_demo_2026` | PostgreSQL 密码 |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/ai_kb` | PostgreSQL JDBC URL |
| `SPRING_REDIS_HOST` | `redis` | Redis 主机名 |
| `APP_PORT` | `18080` | 宿主机应用访问端口 |
| `WEB_PORT` | `18081` | 宿主机前端访问端口 |
| `PG_PORT` | `15432` | 宿主机 PostgreSQL 端口 |
| `REDIS_PORT` | `16379` | 宿主机 Redis 端口 |

#### Scenario: 从 .env 文件加载配置

- **WHEN** 用户将 `.env.example` 复制为 `.env` 并填写 Chat / Embedding 的 6 个模型参数
- **AND** 用户执行 `docker compose up -d`
- **THEN** 应用使用 `.env` 中的配置启动

#### Scenario: 缺少 .env 文件

- **WHEN** 用户在没有 `.env` 文件的情况下执行 `docker compose up -d`
- **THEN** docker-compose 使用 `.env.example` 中的默认值
- **AND** PostgreSQL 和 Redis 正常启动
- **AND** 应用启动但 LLM 功能不可用（API Key 为占位符）

#### Scenario: 填写模型配置即可运行

- **WHEN** 用户将 `.env.example` 中的 Chat / Embedding API Key、Base URL 和模型名称替换为真实值
- **THEN** 数据库和端口等默认配置无需修改即可使 Demo 完整运行

### Requirement: 应用健康检查

Spring Boot 应用容器 MUST暴露健康检查端点，docker-compose 使用它来验证应用是否就绪。

#### Scenario: 应用健康检查通过

- **WHEN** Spring Boot 应用完全启动
- **THEN** 健康检查端点返回 HTTP 200
- **AND** docker-compose 将服务标记为健康
