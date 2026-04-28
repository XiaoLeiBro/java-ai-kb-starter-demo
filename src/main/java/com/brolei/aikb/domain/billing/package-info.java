/**
 * Token 与成本统计限界上下文（Billing Bounded Context）.
 *
 * <p>这个上下文**是 AI 应用进入企业场景的关键门槛**： 企业客户关心"每个用户、每个知识库、每次调用花了多少 Token / 多少钱"， 没有这个模块，AI 系统很难被企业采购接受。
 *
 * <p>核心概念：
 *
 * <ul>
 *   <li><b>TokenUsageRecord</b>（聚合根）：一次 LLM 调用的 Token 记录
 *   <li><b>CostEstimator</b>（领域服务）：按模型单价估算费用
 * </ul>
 *
 * <p>Demo 版本只记录：用户 ID、模型、输入/输出 Token、耗时、状态。
 *
 * <p>商业版扩展（不在 Demo 范围）：
 *
 * <ul>
 *   <li>按用户/知识库/模型多维统计报表
 *   <li>预算告警（超过阈值自动熔断）
 *   <li>成本分摊到部门 / 租户
 * </ul>
 */
package com.brolei.aikb.domain.billing;
