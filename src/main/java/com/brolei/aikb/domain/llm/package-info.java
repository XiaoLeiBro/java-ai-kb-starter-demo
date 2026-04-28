/**
 * 大模型供应商限界上下文（LLM Provider Bounded Context）.
 *
 * <p>这里定义与具体 SDK 无关的模型调用抽象。领域层只表达"需要聊天/Embedding 能力"，不关心底层是 LangChain4j、Spring AI，还是某个厂商的原生 SDK。
 *
 * <p>规划中的核心抽象：
 *
 * <pre>
 *   interface LlmProvider {
 *       ChatResponse chat(ChatRequest request);
 *       EmbeddingResponse embedding(EmbeddingRequest request);
 *       String providerName();
 *   }
 * </pre>
 *
 * <p>Demo 版只保留包边界；真实 Provider 实现放在 {@code infrastructure.llm}。
 */
package com.brolei.aikb.domain.llm;
