# 用户认证规范

## Purpose

覆盖 v0.1 用户注册、登录、JWT 鉴权、当前用户查询和登出接口。

## Requirements

### Requirement: User Registration

The system SHALL allow new users to register with a username, password, and optional email.

#### Scenario: Register with valid credentials

- **Given** 用户未登录
- **When** 用户请求 `POST /api/v1/auth/register`，提供合法的 `username`（3-20 位，字母、数字、下划线）、`password`（至少 8 位）和可选的 `email`
- **Then** 系统创建用户，返回 HTTP 200 和用户基础信息

#### Scenario: Register with duplicate username

- **Given** 用户已存在
- **When** 新用户使用相同的用户名请求注册
- **Then** 系统返回 HTTP 409，错误码 `USERNAME_EXISTS`

#### Scenario: Register with invalid parameters

- **Given** 用户未登录
- **When** 用户请求注册，`username` 或 `password` 不符合规则
- **Then** 系统返回 HTTP 400，错误码 `VALIDATION_ERROR`

### Requirement: User Login

The system SHALL allow registered users to authenticate with username and password to obtain a JWT token.

#### Scenario: Login with valid credentials

- **Given** 用户已注册且状态正常
- **When** 用户请求 `POST /api/v1/auth/login`，提供正确的 `username` 和 `password`
- **Then** 系统返回 HTTP 200，包含 JWT token 和用户基础信息

#### Scenario: Login with invalid credentials

- **Given** 用户已注册或不存在
- **When** 用户请求登录，但用户名不存在、密码错误或用户已禁用
- **Then** 系统返回 HTTP 401，错误码 `INVALID_CREDENTIALS`

### Requirement: Current User Query

The system SHALL allow authenticated users to query their own profile information.

#### Scenario: Query current user with valid token

- **Given** 用户已登录
- **When** 用户请求 `GET /api/v1/auth/me`
- **Then** 系统返回 HTTP 200 和用户基础信息

#### Scenario: Query current user without valid token

- **Given** 用户未登录或 token 无效
- **When** 用户请求 `GET /api/v1/auth/me`
- **Then** 系统返回 HTTP 401

### Requirement: User Logout

The system SHALL provide a logout endpoint for client-side JWT cleanup. The server is stateless and does not maintain session or token blacklist.

#### Scenario: Logout with valid token

- **Given** 用户已登录
- **When** 用户请求 `POST /api/v1/auth/logout`
- **Then** 系统返回 HTTP 204，客户端负责删除本地 JWT

### Requirement: Domain Rules

- `User` 是领域聚合根，不依赖 Spring、HTTP、MyBatis-Plus 或数据库注解。
- 用户 ID 由领域层通过 `UserId.generate()` 生成，持久层不得重新生成 ID。
- 注册时必须校验用户名和密码强度。
- 从数据库还原用户时使用 `User.restore(...)`，不得重新执行业务注册逻辑、重新 hash 密码或更新时间戳。
- 密码 hash 能力通过 `PasswordHasher` 接口注入，领域层不依赖 BCrypt 实现。
- Token 签发能力通过 `TokenService` 接口注入，领域层不依赖 JWT 实现。

### Requirement: Persistence Rules

- 仓储接口定义在 `domain.user.repository.UserRepository`。
- MyBatis-Plus PO、Mapper、Assembler 和 Repository 实现放在 `infrastructure.persistence.user`。
- `users.username` 必须有唯一约束。
- 注册并发下，应用层先做 `existsByUsername` 快判，持久层仍必须捕获唯一约束冲突并转换为 `BusinessException(USERNAME_EXISTS)`。

### Requirement: Security Rules

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 其他接口默认需要认证。
- JWT 使用无状态鉴权，服务端不维护 session。
- 默认 JWT Secret 只允许本地体验；生产 profile 使用默认 secret 时必须阻止启动。

### Requirement: Non-goals

- 不做 RBAC。
- 不做组织架构或多租户。
- 不做服务端 token 黑名单。
- 不做第三方登录。
- 不做修改密码、找回密码、邮箱验证。
