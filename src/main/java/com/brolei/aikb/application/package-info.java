/**
 * 应用层（Application Layer）.
 *
 * <p>职责：
 *
 * <ul>
 *   <li>用例编排（一次 REST 请求对应一个 Application Service 方法）
 *   <li>事务边界（{@code @Transactional} 在这一层声明）
 *   <li>跨聚合协调（如：创建知识库 + 记录操作日志）
 *   <li>领域事件发布
 * </ul>
 *
 * <p>命名约定：
 *
 * <ul>
 *   <li>Command / Query 分离（KnowledgeBaseCommandService / QueryService）
 *   <li>方法名体现用例意图（{@code createKnowledgeBase} 而非 {@code save}）
 * </ul>
 */
package com.brolei.aikb.application;
