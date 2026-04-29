# AI 知识库 Demo 前端

Vue 3 + TypeScript + Element Plus 的最小商业 Demo 控制台。

## 本地开发

```bash
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

Vite 开发服务会把 `/api`、`/actuator`、`/swagger-ui.html` 代理到后端 `http://localhost:18080`。

## Docker Compose

Compose 启动后访问：

```text
前端：http://localhost:18081
后端：http://localhost:18080
Swagger：http://localhost:18080/swagger-ui.html
```

前端容器内部通过 Nginx 把 `/api` 代理到后端 app service。

## 功能范围

- 登录 / 注册
- 知识库列表
- 创建知识库
- 上传文档
- 知识库 AI 问答
- 对话会话
- 调用记录查询
