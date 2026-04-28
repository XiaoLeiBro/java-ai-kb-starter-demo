/**
 * 领域层（Domain Layer）——本系统的业务核心.
 *
 * <p>按限界上下文（Bounded Context）划分子包：
 *
 * <ul>
 *   <li>{@code user} 用户与权限
 *   <li>{@code knowledge} 知识库（聚合根：KnowledgeBase）
 *   <li>{@code chat} 对话（聚合根：Conversation）
 *   <li>{@code llm} 大模型供应商（抽象 Provider）
 *   <li>{@code billing} Token 与成本（聚合根：TokenUsageRecord）
 * </ul>
 *
 * <p>每个上下文内部约定目录：
 *
 * <pre>
 * domain/xxx/
 *   ├── model/        聚合根、实体、值对象、枚举
 *   ├── repository/   仓储接口（实现在 infrastructure）
 *   ├── service/      领域服务（跨实体的业务规则）
 *   └── event/        领域事件
 * </pre>
 *
 * <p>铁律：
 *
 * <ul>
 *   <li>领域层不依赖 Spring / MyBatis-Plus / HTTP 等基础设施
 *   <li>仓储接口在这里定义，实现类放到 {@code infrastructure.persistence}
 *   <li>领域对象不暴露 setter，状态变更必须通过业务方法
 * </ul>
 */
package com.brolei.aikb.domain;
