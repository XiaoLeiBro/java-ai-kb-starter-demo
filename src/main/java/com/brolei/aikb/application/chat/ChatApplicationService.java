package com.brolei.aikb.application.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.chat.model.MessageRole;
import com.brolei.aikb.domain.chat.repository.ConversationRepository;
import com.brolei.aikb.domain.chat.repository.InvocationLogRepository;
import com.brolei.aikb.domain.chat.repository.MessageRepository;
import com.brolei.aikb.domain.knowledge.model.ChatAnswer;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.llm.LlmChatResult;
import com.brolei.aikb.domain.llm.LlmProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 聊天相关用例的应用服务.
 *
 * <p>负责编排 RAG 问答流程。LLM 和 Embedding 调用不在数据库事务中执行。
 */
@Service
public class ChatApplicationService {

  private static final Logger log = LoggerFactory.getLogger(ChatApplicationService.class);

  private final KnowledgeBaseRepository knowledgeBaseRepository;
  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;
  private final InvocationLogRepository invocationLogRepository;
  private final LlmProvider llmProvider;
  private final EmbeddingProvider embeddingProvider;
  private final VectorStore vectorStore;
  private final PromptBuilder promptBuilder;
  private final TransactionTemplate transactionTemplate;
  private final String modelName;

  public ChatApplicationService(
      KnowledgeBaseRepository knowledgeBaseRepository,
      ConversationRepository conversationRepository,
      MessageRepository messageRepository,
      InvocationLogRepository invocationLogRepository,
      LlmProvider llmProvider,
      EmbeddingProvider embeddingProvider,
      VectorStore vectorStore,
      PromptBuilder promptBuilder,
      TransactionTemplate transactionTemplate,
      @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName) {
    this.knowledgeBaseRepository = knowledgeBaseRepository;
    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
    this.invocationLogRepository = invocationLogRepository;
    this.llmProvider = llmProvider;
    this.embeddingProvider = embeddingProvider;
    this.vectorStore = vectorStore;
    this.promptBuilder = promptBuilder;
    this.transactionTemplate = transactionTemplate;
    this.modelName = modelName;
  }

  /** v0.2 兼容：RAG 问答，不记录历史. */
  public ChatResult chat(UserId userId, KnowledgeBaseId kbId, String question, int topK) {
    ChatAnswer chatAnswer = doChatInternal(userId, kbId, question, topK, false);
    return new ChatResult(chatAnswer);
  }

  /** v0.3 新增：RAG 问答并记录历史. */
  public ChatResult chat(
      UserId userId,
      KnowledgeBaseId kbId,
      String question,
      int topK,
      ConversationId conversationId) {
    String userMessageId = null;
    String assistantMessageId = null;
    String invocationLogId = null;

    // 校验会话归属、状态、KB 一致性
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!conversation.ownerId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (conversation.isArchived()) {
      throw new BusinessException(ErrorCode.CONVERSATION_ARCHIVED);
    }
    if (!conversation.knowledgeBaseId().equals(kbId)) {
      throw new BusinessException(ErrorCode.KB_MISMATCH);
    }

    // TX1: 保存 user Message + 刷新 Conversation.updatedAt（独立事务，LLM 调用前提交）
    Message userMsg = saveUserMessageAndTouchConversation(conversation, question);
    userMessageId = userMsg.id().value();

    // LLM 调用（不在事务中）
    long startMs = System.currentTimeMillis();
    try {
      ChatAnswer chatAnswer = doChatInternal(userId, kbId, question, topK, true);
      long durationMs = System.currentTimeMillis() - startMs;

      // TX2: 保存 assistant Message + InvocationLog
      Message assistantMsg =
          Message.create(conversationId, MessageRole.ASSISTANT, chatAnswer.answer());
      var result =
          saveAssistantAndLog(assistantMsg, chatAnswer, userId, kbId, conversationId, durationMs);
      assistantMessageId = result.assistantMessageId;
      invocationLogId = result.invocationLogId;

      return new ChatResult(chatAnswer, userMessageId, assistantMessageId, invocationLogId);

    } catch (BusinessException e) {
      long durationMs = System.currentTimeMillis() - startMs;
      if (e.errorCode() != ErrorCode.LLM_PROVIDER_ERROR) {
        throw e;
      }
      String sanitizedError = ErrorSanitizer.sanitizeAndTruncate(e.getMessage(), 4000);
      saveFailedInvocationLog(userId, kbId, conversationId, userMsg, sanitizedError, durationMs);
      throw new BusinessException(
          ErrorCode.LLM_PROVIDER_ERROR, ErrorSanitizer.sanitize(e.getMessage()));
    } catch (Exception e) {
      long durationMs = System.currentTimeMillis() - startMs;
      log.error("LLM call failed: kbId={}, conversationId={}", kbId.value(), conversationId, e);
      String sanitizedError = ErrorSanitizer.sanitizeAndTruncate(e.getMessage(), 4000);
      saveFailedInvocationLog(userId, kbId, conversationId, userMsg, sanitizedError, durationMs);
      throw new BusinessException(
          ErrorCode.LLM_PROVIDER_ERROR, ErrorSanitizer.sanitize(e.getMessage()));
    }
  }

  private ChatAnswer doChatInternal(
      UserId userId, KnowledgeBaseId kbId, String question, int topK, boolean includeUsage) {
    // 校验知识库归属和状态
    KnowledgeBase kb =
        knowledgeBaseRepository
            .findById(kbId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!kb.ownerId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (kb.isArchived()) {
      throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ARCHIVED);
    }

    // 向量化问题
    float[] queryEmbedding;
    try {
      queryEmbedding = embeddingProvider.embedAll(List.of(question)).get(0);
    } catch (Exception e) {
      log.error("Failed to embed query: kbId={}", kbId.value(), e);
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "查询向量化失败: " + e.getMessage());
    }

    // 向量检索
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

    // 构建提示词
    String systemPrompt = promptBuilder.buildSystemPrompt(references);
    String userMessage = promptBuilder.buildUserMessage(question, references);

    // 调用 LLM
    try {
      if (!includeUsage) {
        return new ChatAnswer(
            toPlainTextAnswer(llmProvider.chat(systemPrompt, userMessage)), references);
      }
      LlmChatResult result = llmProvider.chatWithUsage(systemPrompt, userMessage);
      LlmChatResult plainResult =
          new LlmChatResult(
              toPlainTextAnswer(result.answer()),
              result.promptTokens(),
              result.completionTokens(),
              result.totalTokens());
      return new ChatAnswer(plainResult.answer(), references, plainResult);
    } catch (Exception e) {
      log.error("LLM chat failed: kbId={}", kbId.value(), e);
      throw new BusinessException(ErrorCode.LLM_PROVIDER_ERROR, "LLM 调用失败: " + e.getMessage());
    }
  }

  private String toPlainTextAnswer(String answer) {
    if (answer == null || answer.isBlank()) {
      return "";
    }
    return answer
        .replace("```", "")
        .replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "")
        .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
        .replaceAll("__(.*?)__", "$1")
        .replaceAll("`([^`]+)`", "$1")
        .replaceAll("(?m)^\\s*>\\s?", "")
        .replaceAll("(?m)^\\s*[-*+]\\s+", "")
        .replace("|", "")
        .trim();
  }

  private Message saveUserMessageAndTouchConversation(Conversation conversation, String question) {
    return transactionTemplate.execute(
        status -> {
          conversation.touch();
          conversationRepository.save(conversation);
          Message message = Message.create(conversation.id(), MessageRole.USER, question);
          return messageRepository.save(message);
        });
  }

  private Tx2Result saveAssistantAndLog(
      Message assistantMsg,
      ChatAnswer chatAnswer,
      UserId userId,
      KnowledgeBaseId kbId,
      ConversationId conversationId,
      long durationMs) {
    return transactionTemplate.execute(
        status -> {
          Message saved = messageRepository.save(assistantMsg);
          LlmChatResult llmResult = chatAnswer.llmResult();
          if (llmResult == null) {
            return new Tx2Result(saved.id().value(), null);
          }
          InvocationLog invocationLog =
              InvocationLog.recordSuccess(
                  userId,
                  kbId,
                  conversationId,
                  saved.id(),
                  modelName,
                  llmResult.promptTokens(),
                  llmResult.completionTokens(),
                  durationMs);
          invocationLogRepository.save(invocationLog);
          return new Tx2Result(saved.id().value(), invocationLog.id().value());
        });
  }

  private record Tx2Result(String assistantMessageId, String invocationLogId) {}

  private void saveFailedInvocationLog(
      UserId userId,
      KnowledgeBaseId kbId,
      ConversationId conversationId,
      Message userMsg,
      String errorMessage,
      long durationMs) {
    try {
      transactionTemplate.executeWithoutResult(
          status -> {
            InvocationLog log =
                InvocationLog.recordFailure(
                    userId,
                    kbId,
                    conversationId,
                    userMsg.id(),
                    modelName,
                    durationMs,
                    errorMessage);
            invocationLogRepository.save(log);
          });
    } catch (Exception ex) {
      ChatApplicationService.log.error("Failed to save invocation log", ex);
    }
  }
}
