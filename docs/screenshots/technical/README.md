# Technical Screenshots

这里存放 v0.4 技术可信截图，用于证明 Docker Compose 一键启动和基础服务可用。

| 截图 | 文件名                         | 截图内容 |
|---|-----------------------------|---|
| Docker Compose 容器状态 | `01-compose-ps-healthy.jpg` | 终端执行 `docker compose ps`，展示 app、postgres、redis 为 healthy |
| Actuator 健康检查 | `02-actuator-health-up.jpg` | 浏览器访问 `http://localhost:18080/actuator/health`，返回 `status: UP` |
| 中文 Swagger API 文档 | `03-swagger-cn-api.jpg`     | 浏览器访问 `http://localhost:18080/swagger-ui.html`，展示中文 API 分组和接口说明 |

不要在截图中展示真实 API Key、数据库密码或 Redis 密码。
