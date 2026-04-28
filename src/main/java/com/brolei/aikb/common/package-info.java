/**
 * 通用工具层（Common）.
 *
 * <p>存放跨层共用的小工具，但**不包含业务规则**：
 *
 * <ul>
 *   <li>{@code result} 统一返回结构（ApiResult、PageResult）
 *   <li>{@code exception} 基础异常类（BusinessException、NotFoundException）
 *   <li>{@code util} 纯工具类（IdUtils、DateUtils）
 * </ul>
 *
 * <p>警告：不要把业务逻辑放进 common。如果一个工具方法里出现 "KnowledgeBase"、"User"、"Chat" 等领域词，它应该属于对应的 domain 子包。
 */
package com.brolei.aikb.common;
