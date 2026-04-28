package com.brolei.aikb.application.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.chat.model.InvocationStatus;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.chat.repository.ConversationRepository;
import com.brolei.aikb.domain.chat.repository.InvocationLogRepository;
import com.brolei.aikb.domain.chat.repository.MessageRepository;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.llm.LlmChatResult;
import com.brolei.aikb.domain.llm.LlmProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/** {@link ChatApplicationService} 的单元测试. */
class ChatApplicationServiceTest {

  private FakeKnowledgeBaseRepository knowledgeBaseRepository;
  private FakeConversationRepository conversationRepository;
  private FakeMessageRepository messageRepository;
  private FakeInvocationLogRepository invocationLogRepository;
  private FakeLlmProvider llmProvider;
  private FakeEmbeddingProvider embeddingProvider;
  private FakeVectorStore vectorStore;
  private ChatApplicationService service;
  private UserId ownerId;
  private KnowledgeBase knowledgeBase;
  private Conversation conversation;

  @BeforeEach
  void setUp() {
    knowledgeBaseRepository = new FakeKnowledgeBaseRepository();
    conversationRepository = new FakeConversationRepository();
    messageRepository = new FakeMessageRepository();
    invocationLogRepository = new FakeInvocationLogRepository();
    llmProvider = new FakeLlmProvider();
    embeddingProvider = new FakeEmbeddingProvider();
    vectorStore = new FakeVectorStore();
    service =
        new ChatApplicationService(
            knowledgeBaseRepository,
            conversationRepository,
            messageRepository,
            invocationLogRepository,
            llmProvider,
            embeddingProvider,
            vectorStore,
            new PromptBuilder(),
            new TransactionTemplate(new NoopTransactionManager()),
            "test-model");
    ownerId = UserId.generate();
    knowledgeBase = KnowledgeBase.create(ownerId, "kb", null);
    knowledgeBaseRepository.save(knowledgeBase);
    conversation = Conversation.create(ownerId, knowledgeBase.id(), "test-conv");
    conversationRepository.save(conversation);
  }

  @Test
  void chatShouldReturnNoResultWithoutCallingLlmWhenReferencesEmpty() {
    vectorStore.searchResults = List.of();

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "找不到的问题", 5);

    assertEquals("当前知识库中没有找到相关信息", result.chatAnswer().answer());
    assertEquals(0, result.chatAnswer().references().size());
    assertEquals(1, embeddingProvider.embedAllCount);
    assertEquals(1, vectorStore.searchCount);
    assertEquals(0, llmProvider.chatCount);
  }

  @Test
  void chatShouldCallLlmWhenReferencesExist() {
    vectorStore.searchResults =
        List.of(
            new RetrievedChunk(
                knowledgeBase.id(),
                KnowledgeDocumentId.generate(),
                DocumentChunkId.generate(),
                "doc.md",
                0,
                "年假 5 天",
                0.9));
    llmProvider.answer = "根据知识库，年假 5 天。";

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3);

    assertEquals("根据知识库，年假 5 天。", result.chatAnswer().answer());
    assertEquals(1, result.chatAnswer().references().size());
    assertEquals(1, embeddingProvider.embedAllCount);
    assertEquals(1, vectorStore.searchCount);
    assertEquals(1, llmProvider.chatCount);
    assertEquals(1, llmProvider.chatWithoutUsageCount);
    assertEquals(0, llmProvider.chatWithUsageCount);
  }

  @Test
  void chatShouldReturnNotFoundWhenKnowledgeBaseBelongsToAnotherUser() {
    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(UserId.generate(), knowledgeBase.id(), "年假几天？", 3));

    assertEquals(ErrorCode.NOT_FOUND, ex.errorCode());
    assertEquals(0, embeddingProvider.embedAllCount);
    assertEquals(0, vectorStore.searchCount);
    assertEquals(0, llmProvider.chatCount);
  }

  @Test
  void chatWithConversationIdShouldPersistMessagesAndLog() {
    vectorStore.searchResults =
        List.of(
            new RetrievedChunk(
                knowledgeBase.id(),
                KnowledgeDocumentId.generate(),
                DocumentChunkId.generate(),
                "doc.md",
                0,
                "年假 5 天",
                0.9));
    llmProvider.answer = "根据知识库，年假 5 天。";
    llmProvider.promptTokens = 100;
    llmProvider.completionTokens = 50;

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, conversation.id());

    assertEquals("根据知识库，年假 5 天。", result.chatAnswer().answer());
    assertNotNull(result.userMessageId());
    assertNotNull(result.assistantMessageId());
    assertNotNull(result.invocationLogId());
    assertEquals(2, messageRepository.savedMessages.size());
    assertEquals(1, invocationLogRepository.savedLogs.size());
    InvocationLog log = invocationLogRepository.savedLogs.getFirst();
    assertEquals(result.assistantMessageId(), log.messageId().value());
    assertEquals(100, log.promptTokens());
    assertEquals(50, log.completionTokens());
    assertEquals(0, llmProvider.chatWithoutUsageCount);
    assertEquals(1, llmProvider.chatWithUsageCount);
  }

  @Test
  void chatWithConversationIdShouldPersistEmptyAssistantMessageAsSuccess() {
    vectorStore.searchResults =
        List.of(
            new RetrievedChunk(
                knowledgeBase.id(),
                KnowledgeDocumentId.generate(),
                DocumentChunkId.generate(),
                "doc.md",
                0,
                "年假 5 天",
                0.9));
    llmProvider.answer = "";

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, conversation.id());

    assertEquals("", result.chatAnswer().answer());
    assertNotNull(result.assistantMessageId());
    assertNotNull(result.invocationLogId());
    assertEquals("", messageRepository.savedMessages.get(1).content());
    assertEquals(1, invocationLogRepository.savedLogs.size());
  }

  @Test
  void chatWithConversationIdShouldNotCreateInvocationLogWhenNoLlmCall() {
    vectorStore.searchResults = List.of();

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "找不到的问题", 5, conversation.id());

    assertEquals("当前知识库中没有找到相关信息", result.chatAnswer().answer());
    assertNotNull(result.userMessageId());
    assertNotNull(result.assistantMessageId());
    assertNull(result.invocationLogId());
    assertEquals(2, messageRepository.savedMessages.size());
    assertEquals(0, invocationLogRepository.savedLogs.size());
    assertEquals(0, llmProvider.chatCount);
  }

  @Test
  void chatWithoutConversationIdShouldNotPersistHistory() {
    vectorStore.searchResults =
        List.of(
            new RetrievedChunk(
                knowledgeBase.id(),
                KnowledgeDocumentId.generate(),
                DocumentChunkId.generate(),
                "doc.md",
                0,
                "年假 5 天",
                0.9));
    llmProvider.answer = "根据知识库，年假 5 天。";

    ChatResult result = service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3);

    assertEquals("根据知识库，年假 5 天。", result.chatAnswer().answer());
    assertNull(result.userMessageId());
    assertNull(result.assistantMessageId());
    assertNull(result.invocationLogId());
  }

  @Test
  void chatWithNonExistentConversationIdShouldReturnNotFound() {
    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, ConversationId.generate()));

    assertEquals(ErrorCode.NOT_FOUND, ex.errorCode());
  }

  @Test
  void chatWithConversationIdCrossUserShouldReturnNotFound() {
    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () ->
                service.chat(UserId.generate(), knowledgeBase.id(), "年假几天？", 3, conversation.id()));

    assertEquals(ErrorCode.NOT_FOUND, ex.errorCode());
  }

  @Test
  void chatWithArchivedConversationShouldReturnConflict() {
    conversation.archive();
    conversationRepository.save(conversation);

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, conversation.id()));

    assertEquals(ErrorCode.CONVERSATION_ARCHIVED, ex.errorCode());
  }

  @Test
  void chatWithKbMismatchShouldReturnBadRequest() {
    KnowledgeBase otherKb = KnowledgeBase.create(ownerId, "other", null);
    knowledgeBaseRepository.save(otherKb);

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(ownerId, otherKb.id(), "年假几天？", 3, conversation.id()));

    assertEquals(ErrorCode.KB_MISMATCH, ex.errorCode());
  }

  @Test
  void chatWithArchivedKnowledgeBaseShouldReturnBadRequest() {
    knowledgeBase.deactivate();
    knowledgeBaseRepository.save(knowledgeBase);

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, conversation.id()));

    assertEquals(ErrorCode.KNOWLEDGE_BASE_ARCHIVED, ex.errorCode());
  }

  @Test
  void chatWithConversationIdShouldKeepUserMessageAndSaveFailedLogWhenLlmFails() {
    vectorStore.searchResults =
        List.of(
            new RetrievedChunk(
                knowledgeBase.id(),
                KnowledgeDocumentId.generate(),
                DocumentChunkId.generate(),
                "doc.md",
                0,
                "年假 5 天",
                0.9));
    llmProvider.failure = new RuntimeException("provider failed with sk-abcdefghijklmnop");

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3, conversation.id()));

    assertEquals(ErrorCode.LLM_PROVIDER_ERROR, ex.errorCode());
    assertEquals(1, messageRepository.savedMessages.size());
    assertEquals(1, invocationLogRepository.savedLogs.size());
    InvocationLog log = invocationLogRepository.savedLogs.getFirst();
    assertEquals(InvocationStatus.FAILED, log.status());
    assertEquals(messageRepository.savedMessages.getFirst().id(), log.messageId());
    assertEquals("LLM 调用失败: provider failed with sk-***", log.errorMessage());
  }

  // --- Fake implementations ---

  private static class FakeKnowledgeBaseRepository implements KnowledgeBaseRepository {
    private final Map<KnowledgeBaseId, KnowledgeBase> records = new HashMap<>();

    @Override
    public KnowledgeBase save(KnowledgeBase knowledgeBase) {
      records.put(knowledgeBase.id(), knowledgeBase);
      return knowledgeBase;
    }

    @Override
    public Optional<KnowledgeBase> findById(KnowledgeBaseId id) {
      return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<KnowledgeBase> findByOwnerId(UserId ownerId) {
      return records.values().stream().filter(kb -> kb.ownerId().equals(ownerId)).toList();
    }

    @Override
    public void archiveById(KnowledgeBaseId id) {}
  }

  private static class FakeConversationRepository implements ConversationRepository {
    private final Map<ConversationId, Conversation> records = new HashMap<>();

    @Override
    public Conversation save(Conversation conversation) {
      records.put(conversation.id(), conversation);
      return conversation;
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
      return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<Conversation> findByOwnerIdAndStatus(
        UserId ownerId, com.brolei.aikb.domain.chat.model.ConversationStatus status) {
      return records.values().stream()
          .filter(c -> c.ownerId().equals(ownerId) && c.status() == status)
          .toList();
    }

    @Override
    public List<Conversation> findByOwnerIdAndKnowledgeBaseIdAndStatus(
        UserId ownerId,
        KnowledgeBaseId knowledgeBaseId,
        com.brolei.aikb.domain.chat.model.ConversationStatus status) {
      return records.values().stream()
          .filter(
              c ->
                  c.ownerId().equals(ownerId)
                      && c.knowledgeBaseId().equals(knowledgeBaseId)
                      && c.status() == status)
          .toList();
    }
  }

  private static class FakeMessageRepository implements MessageRepository {
    private final List<Message> savedMessages = new ArrayList<>();

    @Override
    public Message save(Message message) {
      savedMessages.add(message);
      return message;
    }

    @Override
    public List<Message> findByConversationId(ConversationId conversationId) {
      return savedMessages.stream().filter(m -> m.conversationId().equals(conversationId)).toList();
    }
  }

  private static class FakeInvocationLogRepository implements InvocationLogRepository {
    private final List<InvocationLog> savedLogs = new ArrayList<>();

    @Override
    public InvocationLog save(InvocationLog invocationLog) {
      savedLogs.add(invocationLog);
      return invocationLog;
    }

    @Override
    public List<InvocationLog> findByOwnerId(UserId ownerId) {
      return savedLogs.stream().filter(l -> l.ownerId().equals(ownerId)).toList();
    }

    @Override
    public List<InvocationLog> findByOwnerIdAndKnowledgeBaseId(
        UserId ownerId, KnowledgeBaseId knowledgeBaseId) {
      return savedLogs.stream()
          .filter(
              l ->
                  l.ownerId().equals(ownerId)
                      && l.knowledgeBaseId() != null
                      && l.knowledgeBaseId().equals(knowledgeBaseId))
          .toList();
    }

    @Override
    public List<InvocationLog> findByOwnerIdAndDateRange(
        UserId ownerId, java.time.Instant dateFrom, java.time.Instant dateTo) {
      return savedLogs.stream()
          .filter(
              l ->
                  l.ownerId().equals(ownerId)
                      && !l.createdAt().isBefore(dateFrom)
                      && !l.createdAt().isAfter(dateTo))
          .toList();
    }
  }

  private static class FakeLlmProvider implements LlmProvider {
    private int chatCount;
    private int chatWithoutUsageCount;
    private int chatWithUsageCount;
    private String answer = "answer";
    private int promptTokens;
    private int completionTokens;
    private RuntimeException failure;

    @Override
    public String chat(String systemPrompt, String userMessage) {
      chatCount++;
      chatWithoutUsageCount++;
      if (failure != null) {
        throw failure;
      }
      return answer;
    }

    @Override
    public LlmChatResult chatWithUsage(String systemPrompt, String userMessage) {
      chatCount++;
      chatWithUsageCount++;
      if (failure != null) {
        throw failure;
      }
      return LlmChatResult.of(answer, promptTokens, completionTokens);
    }
  }

  private static class FakeEmbeddingProvider implements EmbeddingProvider {
    private int embedAllCount;

    @Override
    public List<float[]> embedAll(List<String> texts) {
      embedAllCount++;
      List<float[]> vectors = new ArrayList<>();
      for (int i = 0; i < texts.size(); i++) {
        vectors.add(new float[] {1.0f, 0.0f});
      }
      return vectors;
    }
  }

  private static class FakeVectorStore implements VectorStore {
    private int searchCount;
    private List<RetrievedChunk> searchResults = List.of();

    @Override
    public void saveAll(List<VectorChunk> chunks) {}

    @Override
    public List<RetrievedChunk> search(
        KnowledgeBaseId knowledgeBaseId, String query, float[] queryEmbedding, int topK) {
      searchCount++;
      return searchResults;
    }
  }

  private static class NoopTransactionManager implements PlatformTransactionManager {

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
      return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus status) {}

    @Override
    public void rollback(TransactionStatus status) {}
  }
}
