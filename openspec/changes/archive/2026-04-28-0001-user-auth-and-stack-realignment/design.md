# Design · 0001 用户模块 + 技术栈对齐

## 1. 持久层：MyBatis-Plus 下如何保持 DDD 干净

### 1.1 现状问题

DDD 的核心约束之一是"**领域层不依赖任何 ORM / 框架**"。JPA 世界里常见的错误做法：

```java
// ❌ 反例：聚合根直接被 @Entity 注解侵入
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    // public setter 也被 JPA 要求加上了
    public void setUsername(String u) { this.username = u; }
}
```

MyBatis-Plus 诱惑略小（注解更轻），但同样会诱使开发者把 `@TableName`、`@TableField` 加到领域对象上，走老路。

### 1.2 本项目采取的做法

**三个对象，严格分层**：

```
┌─────────────────────────────────────┐
│ domain.user.model.User              │  纯业务对象
│  - 私有字段 + 业务方法                 │  无任何框架注解
│  - 静态工厂 User.register(...)       │  不知道数据库的存在
└─────────────────────────────────────┘
              ↕ UserPoAssembler 显式映射
┌─────────────────────────────────────┐
│ infrastructure.persistence.user     │
│   .po.UserPo                        │  MyBatis-Plus PO
│  - @TableName("users")              │  纯贫血模型
│  - @TableId / @TableField           │  只管 ↔ DB 列
│  - lombok @Getter / @Setter        │
│  - @ToString 排除 passwordHash     │
└─────────────────────────────────────┘
              ↕ MyBatis-Plus BaseMapper
┌─────────────────────────────────────┐
│ infrastructure.persistence.user     │
│   .mapper.UserMapper                │  数据访问接口
│  extends BaseMapper<UserPo>         │  MyBatis-Plus 自动实现
└─────────────────────────────────────┘
```

**仓储实现模式**：

```
domain.user.repository.UserRepository   （接口，方法签名全部是领域语言）
          ▲ implements
infrastructure.persistence.user.impl.UserRepositoryImpl
   │
   ├── 注入 UserMapper
   ├── 注入 UserPoAssembler（普通 Spring Component）
   └── 方法体：PO ↔ 领域对象 转换，没有业务逻辑
```

**ID 生成的单一职责**

用户 ID 的生成**由领域层负责**（`User.register()` 内部用 `UserId.generate()` 分配 UUID），
MyBatis-Plus 和数据库**都不参与**。这样做有三个好处：

1. 聚合根的 ID 是它生命周期的一部分，不能在"保存"这一步被 ORM 偷偷生成
2. 领域测试不用启数据库就能拿到可用的聚合实例（带 ID）
3. 未来换存储（Mongo / NoSQL / 分布式 ID 服务）不影响领域代码

对应的 PO 配置**必须**用 `@TableId(type = IdType.INPUT)` —— 让 MyBatis-Plus 原样使用传入的 ID，
**不要**用 `ASSIGN_UUID` / `AUTO` / `ASSIGN_ID`。

同理，`application.yml` 里的 `mybatis-plus.global-config.db-config.id-type` **不设**，
避免全局默认覆盖到 user 表之外的聚合（未来 knowledge、chat 也会各自在领域层生 ID）。

### 1.3 为什么不让领域对象直接当 PO（少写一个类）

短期看"更简洁"，长期看有四个坑：

1. **数据库字段变化会冲击业务代码**：比如给 `users` 加 `tenant_id`，如果领域对象 = PO，`User` 聚合根会被迫增加 `tenantId` 字段，即使业务层暂时用不上
2. **聚合根有不变量（invariants）**：`User.register()` 必须校验用户名格式、密码强度；PO 是"贫血的数据载体"，两种职责混在一起很难单元测试
3. **MyBatis-Plus 注解会钉死字段可见性**：PO 需要 getter / setter，聚合根要求私有状态变更入口——这两者本质冲突
4. **商业版做多存储扩展时代价指数级上升**：如果商业版要支持 MySQL / 国产库 TiDB，JPA/MyBatis-Plus 的 PO 可能要重写；领域对象保持干净，换存储就只是换 PO 和 Mapper

**这是 DDD 的"重复代价"，值得付**。

### 1.4 PO ↔ 领域对象映射：为什么用显式手写映射

- 手写 `new User(po.getId(), po.getUsername(), ...)` 可读但随字段增加会臃肿
- BeanUtils/ModelMapper 是反射，性能差且字段不匹配时静默失败
- 本项目当前字段数量很少，显式手写 `toDomain()` / `toPo()` 更直接，也避免代码生成工具产生额外 Bean 与手写逻辑混用造成认知成本

### 1.5 PO ↔ 领域对象映射：UserPoAssembler 完整示例

`UserId` 是领域值对象（包装 `String`），PO 中保存的是基础类型 `String`。`toDomain()` 必须调 `User.restore()`（原因见 §1.6），不能绕过聚合重建工厂：

```java
@Component
public class UserPoAssembler {

    public User toDomain(UserPo po) {
        if (po == null) return null;
        return User.restore(
            UserId.of(po.getId()),
            po.getUsername(),
            po.getPasswordHash(),
            po.getEmail(),
            UserStatus.valueOf(po.getStatus()),
            po.getCreatedAt(),
            po.getUpdatedAt()
        );
    }

    public UserPo toPo(User user) {
        if (user == null) return null;
        UserPo po = new UserPo();
        po.setId(user.id().value());
        po.setUsername(user.username());
        po.setPasswordHash(user.passwordHash());
        po.setEmail(user.email());
        po.setStatus(user.status().name());
        po.setCreatedAt(user.createdAt());
        po.setUpdatedAt(user.updatedAt());
        return po;
    }
}
```

**原则**：
- 所有**值对象 ↔ 基础类型**的映射都显式写出，不依赖反射或隐式推断
- `toDomain()` **始终**调 `restore()`，避免绕过聚合重建工厂

### 1.6 聚合重建：`register()` vs `restore()`

聚合根有两种诞生方式，对应两个静态工厂：

| 场景 | 工厂方法 | 行为 |
|---|---|---|
| 新用户注册 | `User.register(username, rawPassword, email, hasher)` | 校验业务规则 → 生成 UserId → hash 密码 → 设置 `createdAt=now` |
| 从 DB 还原 | `User.restore(id, username, passwordHash, email, status, createdAt, updatedAt)` | 直接塞字段，**不**做业务校验，**不**生成 ID，**不**改时间戳 |

```java
public static User register(String username, String rawPassword, String email, PasswordHasher hasher) {
    validateUsername(username);
    validatePasswordStrength(rawPassword);
    Instant now = Instant.now();
    return new User(
        UserId.generate(),
        username,
        hasher.hash(rawPassword),
        email,
        UserStatus.ACTIVE,
        now,
        now
    );
}

/**
 * 从持久化数据重建聚合根。仅供 Repository / Assembler 使用，
 * 不触发业务校验，不修改任何字段。
 */
public static User restore(
    UserId id,
    String username,
    String passwordHash,
    String email,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    return new User(id, username, passwordHash, email, status, createdAt, updatedAt);
}
```

**为什么必须分开**：如果 Repository 从 PO 还原时调用 `register()`：

1. `UserId.generate()` 会覆盖原有 ID → 数据丢失
2. `hasher.hash(passwordHash)` 会把已经 hash 过的值再 hash 一次 → 密码永远验不过
3. `createdAt=now` 会覆盖历史时间戳 → 审计数据错乱
4. 业务校验会对历史数据重新执行（比如后续加了"用户名长度 ≥ 4"规则，老用户名 3 个字符会被拒绝还原）

`restore()` 名字上就暗示"不要用来处理业务请求"，配合 Javadoc 说明，降低误用概率。

---

## 2. 单模块扁平化：为什么现在做而不是以后

### 2.1 现状

```
java-ai-kb-starter-demo/                <- parent pom, packaging=pom
├── pom.xml
└── backend/
    └── ai-kb-demo-server/              <- child module, packaging=jar
        ├── pom.xml
        └── src/...
```

### 2.2 目标

```
java-ai-kb-starter-demo/                <- 单模块, packaging=jar, parent=spring-boot-starter-parent
├── pom.xml                             <- 合并后的 pom
└── src/...                             <- 从 backend/ai-kb-demo-server/src 搬上来
```

### 2.3 执行策略

**用 `git mv` 而不是 `mv` + 重建**——保留文件历史、便于 `git blame` 追溯。

```bash
git mv backend/ai-kb-demo-server/src src
git mv backend/ai-kb-demo-server/pom.xml pom.new.xml   # 暂存 child pom
# 手工合并 pom.new.xml 到根 pom.xml
rm pom.new.xml
rmdir backend/ai-kb-demo-server backend                 # 只删空目录
```

### 2.4 合并后的根 pom 关键变化

- `<packaging>pom</packaging>` → `<packaging>jar</packaging>`
- 删除 `<modules>` 段
- 加 `<parent>spring-boot-starter-parent</parent>` 继承（和 medrecord 对齐），同时删除 dependencyManagement 里的 `spring-boot-dependencies` import（parent 已经导入）
- 保留 `langchain4j-bom` 和 `testcontainers-bom` 的 import（这两个不是 Spring Boot 管的）
- 把 backend pom 的 `<dependencies>` 段整体搬上来
- pluginManagement 合并到 plugins 直接执行（单模块不需要管理层了）

### 2.5 目录结构（扁平后）

```
java-ai-kb-starter-demo/
├── pom.xml
├── docker-compose.yml
├── README.md
├── LICENSE
├── checkstyle.xml                      <- 可选：本地定制规则（本 proposal 暂用 google_checks.xml）
├── src/
│   ├── main/
│   │   ├── java/com/brolei/aikb/
│   │   │   ├── AiKbApplication.java
│   │   │   ├── interfaces/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   ├── infrastructure/
│   │   │   └── common/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml.example
│   │       └── db/migration/
│   │           └── V1__init_user.sql   <- 本 proposal 新增
│   └── test/
│       └── java/com/brolei/aikb/
├── docs/
├── openspec/
└── .mvn/ & mvnw & mvnw.cmd             <- 本 proposal 顺手把 Maven Wrapper 加上
```

---

## 3. Spring Security 配置要点

### 3.1 无状态 JWT 模式

```
filter chain:
  JwtAuthenticationFilter  ← 自己写：解析 Authorization header → 放入 SecurityContext
    ↓
  UsernamePasswordAuthenticationFilter  ← 不用，禁用默认表单登录
    ↓
  ...
```

`SecurityFilterChain` 配置：

- `sessionCreationPolicy(STATELESS)`
- `csrf.disable()`（REST API，前端用 Token 不吃 Cookie）
- `cors`：Demo 版允许所有来源（商业版收紧）
- 白名单放行：`POST /api/v1/auth/register`、`POST /api/v1/auth/login`、`/api/v1/health`、`/swagger-ui/**`、`/v3/api-docs/**`（`/me` 和 `/logout` **不**在白名单中，需 JWT 鉴权）
- 其他 `anyRequest().authenticated()`

### 3.2 JWT 结构

```json
{
  "sub": "user-id-uuid",       // User.id
  "iat": 1714320000,
  "exp": 1714924800,           // 7 天
  "username": "alice"          // 方便前端直接展示，不涉密
}
```

密钥：从 `application.yml` 读取 `ai-kb.security.jwt.secret`（Demo 用固定值并在 example 里告警"生产必须改"）。

### 3.3 密码存储

- BCrypt，cost=10（默认）
- 不自定义 PasswordEncoder Bean，直接用 `BCryptPasswordEncoder`（Spring Security 内置）

### 3.4 JWT Secret 分 profile 分级保护

Secret 不能"写在 yml 里就算了"——必须在启动阶段按 profile 硬保护：

| Profile | 遇到默认 secret 时的行为 | 用途 |
|---|---|---|
| `dev` | 静默放行，使用默认值 | 本地开发，不扰乱 log |
| 默认（无 profile 激活） | **启动时打印大字号 WARN**，使用默认值 | 读者 `git clone` 后第一次 `./mvnw spring-boot:run` 能跑起来，但每次启动都能看到明显警告 |
| `prod` / `production` | **启动失败**，明确提示"请配置 `ai-kb.security.jwt.secret` 环境变量" | 真正上生产的人必然会显式激活 profile，硬保护在这里不劝退任何人 |

实现思路（伪代码）：

```java
@Component
public class JwtSecretGuard implements ApplicationRunner {
    private static final String DEFAULT_SECRET = "change-me-in-production-default-demo-secret-do-not-use";

    private final Environment env;
    private final AiKbProperties props;

    @Override
    public void run(ApplicationArguments args) {
        boolean usingDefault = DEFAULT_SECRET.equals(props.getSecurity().getJwt().getSecret());
        if (!usingDefault) return;

        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.contains("prod") || profiles.contains("production")) {
            throw new IllegalStateException(
                "检测到生产环境使用默认 JWT Secret，请配置 ai-kb.security.jwt.secret 环境变量后重试"
            );
        }
        if (profiles.contains("dev")) return;  // dev 静默

        // 默认 profile：大字号 WARN
        log.warn("\n" +
            "****************************************************************************\n" +
            "* 当前正在使用默认 JWT Secret，仅适用于本地体验。                               *\n" +
            "* 部署到公网前必须设置环境变量 AI_KB_SECURITY_JWT_SECRET（长度 ≥ 32 字符）。   *\n" +
            "****************************************************************************");
    }
}
```

这个组件对应 `tasks.md` 里的 **C9**。

---

## 4. User 聚合根设计

### 4.1 字段

| 字段 | 类型 | 说明 |
|---|---|---|
| id | `UserId` 值对象（包装 String/UUID） | 领域层显式 ID 类型 |
| username | String | 登录名，唯一 |
| passwordHash | String | BCrypt 值 |
| email | String nullable | 可选 |
| status | `UserStatus` 枚举 | ACTIVE / DISABLED |
| createdAt / updatedAt | Instant | 时间戳 |

### 4.2 行为

```java
public class User {
    public static User register(String username, String rawPassword, String email, PasswordHasher hasher) {
        // 校验 username 格式（3-20 字符，字母数字下划线）
        // 校验 password 强度（≥ 8 字符）
        // 返回新实例
    }

    public boolean verifyPassword(String rawPassword, PasswordHasher hasher) { ... }

    public void changePassword(String oldRaw, String newRaw, PasswordHasher hasher) {
        // 先校验旧密码，再改——业务规则放在聚合根
    }

    public void disable() { this.status = DISABLED; }
    // 没有 setXxx 方法
}
```

### 4.3 Mapper 扫描配置

启动类加注解：

```java
@MapperScan("com.brolei.aikb.infrastructure.persistence")
```

**不要**写成 `com.brolei.aikb.infrastructure.persistence.**.mapper` 这种 Ant 风格路径——
Spring 官方 `@MapperScan` 本身就是递归扫描，指定一个基础包即可，
**后续新增的 knowledge / chat / billing 的 Mapper 不用动这行配置**。

### 4.4 为什么要 `PasswordHasher` 接口注入到 `register` 方法里

领域层不能依赖 Spring Security 的 `BCryptPasswordEncoder`，但 hashing 是业务规则的一部分（"密码必须加密存储"这个约束属于业务）。

做法：

- 在 `domain.user.service.PasswordHasher` 定义接口（两个方法 `hash(String)` / `matches(String, String)`）
- `infrastructure.security.BCryptPasswordHasher` 实现，内部委托给 `BCryptPasswordEncoder`
- 领域对象通过参数接收 `PasswordHasher`，不做字段持有（保持无状态）

---

## 5. 包结构变更（本 proposal 后的 domain.user）

```
com.brolei.aikb.domain.user/
├── model/
│   ├── User.java                 聚合根
│   ├── UserId.java               值对象
│   └── UserStatus.java           枚举
├── repository/
│   └── UserRepository.java       接口
└── service/
    └── PasswordHasher.java       领域服务接口

com.brolei.aikb.infrastructure.persistence.user/
├── po/
│   └── UserPo.java               MyBatis-Plus PO
├── mapper/
│   └── UserMapper.java           BaseMapper<UserPo>
├── assembler/
│   └── UserPoAssembler.java      显式手写映射
└── impl/
    └── UserRepositoryImpl.java   UserRepository 实现

com.brolei.aikb.infrastructure.security/
├── BCryptPasswordHasher.java     PasswordHasher 实现
├── JwtService.java               生成/解析 JWT
├── JwtAuthenticationFilter.java  每请求过滤器
└── SecurityConfig.java           Spring Security 配置

com.brolei.aikb.application.user/
└── UserApplicationService.java   用例编排：register / login / currentUser

com.brolei.aikb.interfaces.rest/
└── AuthController.java           POST /register, /login, GET /me, POST /logout
```

---

## 6. 回滚策略

如果 MyBatis-Plus 3.5.16 和 Spring Boot 4.0.5 不兼容，三种回退：

1. **第一选**：降级到 MyBatis-Plus 3.5.14（已知兼容 Spring Boot 3.x，4.x 测试中）
2. **第二选**：暂时保留 JPA，只做 user 模块的业务逻辑；持久层切换独立成 0002 proposal
3. **第三选**：Spring Boot 降级到 3.3.x（放弃对齐 medrecord）

优先级 1 > 2 > 3。回退决策在"实施阶段第 3 个 task 失败时"触发。
