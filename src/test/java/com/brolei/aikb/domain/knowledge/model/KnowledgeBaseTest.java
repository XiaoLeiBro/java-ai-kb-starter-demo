package com.brolei.aikb.domain.knowledge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.domain.user.model.UserId;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class KnowledgeBaseTest {

  @Test
  void createShouldUseDomainIdsAndTrimName() {
    UserId ownerId = UserId.of("owner-1");

    KnowledgeBase knowledgeBase = KnowledgeBase.create(ownerId, "  公司制度  ", "demo");

    assertNotNull(knowledgeBase.id());
    assertEquals(ownerId, knowledgeBase.ownerId());
    assertEquals("公司制度", knowledgeBase.name());
    assertEquals(KnowledgeBaseStatus.ACTIVE, knowledgeBase.status());
  }

  @Test
  void renameShouldUpdateNameAndTimestamp() {
    KnowledgeBase knowledgeBase = KnowledgeBase.create(UserId.of("owner-1"), "old", null);
    OffsetDateTime oldUpdatedAt = knowledgeBase.updatedAt();

    knowledgeBase.rename("  new  ");

    assertEquals("new", knowledgeBase.name());
    assertTrue(
        knowledgeBase.updatedAt().isAfter(oldUpdatedAt)
            || knowledgeBase.updatedAt().isEqual(oldUpdatedAt));
  }

  @Test
  void deactivateShouldArchiveKnowledgeBase() {
    KnowledgeBase knowledgeBase = KnowledgeBase.create(UserId.of("owner-1"), "kb", null);

    knowledgeBase.deactivate();

    assertEquals(KnowledgeBaseStatus.ARCHIVED, knowledgeBase.status());
    assertFalse(knowledgeBase.isActive());
  }
}
