/**
 * 对话限界上下文（Chat Bounded Context）.
 *
 * <p>核心概念：
 *
 * <ul>
 *   <li><b>Conversation</b>（聚合根）：一次会话，绑定某个知识库
 *   <li><b>Message</b>（实体）：对话中的一条消息（user / assistant / system）
 *   <li><b>RetrievalContext</b>（值对象）：本次回答命中的知识片段
 * </ul>
 *
 * <p>核心用例：
 *
 * <ul>
 *   <li>新建会话
 *   <li>基于 RAG 检索 + LLM 生成回答
 *   <li>查询会话历史
 * </ul>
 */
package com.brolei.aikb.domain.chat;
