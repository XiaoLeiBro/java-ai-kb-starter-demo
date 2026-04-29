## ADDED Requirements

### Requirement: 项目截图

项目 MUST 在 `docs/screenshots/` 目录中维护截图交付位，分为技术可信截图和商业演示截图。技术可信截图 MUST 放在 `docs/screenshots/technical/`；商业演示截图 MUST 放在 `docs/screenshots/business/`。截图 MUST 在 README.md 中引用并展示。

#### Scenario: 截图目录存在

- **WHEN** 用户查看 `docs/screenshots/` 目录
- **THEN** 目录中包含 `technical/` 和 `business/` 子目录

#### Scenario: 技术可信截图交付位存在

- **WHEN** 用户查看 `docs/screenshots/technical/` 目录
- **THEN** 目录说明中列出 Docker Compose 容器状态、Actuator 健康检查、中文 Swagger API 文档截图文件名

#### Scenario: 商业演示截图交付位存在

- **WHEN** 用户查看 `docs/screenshots/business/` 目录
- **THEN** 目录说明中列出登录、知识库列表、创建知识库、上传文档、AI 问答、调用记录截图文件名

#### Scenario: 截图在 README 中引用

- **WHEN** 用户打开 README.md
- **THEN** 在专门章节中引用技术可信截图和商业演示截图的目标路径

### Requirement: 演示视频

项目 MUST 在 `docs/demo/` 目录中维护演示视频交付位。视频文件 SHOULD 放在 `docs/demo/videos/`，推荐文件名为 `v0.4-docker-compose-commercial-demo.mp4`，展示从项目启动到成功问答的完整用户流程。若视频文件过大或不适合提交到 Git，改为在 `docs/demo/README.md` 中提供外部托管链接。

#### Scenario: 演示视频存在

- **WHEN** 用户查看 `docs/demo/` 目录
- **THEN** 目录中存在 `videos/` 子目录，且 `README.md` 说明推荐视频文件名或外部托管链接位置

#### Scenario: 演示视频覆盖完整用户流程

- **WHEN** 用户观看演示视频
- **THEN** 视频展示：`docker compose up -d --build` 自动构建并启动、容器 healthy、中文 Swagger、前端登录、创建知识库、上传文档、提问并获得 AI 回答、查看调用记录

### Requirement: README 快速启动章节

README.md MUST 包含"快速启动"章节，包含分步指引：前置条件（Docker、4GB+ 可用内存）、配置（.env）、`docker compose up -d` 一键启动命令和预期结果。

#### Scenario: README 包含快速启动

- **WHEN** 用户打开 README.md
- **THEN** 存在"快速启动"章节，包含：前置条件（Docker）、配置步骤、启动命令和预期结果

#### Scenario: 快速启动自包含

- **WHEN** 新用户完全按照 README 快速启动章节操作
- **THEN** 用户可以在 10 分钟内完成从 clone 到看到 Demo 运行的全流程
