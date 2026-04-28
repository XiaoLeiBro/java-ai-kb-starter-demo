# 0001 · 用户注册登录 + JWT 鉴权（并完成技术栈对齐）

- **状态**：Proposed
- **发起日期**：2026-04-28
- **限界上下文**：`domain.user`
- **免费版是否包含**：✅ 是（基础注册/登录能力是免费版入口，不做 RBAC/SSO）
- **商业版对应 SKU**：进阶版追加「多租户」；专业版追加「企业微信/钉钉 SSO + RBAC」

---

## 1. Why：为什么做这件事

**业务动机**

免费版 Demo 要让读者"5 分钟跑起来问答"，但系统必须能识别"是谁"才能：

- 区分不同用户的知识库（避免示例数据互相污染）
- 给后续 Token 计费、对话历史记录留接口位
- 作为安装方给客户演示时的最小权限隔离

没有 user 模块，后面所有业务都没法起来——它是所有聚合根的 `ownerId` 源头。

**技术动机（捆绑处理）**

骨架搭完时留了两个待决项，都在"第一个业务 proposal 时一起定"：

1. **持久层 JPA → MyBatis-Plus**：为了和作者另一个项目 `medrecord` 技术栈对齐，降低后续维护心智成本；同时 MyBatis-Plus 是中国 Java 项目事实标准，读者接受度更高。
2. **多模块 → 单模块扁平化**：当前只有 `backend/ai-kb-demo-server` 一个子模块，双层结构徒增认知负担；如未来真需要拆子模块（CLI、Agent Runtime），再拆代价很小。

**为什么三件事绑在一个 proposal 里而不是拆三个？**

- 持久层切换要落在**具体的 Repository 实现**上，user 模块恰好是第一个需要 Repository 的业务
- 单模块扁平化要移动 `src/` 目录，跟新增 user 模块的代码一起做比之后再改影响小
- 三件事有同一个验收标准："能从 `POST /api/v1/auth/register` 注册 → `POST /api/v1/auth/login` 拿到 JWT → 用 JWT 访问 `/api/v1/auth/me` 返回当前用户"

如果拆开，第二、第三个 proposal 只是机械的文件搬运，不产生业务价值，违背 OpenSpec "规格服务于业务"的原则。

---

## 2. What：做什么

### 2.1 业务能力（用户视角）

| 能力 | HTTP 接口 | 说明 |
|---|---|---|
| 注册 | `POST /api/v1/auth/register` | 用户名 + 密码（BCrypt）+ 邮箱（可选），无邮箱验证 |
| 登录 | `POST /api/v1/auth/login` | 返回 JWT（有效期 7 天）+ 用户基本信息 |
| 查询当前用户 | `GET /api/v1/auth/me` | JWT 鉴权，返回用户信息 |
| 登出 | `POST /api/v1/auth/logout` | 仅客户端清除 Token；服务端拉黑名单留给商业版 |

### 2.2 技术变更

**持久层切换**

- 删除 `spring-boot-starter-data-jpa` 依赖
- 新增 `mybatis-plus-spring-boot4-starter` 3.5.16
- Repository 实现模式：`domain.user.repository.UserRepository`（接口，纯领域语言）→ `infrastructure.persistence.user.impl.UserRepositoryImpl`（实现）委托给 `infrastructure.persistence.user.mapper.UserMapper`（MyBatis-Plus）
- JPA 实体注解（`@Entity` / `@Table` / `@Column`）全部改为 MyBatis-Plus（`@TableName` / `@TableField` / `@TableId`）
- 新增 Flyway 迁移 `V1__init_user.sql`

**项目结构扁平化**

```
变更前                                         变更后
java-ai-kb-starter-demo/                      java-ai-kb-starter-demo/
├── pom.xml (parent, packaging=pom)           ├── pom.xml (单模块, packaging=jar, 继承 spring-boot-starter-parent)
├── backend/                                  ├── src/
│   └── ai-kb-demo-server/                    │   ├── main/
│       ├── pom.xml                           │   │   ├── java/com/brolei/aikb/
│       └── src/...                           │   │   └── resources/
                                              │   └── test/
                                              └── docker-compose.yml
```

### 2.3 关键设计点（详见 design.md）

- 领域层 `User` 聚合根**不加任何 ORM 注解**（MyBatis-Plus 注解只放在 infrastructure 层的 PO 对象上）
- PO ↔ 领域对象通过 `UserPoAssembler` 显式手写映射，保持 DDD 层间干净
- 密码用 `BCryptPasswordEncoder`（Spring Security 内置），JWT 用 jjwt 0.12.6
- Spring Security 配置为**无状态 JWT 模式**（`SessionCreationPolicy.STATELESS`），服务端不保存登录态，每个请求都解析 JWT 重建 `SecurityContext`

---

## 3. Non-goals：不做什么（留给后续或商业版）

| 项 | 留给 | 理由 |
|---|---|---|
| 邮箱/短信验证码 | 未来 proposal | Demo 版不对外开放，降低复杂度 |
| 第三方登录（微信/GitHub） | 商业版专业版 | 涉及回调域名、appid 配置，企业部署时要 |
| RBAC（角色权限） | 商业版进阶版 | 目前只需"登录态"判定，不需要细粒度权限 |
| JWT 黑名单 / 登出失效 | 商业版进阶版 | Redis 存黑名单是轻活，但免费版用不到 |
| 密码重置 | 未来 proposal | 需要邮件通道，依赖先搞定 |
| 多租户 | 商业版进阶版 | 每条记录加 `tenant_id` 是全局改造，不在这个 proposal 里 |
| 审计日志（登录记录、IP） | 商业版专业版 | 合规要求 |
| SSO（企业微信/钉钉/LDAP） | 商业版专业版/企业版 | 企业私有化场景 |
| **登录失败限流 / 防暴力破解** | 部署环境 / 商业版 | 免费版本地 Demo 不做。若读者把 Demo 部署到公网，必须在 Nginx / API 网关 / WAF 层做限流，否则 `/api/v1/auth/login` 会被字典攻击。商业版在应用层会集成 Redis 计数限流 |
| **密码强度策略** | 未来 proposal | 本版仅校验"≥ 8 字符"，不做大小写/数字/符号组合强制 |

---

## 4. 风险与应对

| 风险 | 影响 | 应对 |
|---|---|---|
| Spring Boot 4.0.5 + MyBatis-Plus 3.5.16 兼容性 | 高（无法启动） | `mybatis-plus-spring-boot4-starter` 是官方 Spring Boot 4 专用包；若启动失败回退到 3.5.14 |
| 扁平化目录时 Git 历史断裂 | 中 | `git mv` 保留 rename 元数据；提交信息写明 |
| 现有骨架的 `KnowledgeBase` 聚合根已按 JPA 风格写（虽然没加注解），切 MyBatis-Plus 是否要改？ | 低 | 不改——领域层本来就不依赖 ORM，只改 infrastructure 实现；knowledge 模块的 PO 等第二个 proposal 再写 |
| Spring Security 默认会拦截所有请求，导致现有 `/api/v1/health` 403 | 高 | SecurityConfig 显式放行 `/api/v1/health`、`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、`/swagger-ui/**`、`/v3/api-docs/**`；`/me` 和 `/logout` 不走白名单，需 JWT 鉴权 |

---

## 5. 验收标准（Done 的定义）

1. `mvn clean verify` 通过（含 checkstyle，fmt 自动格式化）
2. 本地 Docker 起来 → `./mvnw spring-boot:run` 启动成功，Flyway 自动建 user 表
3. Postman 集合里 4 个接口（register / login / me / logout）全通
4. `/api/v1/health` 未登录仍可访问
5. `openspec/specs/user/` 下生成正式规格文件（归档阶段产生）
6. `README.md` 在"快速体验"段落增补 register/login 示例

---

## 6. 相关文件

- [design.md](design.md) — 技术决策详解
- [tasks.md](tasks.md) — 实施步骤清单
