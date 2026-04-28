/**
 * 接入层（Interfaces Layer）.
 *
 * <p>职责：
 *
 * <ul>
 *   <li>暴露 REST API / WebSocket 等外部接口
 *   <li>参数校验、DTO 与领域对象的转换
 *   <li>全局异常处理、返回值规范化
 *   <li>认证与鉴权（JWT 解析等切面）
 * </ul>
 *
 * <p>原则：
 *
 * <ul>
 *   <li>不写业务逻辑，只做"翻译"和"编排入口"
 *   <li>依赖方向：interfaces → application → domain ← infrastructure
 * </ul>
 */
package com.brolei.aikb.interfaces;
