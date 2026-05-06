# 快速启动

## 1. 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| Docker | 最新稳定版 | 拉起 Vue 前端、Spring Boot 后端、PostgreSQL、Redis |
| Docker Compose | v2+ | 推荐使用 Docker Desktop 自带版本 |

> Docker Compose 启动路径不要求本机安装 JDK 或 Maven；应用镜像会在 Docker 构建阶段完成 Maven 编译。

---

## 2. 获取大模型 API Key

用户注册、登录、知识库元数据和健康检查可以离线运行，不需要真实大模型 API Key。

上传文档后的 Embedding、RAG 问答、带会话问答和调用记录主链路会使用 **OpenAI Compatible API**。你可以选择任意兼容 OpenAI 接口的大模型服务。

常见选择：

| 服务商 | 地址 | 说明 |
|---|---|---|
| SiliconFlow（硅基流动） | https://siliconflow.cn | 支持多种开源模型与 OpenAI Compatible API |
| DeepSeek | https://platform.deepseek.com | 提供 Chat Completions 类接口 |
| 阿里通义 DashScope | https://dashscope.console.aliyun.com | 可通过兼容接口或适配层接入 |
| 智谱 BigModel | https://bigmodel.cn | GLM 系列模型服务 |
| 其他 OpenAI Compatible Provider | 按服务商文档配置 | 只要兼容 `/v1` 风格接口即可 |

本地跑通 RAG 主流程需要准备：

```text
api-key
base-url
聊天模型名
Embedding 模型名
```

示例：

```text
base-url: https://api.siliconflow.cn/v1
chat-model: Qwen/Qwen2.5-7B-Instruct
embedding-model: BAAI/bge-m3
```

> 注意：不同服务商的模型名称、向量维度、usage 字段可能不同，请以服务商文档为准。

---

## 3. 配置 Docker Compose 环境

在项目根目录执行：

```bash
cp .env.example .env
```

编辑 `.env`，至少替换以下 6 个模型配置：

```text
AI_KB_LLM_API_KEY=your-chat-api-key-here
AI_KB_LLM_BASE_URL=https://api.siliconflow.cn/v1
AI_KB_CHAT_MODEL=Qwen/Qwen2.5-7B-Instruct

AI_KB_EMBEDDING_API_KEY=your-embedding-api-key-here
AI_KB_EMBEDDING_BASE_URL=https://api.siliconflow.cn/v1
AI_KB_EMBEDDING_MODEL=BAAI/bge-m3
```

如果只想验证应用启动，可以暂时保留占位值；上传后的向量化和真实 AI 问答需要有效 API Key。不同服务商的模型名称、base-url 和向量维度可能不同，请以服务商当前文档为准。

---

## 4. 一键启动全部服务

```bash
docker compose up -d
```

首次启动会自动构建 `ai-kb-demo:latest` 后端镜像和 `ai-kb-demo-web:latest` 前端镜像，并拉起 Vue 前端、Spring Boot 后端、PostgreSQL 和 Redis。

查看容器状态：

```bash
docker compose ps
```

默认会启动：

```text
Vue 前端：http://localhost:18081
Spring Boot 后端：http://localhost:18080
Swagger UI：http://localhost:18080/swagger-ui.html
PostgreSQL：localhost:15432
Redis：localhost:16379
```

> `18080` 是后端应用端口，Swagger 只是后端应用中的接口文档页面；前端页面使用 `18081`。

Demo 默认数据库账号仅用于本地体验：

```text
database: ai_kb
username: ai_kb
password: ai_kb_demo_2026
```

> 不要把 Demo 默认密码用于公网或生产环境。

---

## 5. 本地开发启动（可选）

如果你不想使用应用容器，也可以只启动 PostgreSQL/Redis 后在 IDE 中运行应用。

```bash
docker compose up -d postgres redis
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

本地开发配置模板仍保留在：

```text
src/main/resources/application-dev.yml.example
```

---

## 6. 验证服务

执行：

```bash
curl http://localhost:18080/api/v1/health
curl http://localhost:18081/api/v1/health
```

预期返回类似：

```json
{
  "status": "UP",
  "service": "ai-kb-demo-server",
  "version": "0.1.0-SNAPSHOT"
}
```

---

## 7. 注册与登录

### 注册用户

```bash
curl -X POST http://localhost:18080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "password": "demo123456",
    "email": "demo@example.com"
  }'
```

### 登录获取 Token

```bash
curl -X POST http://localhost:18080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "password": "demo123456"
  }'
```

### 查询当前用户

```bash
curl http://localhost:18080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 8. 常见问题

### Q：启动时报找不到向量扩展？

A：确认 PostgreSQL 镜像已经安装 pgvector，并执行：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

如果使用项目提供的 `docker-compose.yml`，通常会在初始化脚本中完成。

---

### Q：端口冲突怎么办？

A：默认宿主机端口：

```text
PostgreSQL: 15432
Redis: 16379
Spring Boot 后端: 18080
Vue 前端: 18081
```

如果冲突，优先修改 `.env` 中的宿主机端口：

```text
APP_PORT=28080
WEB_PORT=28081
PG_PORT=25432
REDIS_PORT=26379
```

然后重新执行：

```bash
docker compose up -d
```

---

### Q：首次启动很慢？

A：首次启动会下载 Maven 依赖和 Docker 镜像，耗时取决于网络环境。  
后续启动会明显变快。

---

### Q：模型返回 401 / 403？

A：这是 RAG / 对话问答主流程接入模型后的问题。通常是以下原因：

```text
API Key 未配置
API Key 无效
base-url 填错
模型名称不正确
服务商账号未开通对应模型
```

先用服务商文档中的 curl 示例验证 API Key，再回到本项目配置。

---

### Q：Embedding 维度不匹配？

A：这是接入 Embedding 后的问题。Embedding 模型不同，向量维度可能不同。  
需要同步修改：

```text
向量表字段维度
应用配置中的 vector dimension
Embedding 模型名称
```

如果表已经创建过，需要清空或重建向量表。

---

### Q：可以部署到公网体验吗？

A：不建议直接把免费 Demo 暴露到公网。  
如果必须公网部署，请至少补充：

```text
HTTPS
强 JWT Secret
登录失败限流
接口频率限制
数据库访问控制
API Key 环境变量管理
日志脱敏
```

免费 Demo 默认面向本地学习，不默认承担公网安全边界。
