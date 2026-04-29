## Why

当前 Demo 需要用户手动配置 PostgreSQL、Redis 并本地运行 Spring Boot 应用，体验门槛较高。作为商业 Demo，"一键启动即跑"是降低试用摩擦、提升转化率的关键体验。配合运行截图和演示视频，用户可以在不写一行代码的情况下直观了解产品能力。

## What Changes

- 新增 Docker Compose 编排，包含 Vue 前端 + Spring Boot 应用 + PostgreSQL + Redis，实现 `docker compose up` 一键启动
- 新增多阶段 `Dockerfile`，由 Docker Compose 自动构建轻量级应用镜像，不要求用户本机安装 JDK / Maven
- 新增 `frontend/`，提供 Vue 3 + TypeScript + Element Plus 的最小商业 Demo 控制台
- 更新 README，加入一键启动指引、技术可信截图路径、商业演示截图路径和视频路径
- 新增 `docs/screenshots/technical/` 目录存放 Docker Compose、健康检查、Swagger 等技术可信截图
- 新增 `docs/screenshots/business/` 目录存放登录、知识库、上传、问答、调用记录等商业演示截图
- 新增 `docs/demo/videos/` 目录存放演示视频，`docs/demo/README.md` 维护外部视频链接位置
- 此能力**免费版包含**，是 Demo 体验的核心组成部分

## Capabilities

### New Capabilities
- `docker-deployment`: Docker Compose 一键部署能力，覆盖 Vue 前端 + PostgreSQL + Redis + Spring Boot 应用的容器编排、健康检查、数据持久化
- `demo-assets`: 项目演示资产，包括技术可信截图、商业演示截图和演示视频/脚本

### Modified Capabilities
无

## Impact

- 新增 `Dockerfile` 和 `.dockerignore`
- 新增 `frontend/` 前端工程和前端 Dockerfile
- 修改 `docker-compose.yml`（从仅基础设施到包含应用）
- 修改 `README.md`（新增一键启动指引）
- 新增 `docs/screenshots/technical/`、`docs/screenshots/business/` 和 `docs/demo/videos/` 目录
