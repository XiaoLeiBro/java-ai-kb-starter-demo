package com.brolei.aikb.application.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.ChatAnswer;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.llm.LlmProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 聊天相关用例的应用服务.
 *
 * <p>负责编排 RAG 问答流程：向量化问题、检索相关片段、构建提示词、调用 LLM 生成回答。 LLM 和 Embedding 调用不在数据库事务中执行。
 */
@Service
public class ChatApplicationService {

  private static final Logger log = LoggerFactory.getLogger(ChatApplicationService.class);

  private final KnowledgeBaseRepository knowledgeBaseRepository;
  private final LlmProvider llmProvider;
  private final EmbeddingProvider embeddingProvider;
  private final VectorStore vectorStore;
  private final PromptBuilder promptBuilder;

  /** 构造聊天应用服务. */
  public ChatApplicationService(
      KnowledgeBaseRepository knowledgeBaseRepository,
      LlmProvider llmProvider,
      EmbeddingProvider embeddingProvider,
      VectorStore vectorStore,
      PromptBuilder promptBuilder) {
    this.knowledgeBaseRepository = knowledgeBaseRepository;
    this.llmProvider = llmProvider;
    this.embeddingProvider = embeddingProvider;
    this.vectorStore = vectorStore;
    this.promptBuilder = promptBuilder;
  }

  /**
   * RAG 问答：检索知识库并生成回答.
   *
   * <p>LLM 和 Embedding 调用不在事务中，避免长时间占用数据库连接。
   *
   * @param userId 当前用户 ID
   * @param kbId 知识库 ID
   * @param question 用户问题
   * @param topK 检索返回的最大片段数
   * @return 包含 LLM 回答和引用片段的聊天回答
   */
  public ChatAnswer chat(UserId userId, KnowledgeBaseId kbId, String question, int topK) {
    // 1. 校验知识库归属
    KnowledgeBase kb =
        knowledgeBaseRepository
            .findById(kbId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!kb.ownerId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    // 2. 将问题向量化（不在事务中）
    float[] queryEmbedding;
    try {
      queryEmbedding = embeddingProvider.embedAll(List.of(question)).get(0);
    } catch (Exception e) {
      log.error("Failed to embed query: kbId={}", kbId.value(), e);
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "查询向量化失败: " + e.getMessage());
    }

    // 3. 向量检索（不在事务中）
    List<RetrievedChunk> references;
    try {
      references = vectorStore.search(kbId, question, queryEmbedding, topK);
    } catch (Exception e) {
      log.error("Vector search failed: kbId={}", kbId.value(), e);
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "向量检索失败: " + e.getMessage());
    }

    log.info("Chat search: kbId={}, topK={}, retrieved={}", kbId.value(), topK, references.size());

    if (references.isEmpty()) {
      return new ChatAnswer("当前知识库中没有找到相关信息", references);
    }

    // 4. 构建提示词
    String systemPrompt = promptBuilder.buildSystemPrompt(references);
    String userMessage = promptBuilder.buildUserMessage(question, references);

    // 5. 调用 LLM 生成回答（不在事务中）
    String answer;
    try {
      answer = llmProvider.chat(systemPrompt, userMessage);
    } catch (Exception e) {
      log.error("LLM chat failed: kbId={}", kbId.value(), e);
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "LLM 调用失败: " + e.getMessage());
    }

    return new ChatAnswer(answer, references);
  }
}
