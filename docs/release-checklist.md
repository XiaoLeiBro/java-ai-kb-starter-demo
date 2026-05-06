# 发布前检查清单

> 用于每次提交、录制演示视频或对外发给潜在客户前做最后确认。

## 1. 提交边界

允许提交：

```text
README.md
docs/quick-start.md
docs/paid-version.md
docs/customer-facing-intro.md
docs/demo-script.md
docs/commercial-roadmap.md
docs/sample-documents/
OpenSpec specs / changes
前后端源码、测试、Docker 配置
```

不要提交：

```text
.env
真实 API Key
客户真实文档
个人商业判断草稿
本地交互上下文
```

当前 `.gitignore` 已忽略：

```text
README-商业化.md
codex-企业知识库商业化交互上下文.md
docs/context.md
```

## 2. 本地校验命令

提交前建议执行：

```bash
openspec validate --specs --strict
openspec validate --changes --strict
./mvnw -q test
cd frontend
npm run build
```

Docker 端到端验证：

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:18080/api/v1/health
```

如果 `.env` 修改了 `APP_PORT` 或 `WEB_PORT`，以本机实际端口为准。

## 3. 演示流程

1. 登录或注册演示用户。
2. 创建一个业务化命名的知识库。
3. 上传 Markdown / TXT / 文本型 PDF 文档。
4. 等待文档状态变为 `READY`。
5. 输入问题，按 Enter 发起查询，Shift + Enter 换行。
6. 检查回答是否为自然中文段落，不出现 Markdown 控制符。
7. 检查引用片段不展示 `#0`、`#3` 这类切片后缀。
8. 点击“下载原文件”，确认浏览器开始下载。
9. 查看调用记录，确认知识库显示名称、耗时为秒、日期格式为 `YYYY-MM-DD`。

## 4. 当前已知边界

免费 Demo 当前支持文本型 PDF，不支持扫描件 OCR。

真实 RAG 问答依赖可用的大模型和 Embedding 服务配置。模型名、base-url、向量维度需要以服务商当前文档为准。

当前鉴权、审计、限流、成本统计和部署方案仍是 Demo 级，不建议直接暴露到公网或用于客户生产环境。
