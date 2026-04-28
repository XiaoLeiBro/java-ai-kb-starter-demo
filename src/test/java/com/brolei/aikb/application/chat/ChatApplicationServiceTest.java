package com.brolei.aikb.application.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.ChatAnswer;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.llm.LlmProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** {@link ChatApplicationService} 的单元测试. */
class ChatApplicationServiceTest {

  private FakeKnowledgeBaseRepository knowledgeBaseRepository;
  private FakeLlmProvider llmProvider;
  private FakeEmbeddingProvider embeddingProvider;
  private FakeVectorStore vectorStore;
  private ChatApplicationService service;
  private UserId ownerId;
  private KnowledgeBase knowledgeBase;

  @BeforeEach
  void setUp() {
    knowledgeBaseRepository = new FakeKnowledgeBaseRepository();
    llmProvider = new FakeLlmProvider();
    embeddingProvider = new FakeEmbeddingProvider();
    vectorStore = new FakeVectorStore();
    service =
        new ChatApplicationService(
            knowledgeBaseRepository,
            llmProvider,
            embeddingProvider,
            vectorStore,
            new PromptBuilder());
    ownerId = UserId.generate();
    knowledgeBase = KnowledgeBase.create(ownerId, "kb", null);
    knowledgeBaseRepository.save(knowledgeBase);
  }

  @Test
  void chatShouldReturnNoResultWithoutCallingLlmWhenReferencesEmpty() {
    vectorStore.searchResults = List.of();

    ChatAnswer answer = service.chat(ownerId, knowledgeBase.id(), "找不到的问题", 5);

    assertEquals("当前知识库中没有找到相关信息", answer.answer());
    assertEquals(0, answer.references().size());
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

    ChatAnswer answer = service.chat(ownerId, knowledgeBase.id(), "年假几天？", 3);

    assertEquals("根据知识库，年假 5 天。", answer.answer());
    assertEquals(1, answer.references().size());
    assertEquals(1, embeddingProvider.embedAllCount);
    assertEquals(1, vectorStore.searchCount);
    assertEquals(1, llmProvider.chatCount);
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

  private static class FakeLlmProvider implements LlmProvider {
    private int chatCount;
    private String answer = "answer";

    @Override
    public String chat(String systemPrompt, String userMessage) {
      chatCount++;
      return answer;
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
}
