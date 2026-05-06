# 用户认证规范

## Purpose

覆盖 v0.1 用户注册、登录、JWT 鉴权、当前用户查询和登出接口，作为免费 Demo 的身份认证基础能力规范。

## Requirements

### Requirement: 用户注册

系统 SHALL 允许新用户通过用户名、密码和可选邮箱进行注册。

#### Scenario: 使用有效凭证注册

- **Given** 用户未登录
- **When** 用户请求 `POST /api/v1/auth/register`，提供合法的 `username`（3-20 位，字母、数字、下划线）、`password`（至少 8 位）和可选的 `email`
- **Then** 系统创建用户，返回 HTTP 200 和用户基础信息

#### Scenario: 使用重复用户名注册

- **Given** 用户已存在
- **When** 新用户使用相同的用户名请求注册
- **Then** 系统返回 HTTP 409，错误码 `USERNAME_EXISTS`

#### Scenario: 使用无效参数注册

- **Given** 用户未登录
- **When** 用户请求注册，`username` 或 `password` 不符合规则
- **Then** 系统返回 HTTP 400，错误码 `VALIDATION_ERROR`

### Requirement: 用户登录

系统 SHALL 允许已注册用户通过用户名和密码进行认证，获取 JWT token。

#### Scenario: 使用有效凭证登录

- **Given** 用户已注册且状态正常
- **When** 用户请求 `POST /api/v1/auth/login`，提供正确的 `username` 和 `password`
- **Then** 系统返回 HTTP 200，包含 JWT token 和用户基础信息

#### Scenario: 使用无效凭证登录

- **Given** 用户已注册或不存在
- **When** 用户请求登录，但用户名不存在、密码错误或用户已禁用
- **Then** 系统返回 HTTP 401，错误码 `INVALID_CREDENTIALS`

### Requirement: 当前用户查询

系统 SHALL 允许已认证用户查询自己的个人信息。

#### Scenario: 使用有效 token 查询当前用户

- **Given** 用户已登录
- **When** 用户请求 `GET /api/v1/auth/me`
- **Then** 系统返回 HTTP 200 和用户基础信息

#### Scenario: 无有效 token 查询当前用户

- **Given** 用户未登录或 token 无效
- **When** 用户请求 `GET /api/v1/auth/me`
- **Then** 系统返回 HTTP 401

### Requirement: 用户登出

系统 SHALL 提供登出接口供客户端清理 JWT。服务端无状态，不维护 session 或 token 黑名单。

#### Scenario: 使用有效 token 登出

- **Given** 用户已登录
- **When** 用户请求 `POST /api/v1/auth/logout`
- **Then** 系统返回 HTTP 204，客户端负责删除本地 JWT

### Requirement: 领域规则

系统 MUST 保持用户领域模型与基础设施实现解耦。

- `User` 是领域聚合根，不依赖 Spring、HTTP、MyBatis-Plus 或数据库注解。
- 用户 ID 由领域层通过 `UserId.generate()` 生成，持久层不得重新生成 ID。
- 注册时必须校验用户名和密码强度。
- 从数据库还原用户时使用 `User.restore(...)`，不得重新执行业务注册逻辑、重新 hash 密码或更新时间戳。
- 密码 hash 能力通过 `PasswordHasher` 接口注入，领域层不依赖 BCrypt 实现。
- Token 签发能力通过 `TokenService` 接口注入，领域层不依赖 JWT 实现。

#### Scenario: 领域模型不依赖基础设施

- **Given** 开发者查看用户领域模型
- **When** 检查 `domain.user` 下的聚合根、值对象和领域服务
- **Then** 这些类型不得依赖 Spring、HTTP、MyBatis-Plus、BCrypt 或 JWT 具体实现

### Requirement: 持久层规则

系统 MUST 将用户持久化实现放在基础设施层，并保持用户名唯一性。

- 仓储接口定义在 `domain.user.repository.UserRepository`。
- MyBatis-Plus PO、Mapper、Assembler 和 Repository 实现放在 `infrastructure.persistence.user`。
- `users.username` 必须有唯一约束。
- 注册并发下，应用层先做 `existsByUsername` 快判，持久层仍必须捕获唯一约束冲突并转换为 `BusinessException(USERNAME_EXISTS)`。

#### Scenario: 用户名唯一性由数据库兜底

- **Given** 两个注册请求使用相同用户名并发提交
- **When** 应用层快判未能提前拦截其中一个请求
- **Then** 持久层唯一约束冲突必须转换为 `BusinessException(USERNAME_EXISTS)`

### Requirement: 安全规则

系统 MUST 对匿名路径和认证路径进行明确隔离。

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 其他接口默认需要认证。
- JWT 使用无状态鉴权，服务端不维护 session。
- 默认 JWT Secret 只允许本地体验；生产 profile 使用默认 secret 时必须阻止启动。

#### Scenario: 未认证访问受保护接口

- **Given** 用户未登录
- **When** 用户请求除匿名白名单之外的接口
- **Then** 系统必须返回 HTTP 401

### Requirement: 非目标

系统 MUST 保持免费 Demo 的身份能力边界，不应把以下能力描述为当前已实现能力。

- 不做 RBAC。
- 不做组织架构或多租户。
- 不做服务端 token 黑名单。
- 不做第三方登录。
- 不做修改密码、找回密码、邮箱验证。

#### Scenario: 非目标身份能力不作为当前能力暴露

- **Given** 读者查看当前用户认证规格
- **When** 查看功能边界
- **Then** RBAC、组织架构、多租户、第三方登录、修改密码和找回密码必须被标记为非目标或后续计划
