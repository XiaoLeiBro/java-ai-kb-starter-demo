# Screenshots

真实截图请在完成 `docker compose up -d --build` 端到端验证后生成。

截图分为两类：

## 技术可信截图

目录：[technical](technical)

| 截图 | 文件名 | 用途 |
|---|---|---|
| Docker Compose 容器状态 | `technical/01-compose-ps-healthy.jpg` | 证明 app、postgres、redis 已启动并 healthy |
| Actuator 健康检查 | `technical/02-actuator-health-up.jpg` | 证明后端服务返回 `UP` |
| 中文 Swagger API 文档 | `technical/03-swagger-cn-api.jpg` | 证明 API 文档已中文化且可访问 |

## 商业演示截图

目录：[business](business)

| 截图 | 文件名 | 用途 |
|---|---|---|
| 登录 / 进入系统 | `business/01-login.jpg` | 展示用户进入系统 |
| 知识库列表 | `business/02-knowledge-base-list.jpg` | 展示已有知识库和管理入口 |
| 创建知识库 | `business/03-create-knowledge-base.jpg` | 展示创建知识库流程 |
| 上传文档并处理完成 | `business/04-upload-document-ready.jpg` | 展示文档上传、解析、切分完成 |
| AI 问答与引用片段 | `business/05-ai-qa-with-references.jpg` | 展示 RAG 问答效果和引用来源 |
| 调用记录 / 成本追踪入口 | `business/06-invocation-logs.jpg` | 展示模型、耗时、Token、状态等记录 |

截图生成或文件名调整后，需要同步更新根目录 `README.md` 的截图章节。
