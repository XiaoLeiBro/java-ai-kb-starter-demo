# Tasks · 0001 用户模块 + 技术栈对齐

> 任务顺序按「**先基础设施就绪 → 再领域核心 → 最后接入层**」安排。
> 每个任务 ≤ 4 小时；跨层改动拆成独立任务。

---

## 阶段 A · 准备（基础设施与项目结构）

### A1. 单模块扁平化
- **工作量**：≤ 2h
- **所属层**：构建
- **内容**：
  - `git mv backend/ai-kb-demo-server/src src`
  - 删除 `backend/` 目录
  - 合并 `backend/ai-kb-demo-server/pom.xml` 到根 `pom.xml`
  - 根 pom 改为：
    - `<packaging>jar</packaging>`
    - 继承 `spring-boot-starter-parent` 4.0.5
    - 删除 `<modules>`、删除 BOM import 里的 `spring-boot-dependencies`（parent 已导）
    - 保留 `langchain4j-bom`、`testcontainers-bom` 的 import
- **验收**：`mvn clean compile` 成功，目录结构如 design.md §2.5

### A2. 加 Maven Wrapper
- **工作量**：≤ 15min
- **所属层**：构建
- **内容**：`mvn -N wrapper:wrapper -Dmaven=3.9.9`；提交 `mvnw`、`mvnw.cmd`、`.mvn/`
- **验收**：`./mvnw --version` 返回 3.9.9

### A3. 持久层依赖切换：JPA → MyBatis-Plus
- **工作量**：≤ 1h
- **所属层**：构建
- **内容**：
  - 移除 `spring-boot-starter-data-jpa`
  - 新增 `com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16`
  - 新增 `mybatis-plus.version` 到 properties
- **验收**：`mvn dependency:tree | grep mybatis-plus` 有输出，`mvn compile` 通过

### A4. Flyway 初始迁移脚本
- **工作量**：≤ 30min
- **所属层**：基础设施（DB）
- **内容**：新增 `src/main/resources/db/migration/V1__init_user.sql`
  ```sql
  CREATE TABLE users (
    id            VARCHAR(36)  PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    email         VARCHAR(100),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
  );
  ```
- **验收**：`./mvnw spring-boot:run` 启动时 Flyway 自动执行，PostgreSQL 里可以查到 `users` 表

### A5. 更新 application.yml 新增 MyBatis-Plus / Flyway 配置段
- **工作量**：≤ 30min
- **所属层**：基础设施（配置）
- **内容**：
  ```yaml
  spring:
    flyway:
      enabled: true
      baseline-on-migrate: true
  mybatis-plus:
    configuration:
      map-underscore-to-camel-case: true
  ```
- **注意**：
  - **不要**配 `global-config.db-config.id-type` —— 所有聚合根 ID 由领域层生成，PO 统一用 `@TableId(type = IdType.INPUT)`
  - **不要**配 `logic-delete-field: deleted` —— 当前 `users` 表没有 `deleted` 字段，也没有删除用户能力；等未来有删除 proposal 时再引入
- **验收**：应用启动无报错；默认不在控制台打印 SQL 参数，避免泄露密码哈希等敏感字段

---

## 阶段 B · 领域层（纯业务）

### B1. User 聚合根 + UserId 值对象 + UserStatus 枚举 + PasswordHasher 接口
- **工作量**：≤ 2.5h
- **所属层**：domain
- **内容**：按 design.md §4 实现 `domain.user.model.*` 和 `domain.user.service.PasswordHasher`，不依赖 Spring/JPA/MyBatis-Plus
- **验收**：单元测试 `UserTest`
  - `register()` 成功返回实例，id/createdAt 自动生成
  - 用户名含空格时抛 `IllegalArgumentException`
  - 密码 < 8 字符时抛 `IllegalArgumentException`
  - `verifyPassword` 正确/错误路径都覆盖
  - `PasswordHasher` 接口编译通过，两方法签名 `hash(String)` / `matches(String, String)`

### B2. UserRepository 接口
- **工作量**：≤ 15min
- **所属层**：domain
- **内容**：`domain.user.repository.UserRepository`
  ```java
  public interface UserRepository {
      Optional<User> findById(UserId id);
      Optional<User> findByUsername(String username);
      boolean existsByUsername(String username);
      User save(User user);
  }
  ```
- **验收**：接口编译通过；方法签名全部用领域对象，无 JPA/MyBatis-Plus 类型泄漏

### B3. User.restore(...) 聚合重建工厂
- **工作量**：≤ 30min
- **所属层**：domain
- **内容**：
  为 `User` 聚合根增加 `restore()` 静态工厂，供 Repository / Assembler 从 DB 还原聚合时使用。
  详见 `design.md §1.6`。
  ```java
  public static User restore(
      UserId id, String username, String passwordHash, String email,
      UserStatus status, Instant createdAt, Instant updatedAt
  ) { ... }
  ```
- **注意**：
  - **不**触发 `register()` 的任何业务校验
  - **不**重新生成 ID
  - **不**重新 hash 密码
  - **不**修改时间戳
  - Javadoc 明确标注"仅供 Repository / Assembler 使用，不要在业务代码里调用"
- **验收**：单元测试 `UserRestoreTest`
  - 传入已有字段，返回实例的每个字段都与传入值严格相等
  - 不触发 `validateUsername` / `validatePasswordStrength`（可通过传入业务规则下不合法但历史存在的值验证，例如 2 字符用户名）

---

## 阶段 C · 基础设施层（适配）

### C1. UserPo（MyBatis-Plus PO）
- **工作量**：≤ 30min
- **所属层**：infrastructure.persistence.user.po
- **内容**：`@TableName("users")` + `@TableId(type = IdType.INPUT)` + Lombok `@Getter` / `@Setter` + `@ToString(exclude = "passwordHash")`
- **关键**：ID 类型用 `IdType.INPUT`，由领域层传入，MyBatis-Plus 不参与生成（见 design.md §1.2）
- **验收**：编译通过

### C2. UserMapper（BaseMapper）
- **工作量**：≤ 15min
- **所属层**：infrastructure.persistence.user.mapper
- **内容**：`extends BaseMapper<UserPo>` + `@Mapper` 注解；在启动类加 `@MapperScan("com.brolei.aikb.infrastructure.persistence")`（基础包，Spring 递归扫描，详见 design.md §4.3）
- **验收**：启动时 Mapper 被注册

### C3. UserPoAssembler（显式手写映射）
- **工作量**：≤ 45min
- **所属层**：infrastructure.persistence.user.assembler
- **内容**：普通 `@Component`；`toPo()` / `toDomain()` 都显式手写，`toDomain()` 调 `User.restore(...)`（详见 design.md §1.6）
- **验收**：单测覆盖 `UserPo → User`（验证所有字段还原）、`User → UserPo`

### C3.1. UserId ↔ String 显式映射
- **工作量**：≤ 15min
- **所属层**：infrastructure.persistence.user.assembler
- **内容**：在 `UserPoAssembler` 中显式转换：
  ```java
  po.setId(user.id().value());
  UserId.of(po.getId());
  ```
- **原因**：`UserId` 是值对象，不应把基础类型转换逻辑散落到 Repository 实现中
- **验收**：双向映射单测都通过

### C4. UserRepositoryImpl
- **工作量**：≤ 1.5h
- **所属层**：infrastructure.persistence.user.impl
- **内容**：
  - `@Repository` 实现 `UserRepository`，委托 `UserMapper` + `UserPoAssembler`
  - `save()` 方法捕获 `org.springframework.dao.DuplicateKeyException`（MyBatis-Plus 抛），转成领域层可识别的异常（见 D1.1）
- **验收**：
  - Testcontainers 起 PostgreSQL 容器
  - 集成测试 `UserRepositoryImplIT` 覆盖：
    - save / findById / findByUsername / existsByUsername
    - 同一 username 二次 save 时抛 `BusinessException(USERNAME_EXISTS)`（不是原始数据库异常）
  - 所有测试通过

### C5. BCryptPasswordHasher（实现 PasswordHasher）
- **工作量**：≤ 30min
- **所属层**：infrastructure.security
- **内容**：`@Component` 包装 `BCryptPasswordEncoder`
- **验收**：单测（非集成）覆盖 hash + matches

### C6. JwtService（生成/解析）
- **工作量**：≤ 1.5h
- **所属层**：infrastructure.security
- **内容**：
  - 用 jjwt 0.12.6 API（HS256，secret 从 `AiKbProperties` 读）
  - `String issue(UserId userId, String username)` 返回 7 天有效期 JWT
  - `Optional<JwtClaims> parse(String token)` 解析失败返回空
- **验收**：单测覆盖
  - 正常签发 + 解析
  - 过期 Token 返回空
  - 篡改签名返回空

### C7. JwtAuthenticationFilter
- **工作量**：≤ 1h
- **所属层**：infrastructure.security
- **内容**：继承 `OncePerRequestFilter`，从 `Authorization: Bearer xxx` 解析，成功则构造 `UsernamePasswordAuthenticationToken` 塞进 `SecurityContext`
- **验收**：单元测试（**不依赖 AuthController，/me 还不存在**）
  - 构造 `MockHttpServletRequest` + `MockHttpServletResponse` + `MockFilterChain`
  - 带合法 JWT：filter 执行后 `SecurityContextHolder.getContext().getAuthentication()` 非空，principal 是 `UserId`
  - 带过期 JWT：`SecurityContext` 保持为空，`filterChain.doFilter` 正常继续（由 `authorizeHttpRequests` 拦截）
  - 不带 Authorization header：`SecurityContext` 保持为空，正常继续

### C8. SecurityConfig
- **工作量**：≤ 45min
- **所属层**：infrastructure.security
- **内容**：按 design.md §3.1 配置白名单 + 无状态 + 过滤器链
- **验收**：启动阶段可验证的部分（**/me 接口未完成，延后到 D4**）
  - `SecurityFilterChain` Bean 正常注册
  - `/api/v1/health` 不带 JWT 返回 200
  - `POST /api/v1/auth/register`、`POST /api/v1/auth/login` 在白名单中（不带 JWT 可访问）
  - `GET /api/v1/auth/me`、`POST /api/v1/auth/logout` **不在**白名单中（不带 JWT 返回 401）
  - `/swagger-ui/index.html` 不带 JWT 可访问
  - 任一未列入白名单的路径（如 `/api/v1/internal/ping`，临时加一个测试端点）不带 JWT 返回 401

### C9. JWT Secret 分 profile 保护
- **工作量**：≤ 1h
- **所属层**：infrastructure.security
- **内容**：实现 `JwtSecretGuard`（`ApplicationRunner` 或 `@PostConstruct` 组件），按 design.md §3.4 分级：
  - `dev` profile 静默
  - 默认 profile 打印大字号 WARN（星号包裹）
  - `prod` / `production` profile 使用默认 secret 时抛 `IllegalStateException` 阻止启动
- **验收**：
  - 单元测试：mock `Environment` 为 dev profile、默认、prod profile 三种场景，分别断言行为
  - 集成测试（可选）：`@ActiveProfiles("prod")` 启动 `@SpringBootTest` 时预期启动失败，失败信息包含"ai-kb.security.jwt.secret"
  - `application-dev.yml.example` 里明确标注"生产必须改"

---

## 阶段 D · 应用层 + 接入层

### D1. UserApplicationService
- **工作量**：≤ 1.5h
- **所属层**：application.user
- **内容**：三个用例方法
  ```java
  User register(RegisterCommand cmd);         // existsByUsername 快判 + User.register() + save
  LoginResult login(LoginCommand cmd);        // findByUsername + verifyPassword + JwtService.issue
  User currentUser(UserId userId);            // findById
  ```
  加 `@Transactional(readOnly = ...)` 注解
- **验收**：
  - 使用轻量 fake 单测覆盖三个方法
  - 重复用户名注册（快判命中）抛 `BusinessException(USERNAME_EXISTS)`
  - 错误密码登录抛 `BusinessException(INVALID_CREDENTIALS)`

### D1.1 注册并发唯一性处理
- **工作量**：≤ 45min
- **所属层**：application.user + infrastructure.persistence
- **内容**：
  - `UserApplicationService.register()` 先做 `existsByUsername` 快速判断（正常路径 + 性能）
  - **底层兜底**：`UserRepositoryImpl.save()` 捕获 `DuplicateKeyException`（MyBatis-Plus 把 PostgreSQL 唯一索引冲突包装成这个），转成 `BusinessException(USERNAME_EXISTS)`（在 C4 里已落地，这里把 common/exception 里的错误码枚举补齐并写单测覆盖竞态路径）
  - 业务错误码 `USERNAME_EXISTS` 定义在 `common.exception.ErrorCode` 枚举中
- **场景**：
  ```
  请求 A：existsByUsername → false
  请求 B：existsByUsername → false
  请求 A：insert → 成功
  请求 B：insert → DuplicateKeyException → 转 BusinessException(USERNAME_EXISTS)
  ```
- **验收**：
  - 集成测试模拟并发：两个线程同时调 `register("bob", ...)`，一个成功一个返回 `USERNAME_EXISTS`，不泄漏数据库异常栈
  - Controller 层返回 HTTP 409（由 D2 的 `@ControllerAdvice` 统一映射）

### D2. DTO + 全局异常处理
- **工作量**：≤ 45min
- **所属层**：interfaces.dto + interfaces.exception
- **内容**：
  - `interfaces.dto.auth.*` 四个 Request / Response（`RegisterRequest` / `LoginRequest` / `LoginResponse` / `UserResponse`）
  - `@ControllerAdvice` 处理：
    - `BusinessException` → `ApiResult.error(code, message)`，HTTP 码按 `ErrorCode` 映射（`USERNAME_EXISTS` → 409，`INVALID_CREDENTIALS` → 401）
    - `MethodArgumentNotValidException` → 400 + 字段错误详情
- **验收**：
  - 注册时不传用户名返回 400 + 提示"username 不能为空"
  - 重复用户名返回 HTTP 409 + errorCode=`USERNAME_EXISTS`
  - Controller 层独立单测（不起完整 Spring 上下文）

### D3. AuthController
- **工作量**：≤ 1h
- **所属层**：interfaces.rest
- **内容**：
  - `POST /api/v1/auth/register` 接收 `RegisterRequest`，返回 `UserResponse`
  - `POST /api/v1/auth/login` 接收 `LoginRequest`，返回 `LoginResponse { token, user }`
  - `GET /api/v1/auth/me` 从 `SecurityContext` 取 userId，返回 `UserResponse`
  - `POST /api/v1/auth/logout` 204 No Content（服务端空实现）
- **依赖**：D2 完成（DTO + 异常处理已就位）
- **验收**：SpringDoc Swagger UI 里 4 个接口都有完整请求/响应示例

### D4. 端到端集成测试（MockMvc + Testcontainers）
- **工作量**：≤ 1.5h
- **所属层**：test
- **内容**：`AuthControllerIT`
  - `register → 200` + 返回 userId
  - `login → 200` + JWT 非空
  - 用返回的 JWT 访问 `/me → 200`
  - 未带 JWT 访问 `/me → 401`
  - `POST /api/v1/auth/logout` 带 JWT → 204
  - `POST /api/v1/auth/logout` 不带 JWT → 401
- **验收**：CI 能跑过

---

## 阶段 E · 收尾

### E1. README 增补"体验登录"段落
- **工作量**：≤ 20min
- **内容**：加 curl 示例（register → login → 用 JWT 调 /me）
- **验收**：README 渲染正常，复制粘贴能跑

### E2. 更新 docs/architecture.md
- **工作量**：≤ 30min
- **内容**：
  - 把"持久层：JPA"改为"持久层：MyBatis-Plus"
  - 增加"为什么 PO ↔ 领域对象分离"的简述（引用本 proposal 的 design.md）
  - 目录结构示例更新为扁平化后的版本
- **验收**：文档与代码一致

### E3. 归档：移到 openspec/specs/user/
- **工作量**：≤ 15min
- **内容**：
  - 把 proposal / design / tasks 中"已实施的能力规格"沉淀到 `openspec/specs/user/auth.md`
  - 原 `changes/0001-*` 目录 `git mv` 到 `openspec/changes/archive/0001-*`
- **验收**：`openspec/specs/user/auth.md` 包含当前登录系统的稳定规格

---

## 任务依赖图（放松版）

```
A1 ─┬─ A2
    └─ A3 ─┬─ A4 ─ A5 ─────────────────┐   （数据库配置）
           │                           │
           ├─ C1 ─ C2 ─ C3 ─ C3.1 ─────┤   （PO / Mapper / Assembler，只依赖 A3 依赖切换）
           │                           │
           └─ B1 ─ B2 ─ B3 ─────────────┤   （领域层，只依赖 A3 编译环境）
                                       ↓
                                      C4   （Repository 实现，需 A5 + C3.1 + B3 就位）
                                       │
                        C5 ──────────┐ │   （密码 hasher，独立）
                        C6 ──────────┤ │   （JwtService，独立）
                        C7 ──────────┤ │   （JwtAuthenticationFilter，依赖 C6）
                        C8 ──────────┤ │   （SecurityConfig，依赖 C7）
                        C9 ──────────┤ │   （JWT Secret 保护，独立于 C5-C8，依赖 AiKbProperties）
                                     ↓ ↓
                                 D1 ─ D1.1 ─ D2 ─ D3 ─ D4 ─ E1 ─ E2 ─ E3
```

**并行机会（单人开发也能有序推进）**：

- **A3 完成后**，B 阶段（领域层）、C1-C3.1（PO 适配器）可以并行推进，因为领域层和 PO 都不依赖数据库就绪
- **A5 + C3.1 + B3 都就位后**才能做 C4（Repository 实现 + 集成测试）
- **C5 / C6 / C9** 三个安全基础组件互相独立，可以任意顺序
- **C7 依赖 C6**（需要 JwtService）；**C8 依赖 C7**（需要把 Filter 串进链里）
- **D1 依赖**：B1-B3 领域完整、C4 Repository 可用、C5 密码 hasher 可用、C6 JwtService 可用
- **D2 先于 D3**：Controller 要用 DTO 和全局异常处理

---

## 进度追踪

进入 `/opsx:apply` 阶段后，每完成一个任务在这里打 ✅；完成全部后发起 `/opsx:archive`。
