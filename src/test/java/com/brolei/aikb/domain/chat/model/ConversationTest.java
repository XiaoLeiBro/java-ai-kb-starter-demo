package com.brolei.aikb.domain.chat.model;

import static org.junit.jupiter.api.Assertions.*;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

class ConversationTest {

  @Test
  void createShouldSucceedWithValidInput() {
    Conversation c = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), "Test");
    assertNotNull(c.id());
    assertEquals("Test", c.title());
    assertTrue(c.isActive());
    assertFalse(c.isArchived());
  }

  @Test
  void createShouldFailWithNullOwnerId() {
    assertThrows(
        NullPointerException.class,
        () -> Conversation.create(null, KnowledgeBaseId.generate(), "Test"));
  }

  @Test
  void createShouldFailWithNullKnowledgeBaseId() {
    assertThrows(
        NullPointerException.class, () -> Conversation.create(UserId.generate(), null, "Test"));
  }

  @Test
  void createShouldSucceedWithNullTitle() {
    Conversation c = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), null);
    assertNull(c.title());
  }

  @Test
  void createShouldFailWithTitleExceeding200() {
    String longTitle = "x".repeat(201);
    assertThrows(
        IllegalArgumentException.class,
        () -> Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), longTitle));
  }

  @Test
  void archiveShouldChangeStatusAndUpdateTime() {
    Conversation c = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), "Test");
    c.archive();
    assertTrue(c.isArchived());
    assertFalse(c.isActive());
  }

  @Test
  void archiveShouldBeIdempotent() {
    Conversation c = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), "Test");
    c.archive();
    assertDoesNotThrow(c::archive);
    assertTrue(c.isArchived());
  }

  @Test
  void hasTitleShouldReturnCorrectly() {
    Conversation c1 = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), "Title");
    Conversation c2 = Conversation.create(UserId.generate(), KnowledgeBaseId.generate(), null);
    assertTrue(c1.hasTitle());
    assertFalse(c2.hasTitle());
  }
}
