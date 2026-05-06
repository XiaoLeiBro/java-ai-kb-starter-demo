# Demo Video

真实演示视频请在完成端到端验证后录制。

视频目录：[videos](videos)

建议文件：

- `videos/v0.4-docker-compose-commercial-demo.mp4`: 不超过 5 分钟

## 推荐录制流程

1. 在项目根目录展示 `.env` 已由 `.env.example` 复制生成，但不要展示真实 API Key。
2. 执行 `docker compose up -d --build`，展示一键构建并启动。
3. 执行 `docker compose ps`，展示 app、postgres、redis 为 healthy。
4. 打开 `http://localhost:18080/swagger-ui.html`，快速展示中文 API 文档。
5. 打开前端页面 `http://localhost:18081`，注册或登录用户。
6. 创建知识库。
7. 上传 `docs/sample-documents/公司年假制度.md`、`docs/sample-documents/客服售后规则.md`、`docs/sample-documents/产品介绍.md` 或文本型 PDF。
8. 等待文档状态变为 `READY`。
9. 提问并得到 AI 回答，展示带自然段落的中文结果和引用片段。
10. 点击引用片段对应的“下载原文件”，验证可追溯到原始资料。
11. 打开调用记录页面，展示知识库名称、模型名、状态、秒级耗时、Token 和 `YYYY-MM-DD` 日期。
12. 结尾展示一句话结论：Docker Compose 一键启动后，可以完成知识库创建、文档上传、RAG 问答、来源追溯、原文件下载、对话历史和调用记录查看。

详细客户演示话术见：[../demo-script.md](../demo-script.md)

## 外部视频链接

如果视频超过仓库限制或不适合提交到 Git，请使用外部托管，并在这里维护链接：

| 视频 | 链接 |
|---|---|
| v0.4 Docker Compose + 商业流程演示 | 待补充 |
