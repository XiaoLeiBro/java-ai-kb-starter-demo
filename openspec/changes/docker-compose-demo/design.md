## Context

当前项目已有 `docker-compose.yml`，但仅包含 PostgreSQL + Redis 两个基础设施服务。Spring Boot 应用需要用户本地配置 JDK 21、安装依赖、配置环境变量后手动运行。作为商业 Demo，这意味着潜在用户需要 5-10 分钟的配置才能看到效果，体验门槛较高。

## Goals / Non-Goals

**Goals:**
- `docker compose up -d` 一键启动全部服务（应用 + PostgreSQL + Redis）
- 应用容器镜像轻量、快速启动（目标 < 60s）
- 数据持久化，重启后不丢失
- 提供完整的 README 启动指引、截图和演示视频

**Non-Goals:**
- 不做 Kubernetes / 多节点编排
- 不做 CI/CD 流水线集成
- 不做生产级监控、日志聚合
- 不做多环境（dev/staging/prod）Docker 配置分离

## Decisions

### 1. 使用多阶段 Dockerfile 让 Compose 自动构建应用镜像

**决策**: 使用 `Dockerfile` 多阶段构建，`docker-compose.yml` 的 `app` 服务配置 `build:`，让 `docker compose up -d` 自动构建并启动应用镜像。

**理由**:
- 用户只需要 Docker Compose，不需要本机安装 JDK / Maven
- `docker compose up -d` 可以从源码直接完成构建和启动，符合一键启动体验
- 构建阶段使用 Maven 镜像，运行阶段使用 JRE 镜像，避免把完整构建工具带入运行镜像
- Dockerfile 显式可读，便于商业交付场景排查和二开

**备选方案**: 使用 Jib Maven 插件。优势是分层优化和 Maven 集成好，劣势是用户必须先执行 `mvn compile jib:dockerBuild`，不满足真正的 Docker Compose 一键启动。

### 2. docker-compose 包含应用服务，使用 depends_on + healthcheck

**决策**: 在现有 docker-compose.yml 中新增 `app` service，通过 `depends_on` 依赖 PostgreSQL 和 Redis 的健康检查。

**理由**:
- 用户一条命令启动全部，无需额外步骤
- healthcheck 确保应用启动时数据库已就绪
- 保持单机部署的简单性

### 3. 应用配置通过 environment 注入

**决策**: docker-compose 中通过 `environment` 覆盖 Spring Boot 配置（数据库 URL、密码、LLM API Key 等），而非挂载配置文件。

**理由**:
- 环境变量的优先级高于 YAML 配置，覆盖简单直接
- API Key 等敏感信息通过 `.env` 文件管理，不提交到 git

### 4. 应用暴露端口映射到宿主机

**决策**: 应用容器内部端口 8080 映射到宿主机 `18080`，与 PostgreSQL（15432）、Redis（16379）保持一致的端口偏移风格。

**理由**:
- 避免与宿主机上可能运行的 8080 端口冲突
- 与已有服务的端口映射风格一致（host port = container port + 10000）
- Swagger UI 可通过 `http://localhost:18080/swagger-ui.html` 访问

### 4A. 前端 Web 服务

**决策**: 新增 `web` service，构建 `frontend/`，运行阶段使用 Nginx，容器内部端口 80 映射到宿主机 `18081`。

**理由**:
- `18080` 保持为后端应用端口，Swagger 只是后端应用中的文档页面
- `18081` 作为商业 Demo 前端入口，适合截图和录屏
- Nginx 将 `/api` 代理到 Compose 内部的 `app:8080`，浏览器无需配置后端地址，也避免 CORS 问题

### 5. 截图和视频存放在仓库中

**决策**: `docs/screenshots/technical/`、`docs/screenshots/business/` 和 `docs/demo/videos/` 目录存入仓库。

**理由**:
- 技术可信截图和商业演示截图用途不同，分目录可以避免交付物混乱
- Demo 截图资产量小，仓库可承受
- 用户 clone 即可看到截图目录和命名规范
- 视频若过大或不适合提交到 Git，改为使用外部托管链接

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 首次 Compose 启动需要下载 Maven 依赖和基础镜像 | README 中明确首次启动耗时取决于网络，建议配置 Docker 镜像加速器 |
| 容器启动慢（JVM 冷启动） | 使用 `eclipse-temurin:21-jre` 基础镜像，关闭不必要的 Spring Boot devtools |
| API Key 需要用户自行配置 | 提供 `.env.example` 模板，README 中说明获取方式 |
| 截图/视频增加仓库体积 | 图片压缩，视频超过 10MB 使用外部托管 |
