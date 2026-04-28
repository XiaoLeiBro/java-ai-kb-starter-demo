package com.brolei.aikb.domain.knowledge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brolei.aikb.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

/** {@link KnowledgeDocument} 的单元测试. */
class KnowledgeDocumentTest {

  @Test
  void createShouldSetUploadedStatus() {
    KnowledgeDocument doc =
        KnowledgeDocument.create(
            KnowledgeBaseId.generate(),
            UserId.generate(),
            "test.md",
            "/path/to/test.md",
            "text/markdown",
            1024);
    assertNotNull(doc.id());
    assertEquals(DocumentStatus.UPLOADED, doc.status());
    assertEquals(0, doc.chunkCount());
  }

  @Test
  void createShouldUseSpecifiedId() {
    KnowledgeDocumentId id = KnowledgeDocumentId.generate();

    KnowledgeDocument doc =
        KnowledgeDocument.create(
            id,
            KnowledgeBaseId.generate(),
            UserId.generate(),
            "test.md",
            "/path/to/test.md",
            "text/markdown",
            1024);

    assertEquals(id, doc.id());
  }

  @Test
  void createShouldRejectZeroFileSize() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            KnowledgeDocument.create(
                KnowledgeBaseId.generate(),
                UserId.generate(),
                "test.md",
                "/path/test.md",
                "text/markdown",
                0));
  }

  @Test
  void markAsIndexingShouldChangeStatus() {
    KnowledgeDocument doc = createSample();
    doc.markAsIndexing();
    assertEquals(DocumentStatus.INDEXING, doc.status());
  }

  @Test
  void markAsReadyShouldSetChunkCount() {
    KnowledgeDocument doc = createSample();
    doc.markAsReady(5);
    assertEquals(DocumentStatus.READY, doc.status());
    assertEquals(5, doc.chunkCount());
  }

  @Test
  void markAsFailedShouldSetErrorMessage() {
    KnowledgeDocument doc = createSample();
    doc.markAsFailed("文件解析失败");
    assertEquals(DocumentStatus.FAILED, doc.status());
    assertEquals("文件解析失败", doc.errorMessage());
  }

  @Test
  void rehydrateShouldRestoreAllFields() {
    KnowledgeDocumentId id = KnowledgeDocumentId.generate();
    KnowledgeBaseId kbId = KnowledgeBaseId.generate();
    UserId ownerId = UserId.generate();
    java.time.Instant now = java.time.Instant.now();

    KnowledgeDocument doc =
        KnowledgeDocument.rehydrate(
            id,
            kbId,
            ownerId,
            "doc.txt",
            "/store/doc.txt",
            "text/plain",
            2048,
            DocumentStatus.READY,
            3,
            null,
            now,
            now);

    assertEquals(id, doc.id());
    assertEquals(kbId, doc.knowledgeBaseId());
    assertEquals(ownerId, doc.ownerId());
    assertEquals("doc.txt", doc.originalFilename());
    assertEquals(DocumentStatus.READY, doc.status());
    assertEquals(3, doc.chunkCount());
  }

  private KnowledgeDocument createSample() {
    return KnowledgeDocument.create(
        KnowledgeBaseId.generate(), UserId.generate(), "doc.md", "/path/doc.md", null, 100);
  }
}
