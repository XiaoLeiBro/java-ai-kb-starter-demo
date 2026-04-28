package com.brolei.aikb.application.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.application.common.FileValidator;
import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.knowledge.model.DocumentChunk;
import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.DocumentStatus;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import com.brolei.aikb.domain.knowledge.repository.DocumentChunkRepository;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeDocumentRepository;
import com.brolei.aikb.domain.knowledge.service.FileStorage;
import com.brolei.aikb.domain.knowledge.service.TextSplitter;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** {@link KnowledgeApplicationService} 的单元测试. */
class KnowledgeApplicationServiceTest {

  private FakeKnowledgeBaseRepository knowledgeBaseRepository;
  private FakeKnowledgeDocumentRepository knowledgeDocumentRepository;
  private FakeDocumentChunkRepository documentChunkRepository;
  private FakeFileStorage fileStorage;
  private FakeTextSplitter textSplitter;
  private FakeEmbeddingProvider embeddingProvider;
  private FakeVectorStore vectorStore;
  private KnowledgeApplicationService service;
  private UserId ownerId;
  private KnowledgeBase knowledgeBase;

  @BeforeEach
  void setUp() {
    knowledgeBaseRepository = new FakeKnowledgeBaseRepository();
    knowledgeDocumentRepository = new FakeKnowledgeDocumentRepository();
    documentChunkRepository = new FakeDocumentChunkRepository();
    fileStorage = new FakeFileStorage();
    textSplitter = new FakeTextSplitter();
    embeddingProvider = new FakeEmbeddingProvider();
    vectorStore = new FakeVectorStore();
    service =
        new KnowledgeApplicationService(
            knowledgeBaseRepository,
            knowledgeDocumentRepository,
            documentChunkRepository,
            fileStorage,
            textSplitter,
            embeddingProvider,
            vectorStore,
            new FileValidator(),
            new AiKbProperties());
    ownerId = UserId.generate();
    knowledgeBase = KnowledgeBase.create(ownerId, "kb", null);
    knowledgeBaseRepository.save(knowledgeBase);
  }

  @Test
  void uploadAndIndexDocumentShouldUseSameDocumentIdForStorageAndDomain() {
    byte[] content = "hello knowledge base".getBytes(StandardCharsets.UTF_8);

    KnowledgeDocument document =
        service.uploadAndIndexDocument(ownerId, knowledgeBase.id(), "demo.md", content);

    assertEquals(DocumentStatus.READY, document.status());
    assertEquals(1, document.chunkCount());
    assertEquals(document.id(), fileStorage.docId);
    assertTrue(document.storagePath().contains(document.id().value()));
    assertEquals(document.id(), documentChunkRepository.savedChunks.get(0).documentId());
    assertEquals(document.id(), vectorStore.savedChunks.get(0).documentId());
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

  private static class FakeKnowledgeDocumentRepository implements KnowledgeDocumentRepository {
    private final Map<KnowledgeDocumentId, KnowledgeDocument> records = new HashMap<>();

    @Override
    public KnowledgeDocument save(KnowledgeDocument document) {
      records.put(document.id(), document);
      return document;
    }

    @Override
    public Optional<KnowledgeDocument> findById(KnowledgeDocumentId id) {
      return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<KnowledgeDocument> findByKnowledgeBaseId(KnowledgeBaseId kbId) {
      return records.values().stream().filter(doc -> doc.knowledgeBaseId().equals(kbId)).toList();
    }

    @Override
    public List<KnowledgeDocument> findByKnowledgeBaseIdAndOwnerId(
        KnowledgeBaseId kbId, UserId ownerId) {
      return records.values().stream()
          .filter(doc -> doc.knowledgeBaseId().equals(kbId))
          .filter(doc -> doc.ownerId().equals(ownerId))
          .toList();
    }
  }

  private static class FakeDocumentChunkRepository implements DocumentChunkRepository {
    private final List<DocumentChunk> savedChunks = new ArrayList<>();

    @Override
    public void saveAll(List<DocumentChunk> chunks) {
      savedChunks.addAll(chunks);
    }

    @Override
    public List<DocumentChunk> findByDocumentId(KnowledgeDocumentId documentId) {
      return savedChunks.stream().filter(chunk -> chunk.documentId().equals(documentId)).toList();
    }

    @Override
    public List<DocumentChunk> findByKnowledgeBaseId(KnowledgeBaseId kbId) {
      return savedChunks.stream().filter(chunk -> chunk.knowledgeBaseId().equals(kbId)).toList();
    }

    @Override
    public Optional<DocumentChunk> findById(DocumentChunkId id) {
      return savedChunks.stream().filter(chunk -> chunk.id().equals(id)).findFirst();
    }
  }

  private static class FakeFileStorage implements FileStorage {
    private KnowledgeDocumentId docId;

    @Override
    public String store(
        UserId userId,
        KnowledgeBaseId kbId,
        KnowledgeDocumentId docId,
        String originalFilename,
        byte[] content) {
      this.docId = docId;
      return userId.value() + "/" + kbId.value() + "/" + docId.value() + "/" + originalFilename;
    }

    @Override
    public byte[] read(String storagePath) {
      return new byte[0];
    }
  }

  private static class FakeTextSplitter implements TextSplitter {
    @Override
    public List<String> split(String text) {
      return List.of(text);
    }
  }

  private static class FakeEmbeddingProvider implements EmbeddingProvider {
    @Override
    public List<float[]> embedAll(List<String> texts) {
      return texts.stream().map(text -> new float[] {1.0f, 0.0f}).toList();
    }
  }

  private static class FakeVectorStore implements VectorStore {
    private final List<VectorChunk> savedChunks = new ArrayList<>();

    @Override
    public void saveAll(List<VectorChunk> chunks) {
      savedChunks.addAll(chunks);
    }

    @Override
    public List<RetrievedChunk> search(
        KnowledgeBaseId knowledgeBaseId, String query, float[] queryEmbedding, int topK) {
      return List.of();
    }
  }
}
