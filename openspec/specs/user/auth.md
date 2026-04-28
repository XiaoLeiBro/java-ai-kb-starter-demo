# 用户认证规范

## 范围

当前稳定规格覆盖 `v0.1` 用户注册、登录、JWT 鉴权、当前用户查询和登出接口。

### 注册

- Method: `POST`
- Path: `/api/v1/auth/register`
- Auth: 不需要 JWT
- Request:
  - `username`: 必填，3-20 位，字母、数字、下划线
  - `password`: 必填，至少 8 位
  - `email`: 可选，符合 email 格式
- Success: HTTP 200，返回用户基础信息
- Failure:
  - 用户名重复：HTTP 409，`USERNAME_EXISTS`
  - 参数校验失败：HTTP 400，`VALIDATION_ERROR`

### 登陆

- Method: `POST`
- Path: `/api/v1/auth/login`
- Auth: 不需要 JWT
- Request:
  - `username`: 必填
  - `password`: 必填
- Success: HTTP 200，返回 JWT 和用户基础信息
- Failure:
  - 用户不存在、密码错误或用户禁用：HTTP 401，`INVALID_CREDENTIALS`

### 当前用户

- Method: `GET`
- Path: `/api/v1/auth/me`
- Auth: 需要 `Authorization: Bearer <token>`
- Success: HTTP 200，返回当前用户基础信息
- Failure:
  - 未登录或 token 无效：HTTP 401

### 注销

- Method: `POST`
- Path: `/api/v1/auth/logout`
- Auth: 需要 `Authorization: Bearer <token>`
- Behavior: 服务端无状态登出，客户端自行删除 JWT
- Success: HTTP 204

## 领域规则

- `User` 是领域聚合根，不依赖 Spring、HTTP、MyBatis-Plus 或数据库注解。
- 用户 ID 由领域层通过 `UserId.generate()` 生成，持久层不得重新生成 ID。
- 注册时必须校验用户名和密码强度。
- 从数据库还原用户时使用 `User.restore(...)`，不得重新执行业务注册逻辑、重新 hash 密码或更新时间戳。
- 密码 hash 能力通过 `PasswordHasher` 接口注入，领域层不依赖 BCrypt 实现。
- Token 签发能力通过 `TokenService` 接口注入，领域层不依赖 JWT 实现。

## 持久层规则

- 仓储接口定义在 `domain.user.repository.UserRepository`。
- MyBatis-Plus PO、Mapper、Assembler 和 Repository 实现放在 `infrastructure.persistence.user`。
- `users.username` 必须有唯一约束。
- 注册并发下，应用层先做 `existsByUsername` 快判，持久层仍必须捕获唯一约束冲突并转换为 `BusinessException(USERNAME_EXISTS)`。

## 安全规则

- `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、Swagger 文档路径允许匿名访问。
- 其他接口默认需要认证。
- JWT 使用无状态鉴权，服务端不维护 session。
- 默认 JWT Secret 只允许本地体验；生产 profile 使用默认 secret 时必须阻止启动。

## 非目标

- 不做 RBAC。
- 不做组织架构或多租户。
- 不做服务端 token 黑名单。
- 不做第三方登录。
- 不做修改密码、找回密码、邮箱验证。
