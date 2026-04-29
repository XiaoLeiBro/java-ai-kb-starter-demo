## 1. 应用镜像构建

- [x] 1.1 新增多阶段 `Dockerfile`：构建阶段使用 Maven + JDK 21，运行阶段使用 `eclipse-temurin:21-jre`
- [x] 1.2 新增 `.dockerignore`，避免 `.env`、`target/`、`data/`、IDE 文件进入构建上下文
- [x] 1.3 验证 `docker compose up -d` 能自动构建 `ai-kb-demo:latest` 并启动应用

## 2. Docker Compose 应用服务

- [x] 2.1 修改 `docker-compose.yml`，新增 `app` service：`build: .` + 镜像 `ai-kb-demo:latest`，端口映射 `${APP_PORT:-18080}:8080`
- [x] 2.2 配置 `depends_on` 让 app 服务等待 PostgreSQL 和 Redis 健康检查通过
- [x] 2.3 配置 Spring Boot 健康检查端点（`/actuator/health`）在 docker-compose 中，interval 30s，retries 10
- [x] 2.4 创建 `.env.example` 模板，包含：`AI_KB_LLM_API_KEY`、`AI_KB_LLM_BASE_URL`、`AI_KB_CHAT_MODEL`、`AI_KB_EMBEDDING_API_KEY`、`AI_KB_EMBEDDING_BASE_URL`、`AI_KB_EMBEDDING_MODEL`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATASOURCE_URL`、`SPRING_REDIS_HOST`、`APP_PORT`、`WEB_PORT`、`PG_PORT`、`REDIS_PORT` 及默认值
- [x] 2.5 确保 `.env` 在 `.gitignore` 中（验证：`git status` 不显示 `.env` 为 untracked）
- [x] 2.6 验证 `docker compose up -d` 全部服务启动后，`curl http://localhost:18080/actuator/health` 返回 200
- [x] 2.7 新增 `web` service：`build: ./frontend` + 镜像 `ai-kb-demo-web:latest`，端口映射 `${WEB_PORT:-18081}:80`
- [x] 2.8 配置 web service 等待 app 健康检查通过，并通过 Nginx 将 `/api` 代理到 app service
- [x] 2.9 验证 `curl http://localhost:18081/api/v1/health` 能通过前端代理访问后端

## 3. 应用配置适配

- [x] 3.1 确保 `application.yml` 支持通过 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_REDIS_HOST` 环境变量覆盖
- [x] 3.2 确认 `spring-boot-starter-actuator` 依赖已存在，`/actuator/health` 端点默认启用
- [x] 3.3 验证容器内应用能正确连接 PostgreSQL 和 Redis（检查启动日志无连接错误）

## 3A. Vue 前端

- [x] 3A.1 创建 `frontend/`，使用 Vue 3 + TypeScript + Element Plus + Vite
- [x] 3A.2 实现登录 / 注册、知识库列表、创建知识库、文档上传、AI 问答、会话和调用记录页面
- [x] 3A.3 创建 `frontend/Dockerfile`，构建阶段使用 Node，运行阶段使用 Nginx
- [x] 3A.4 创建 `frontend/nginx.conf`，支持 SPA fallback 和 `/api` 反向代理
- [x] 3A.5 验证 `npm run build` 通过
- [x] 3A.6 使用浏览器打开 `http://localhost:18081`，验证前端登录和创建知识库交互成功

## 4. README 更新

- [x] 4.1 在 README 中添加"快速启动"章节，分步指引：
  - 前置条件：Docker Desktop（4GB+ 内存分配）
  - Step 1: `cp .env.example .env` 并填写 Chat / Embedding 的 6 个模型参数
  - Step 2: `docker compose up -d` 自动构建并启动全部服务
  - Step 3: 访问 `http://localhost:18081` 使用前端 Demo；访问 `http://localhost:18080/swagger-ui.html` 查看 Swagger
- [x] 4.2 在 README 中添加截图展示区域（截图占位，待步骤 5 替换为实际图片）

## 5. 截图与演示视频

- [x] 5.1 创建 `docs/screenshots/` 目录
- [x] 5.2 创建技术可信截图目录 `docs/screenshots/technical/`，维护 Docker Compose、Actuator、Swagger 中文文档截图文件名
- [x] 5.3 创建商业演示截图目录 `docs/screenshots/business/`，维护登录、知识库、上传、问答、调用记录截图文件名
- [ ] 5.4 截取技术可信截图：
  - `docs/screenshots/technical/01-compose-ps-healthy.jpg`
  - `docs/screenshots/technical/02-actuator-health-up.jpg`
  - `docs/screenshots/technical/03-swagger-cn-api.jpg`
- [ ] 5.5 截取商业演示截图：
  - `docs/screenshots/business/01-login.jpg`
  - `docs/screenshots/business/02-knowledge-base-list.jpg`
  - `docs/screenshots/business/03-create-knowledge-base.jpg`
  - `docs/screenshots/business/04-upload-document-ready.jpg`
  - `docs/screenshots/business/05-ai-qa-with-references.jpg`
  - `docs/screenshots/business/06-invocation-logs.jpg`
- [x] 5.6 创建 `docs/demo/` 目录
- [x] 5.7 创建演示视频目录 `docs/demo/videos/`，维护 `v0.4-docker-compose-commercial-demo.mp4` 文件名和外部链接位置
- [ ] 5.8 录制演示视频 `docs/demo/videos/v0.4-docker-compose-commercial-demo.mp4`（MP4 格式，建议 ≤5 分钟），覆盖 `docker compose up -d --build` → 容器 healthy → 中文 Swagger → 前端登录 → 创建知识库 → 上传文档 → 提问 → 调用记录；如文件超过仓库限制，上传至外部托管并在 `docs/demo/README.md` 中提供链接

## 6. 验证

- [ ] 6.1 完整走一遍 clone → `cp .env.example .env` → 填写 Chat / Embedding 模型配置 → `docker compose up -d` → 创建知识库 → 上传文档 → 问答，确认端到端可用
- [ ] 6.2 验证 `docker compose down` 后数据保留（重新启动知识库仍在），`docker compose down -v` 后数据清空（数据库回到 Flyway 初始化状态）
- [ ] 6.3 验证端口冲突场景：占用 18080 端口后启动，确认报错清晰；修改 `.env` 中 `APP_PORT=28080` 后重新启动成功
