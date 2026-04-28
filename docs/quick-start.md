# 快速启动

## 1. 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 21+ | 推荐 Temurin 21 |
| Maven | 3.9+ | 也可以使用项目内 `mvnw` |
| Docker | 最新稳定版 | 拉起 PostgreSQL + Redis |
| Docker Compose | v2+ | 推荐使用 Docker Desktop 自带版本 |

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

## 3. 启动本地依赖

在项目根目录执行：

```bash
docker compose up -d
```

查看容器状态：

```bash
docker compose ps
```

默认会启动：

```text
PostgreSQL：localhost:15432
Redis：localhost:16379
```

Demo 默认数据库账号仅用于本地体验：

```text
database: ai_kb
username: ai_kb
password: ai_kb_demo_2026
```

> 不要把 Demo 默认密码用于公网或生产环境。

---

## 4. 配置应用

复制开发环境配置文件：

```bash
cp src/main/resources/application-dev.yml.example \
   src/main/resources/application-dev.yml
```

编辑 `application-dev.yml`，至少修改 JWT Secret。要跑通上传、向量化、RAG 问答、对话历史和调用记录主链路，需要填入真实模型配置：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-your-real-key-here
      base-url: https://api.siliconflow.cn/v1
      model-name: Qwen/Qwen2.5-7B-Instruct
    embedding-model:
      api-key: sk-your-real-key-here
      base-url: https://api.siliconflow.cn/v1
      model-name: BAAI/bge-m3

ai-kb:
  chat:
    max-list-results: 100
  security:
    jwt:
      secret: your-jwt-secret-at-least-32-bytes
```

建议用环境变量传入 API Key，避免真实密钥写入本地配置文件：

```bash
export AI_KB_LLM_API_KEY="你的 API Key"
export AI_KB_EMBEDDING_API_KEY="你的 API Key"
export AI_KB_CHAT_MAX_LIST_RESULTS="100"
```

> `application-dev.yml` 应加入 `.gitignore`，不要提交真实 API Key。

---

## 5. 启动应用

使用 Maven Wrapper：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

或者在 IDE 中直接运行：

```text
AiKbApplication
```

如需启用开发 profile，设置：

```text
spring.profiles.active=dev
```

---

## 6. 验证服务

执行：

```bash
curl http://localhost:8080/api/v1/health
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
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "password": "demo123456",
    "email": "demo@example.com"
  }'
```

### 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "password": "demo123456"
  }'
```

### 查询当前用户

```bash
curl http://localhost:8080/api/v1/auth/me \
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
应用服务: 8080
```

如果冲突，修改 `docker-compose.yml` 或 `application-dev.yml` 中的端口配置。

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
